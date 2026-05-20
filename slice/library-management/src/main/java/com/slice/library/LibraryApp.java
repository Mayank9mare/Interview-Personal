package com.slice.library;

import com.slice.library.exception.LibraryException;
import com.slice.library.model.BorrowRecord;
import com.slice.library.model.Book;
import com.slice.library.service.LibraryService;

import java.util.Arrays;
import java.util.List;

public class LibraryApp {

    private static final LibraryService svc = new LibraryService();

    public static void main(String[] args) {
        setup();

        section("Available books");
        svc.getAvailableBooks().forEach(System.out::println);

        section("Search 'java'");
        svc.searchBooks("java").forEach(System.out::println);

        section("Borrow B1 + B2 for alice");
        List<BorrowRecord> records = svc.borrowBooks("alice", Arrays.asList("B1", "B2"));
        records.forEach(System.out::println);

        section("Available after borrow");
        svc.getAvailableBooks().forEach(System.out::println);

        section("Constraint-2: duplicate active borrow (should throw)");
        tryBorrow("alice", Arrays.asList("B1", "B3"));

        section("Constraint-1: more than 3 books (should throw)");
        tryBorrow("alice", Arrays.asList("B3", "B4", "B5", "B6"));

        section("Borrow B3 for bob");
        svc.borrowBooks("bob", Arrays.asList("B3")).forEach(System.out::println);

        section("B3 has 0 copies — alice tries to borrow (should throw)");
        tryBorrow("alice", Arrays.asList("B3"));

        section("alice returns B1");
        System.out.println(svc.returnBook("alice", "B1"));

        section("Available after return");
        svc.getAvailableBooks().forEach(System.out::println);

        section("alice borrow history");
        svc.getBorrowHistory("alice").forEach(System.out::println);

        section("Currently borrowed by alice");
        svc.getBorrowedBooks("alice").forEach(System.out::println);

        section("Concurrent borrow stress test");
        concurrencyTest();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void setup() {
        // Books (count = 1 to expose concurrency edge cases)
        svc.addBook("B1", "Effective Java",              "Joshua Bloch",     2);
        svc.addBook("B2", "Clean Code",                  "Robert Martin",    3);
        svc.addBook("B3", "Java Concurrency in Practice","Brian Goetz",      1);
        svc.addBook("B4", "Design Patterns",             "Gang of Four",     2);
        svc.addBook("B5", "Refactoring",                 "Martin Fowler",    1);
        svc.addBook("B6", "The Pragmatic Programmer",    "Hunt & Thomas",    1);

        // Users
        svc.addUser("alice", "Alice Smith",  "pass1");
        svc.addUser("bob",   "Bob Jones",    "pass2");
        svc.addUser("carol", "Carol White",  "pass3");
    }

    private static void tryBorrow(String userId, List<String> bookIds) {
        try {
            svc.borrowBooks(userId, bookIds).forEach(System.out::println);
        } catch (LibraryException e) {
            System.out.println("  [EXPECTED] " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    /** Two threads race to grab the last copy of B5. Only one should succeed. */
    private static void concurrencyTest() {
        svc.addUser("t1", "Thread1", "x");
        svc.addUser("t2", "Thread2", "x");

        Thread ta = new Thread(() -> {
            try {
                svc.borrowBooks("t1", Arrays.asList("B5"));
                System.out.println("  t1 got B5");
            } catch (LibraryException e) {
                System.out.println("  t1 failed: " + e.getMessage());
            }
        });
        Thread tb = new Thread(() -> {
            try {
                svc.borrowBooks("t2", Arrays.asList("B5"));
                System.out.println("  t2 got B5");
            } catch (LibraryException e) {
                System.out.println("  t2 failed: " + e.getMessage());
            }
        });

        ta.start(); tb.start();
        try { ta.join(); tb.join(); } catch (InterruptedException ignored) {}
        System.out.println("  B5 remaining copies: " +
                svc.getAvailableBooks().stream()
                        .filter(b -> b.getBookId().equals("B5"))
                        .mapToInt(Book::getCount).sum());
    }
}
