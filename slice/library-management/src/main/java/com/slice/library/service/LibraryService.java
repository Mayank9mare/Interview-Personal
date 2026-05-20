package com.slice.library.service;

import com.slice.library.exception.LibraryException;
import com.slice.library.model.Book;
import com.slice.library.model.BorrowRecord;
import com.slice.library.model.BorrowStatus;
import com.slice.library.model.User;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class LibraryService {

    private static final int MAX_BORROW_PER_REQUEST = 3;

    private final Map<String, Book>         books       = new ConcurrentHashMap<>();
    private final Map<String, User>         users       = new ConcurrentHashMap<>();
    private final List<BorrowRecord>        history     = new CopyOnWriteArrayList<>();
    // per-book lock: never lock on `books` itself to allow concurrent access to different books
    private final ConcurrentHashMap<String, ReentrantLock> bookLocks = new ConcurrentHashMap<>();

    // ── Catalogue management ──────────────────────────────────────────────────

    public void addBook(String bookId, String bookName, String author, int count) {
        if (count < 0) throw new LibraryException("count cannot be negative", bookId);
        books.put(bookId, new Book(bookId, bookName, author, count));
        bookLocks.putIfAbsent(bookId, new ReentrantLock());
    }

    public void addUser(String username, String name, String password) {
        if (users.containsKey(username))
            throw new LibraryException("User already registered: " + username);
        users.put(username, new User(username, name, password));
    }

    // ── Borrow ────────────────────────────────────────────────────────────────

    /**
     * Atomically borrows all requested books for a user.
     *
     * Constraints enforced:
     *   1. At most MAX_BORROW_PER_REQUEST books per call.
     *   2. User must not already have an active (BORROWED) record for any requested book.
     *   3. Each book must exist and have at least one copy available.
     *
     * Deadlock prevention: locks are always acquired in lexicographic bookId order.
     */
    public List<BorrowRecord> borrowBooks(String userId, List<String> bookIds) {
        validateUser(userId);
        if (bookIds == null || bookIds.isEmpty())
            throw new LibraryException("bookIds list is empty");
        if (bookIds.size() > MAX_BORROW_PER_REQUEST)
            throw new LibraryException("cannot borrow more than " + MAX_BORROW_PER_REQUEST + " books at once");

        // Constraint 1 check: all books must exist
        for (String id : bookIds) {
            if (!books.containsKey(id))
                throw new LibraryException("book not found", id);
        }

        // Constraint 2 (fast-fail before locking): no active borrow of same book by this user
        Set<String> alreadyBorrowed = history.stream()
                .filter(r -> r.getUserId().equals(userId) && r.getStatus() == BorrowStatus.BORROWED)
                .map(BorrowRecord::getBookId)
                .collect(Collectors.toSet());
        for (String id : bookIds) {
            if (alreadyBorrowed.contains(id))
                throw new LibraryException("user already has an active borrow for book", id);
        }

        // Sort to impose a global lock-acquisition order → prevents deadlock
        List<String> sorted = bookIds.stream().sorted().collect(Collectors.toList());
        List<ReentrantLock> acquired = new ArrayList<>();

        try {
            for (String id : sorted) {
                ReentrantLock lock = bookLocks.computeIfAbsent(id, k -> new ReentrantLock());
                lock.lock();
                acquired.add(lock);
            }

            // Inside all locks: verify availability (simulates DB optimistic check)
            for (String id : sorted) {
                Book book = books.get(id);
                if (book.getCount() <= 0)
                    throw new LibraryException("book is not available", id);
            }

            // All checks passed — commit
            List<BorrowRecord> records = new ArrayList<>();
            for (String id : sorted) {
                Book book = books.get(id);
                book.setCount(book.getCount() - 1);
                BorrowRecord record = new BorrowRecord(id, userId);
                history.add(record);
                records.add(record);
            }
            return records;

        } finally {
            // Release in reverse order (convention; any order is safe once acquired)
            for (int i = acquired.size() - 1; i >= 0; i--)
                acquired.get(i).unlock();
        }
    }

    // ── Return ────────────────────────────────────────────────────────────────

    public BorrowRecord returnBook(String userId, String bookId) {
        validateUser(userId);
        Book book = books.get(bookId);
        if (book == null) throw new LibraryException("book not found", bookId);

        ReentrantLock lock = bookLocks.computeIfAbsent(bookId, k -> new ReentrantLock());
        lock.lock();
        try {
            BorrowRecord record = history.stream()
                    .filter(r -> r.getBookId().equals(bookId)
                            && r.getUserId().equals(userId)
                            && r.getStatus() == BorrowStatus.BORROWED)
                    .findFirst()
                    .orElseThrow(() -> new LibraryException(
                            "no active borrow record found for user " + userId, bookId));

            record.markReturned();
            book.setCount(book.getCount() + 1);
            return record;
        } finally {
            lock.unlock();
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** Case-insensitive substring match on title or author. */
    public List<Book> searchBooks(String query) {
        String q = query.toLowerCase();
        return books.values().stream()
                .filter(b -> b.getBookName().toLowerCase().contains(q)
                        || b.getAuthor().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<Book> getAvailableBooks() {
        return books.values().stream()
                .filter(b -> b.getCount() > 0)
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getBorrowedBooks(String userId) {
        validateUser(userId);
        return history.stream()
                .filter(r -> r.getUserId().equals(userId) && r.getStatus() == BorrowStatus.BORROWED)
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getBorrowHistory(String userId) {
        validateUser(userId);
        return history.stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<BorrowRecord> getBookBorrowHistory(String bookId) {
        if (!books.containsKey(bookId)) throw new LibraryException("book not found", bookId);
        return history.stream()
                .filter(r -> r.getBookId().equals(bookId))
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validateUser(String userId) {
        if (!users.containsKey(userId))
            throw new LibraryException("user not found: " + userId);
    }
}
