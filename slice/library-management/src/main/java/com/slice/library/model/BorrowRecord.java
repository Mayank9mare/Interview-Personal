package com.slice.library.model;

import java.time.LocalDateTime;

public class BorrowRecord {

    private final String bookId;
    private final String userId;
    private volatile BorrowStatus status;
    private final LocalDateTime borrowedAt;
    private volatile LocalDateTime returnedAt;

    public BorrowRecord(String bookId, String userId) {
        this.bookId     = bookId;
        this.userId     = userId;
        this.status     = BorrowStatus.BORROWED;
        this.borrowedAt = LocalDateTime.now();
    }

    public String       getBookId()     { return bookId; }
    public String       getUserId()     { return userId; }
    public BorrowStatus getStatus()     { return status; }
    public LocalDateTime getBorrowedAt(){ return borrowedAt; }
    public LocalDateTime getReturnedAt(){ return returnedAt; }

    public void markReturned() {
        this.status     = BorrowStatus.RETURNED;
        this.returnedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("BorrowRecord{book=%s, user=%s, status=%s}", bookId, userId, status);
    }
}
