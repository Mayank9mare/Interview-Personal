package com.slice.library.exception;

public class LibraryException extends RuntimeException {

    private final String bookId;

    public LibraryException(String message, String bookId) {
        super(message + (bookId != null ? " [bookId=" + bookId + "]" : ""));
        this.bookId = bookId;
    }

    public LibraryException(String message) {
        this(message, null);
    }

    public String getBookId() { return bookId; }
}
