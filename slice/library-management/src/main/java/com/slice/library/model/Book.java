package com.slice.library.model;

public class Book {

    private final String bookId;
    private final String bookName;
    private final String author;
    private volatile int count;

    public Book(String bookId, String bookName, String author, int count) {
        this.bookId   = bookId;
        this.bookName = bookName;
        this.author   = author;
        this.count    = count;
    }

    public String getBookId()   { return bookId; }
    public String getBookName() { return bookName; }
    public String getAuthor()   { return author; }
    public int    getCount()    { return count; }
    public void   setCount(int count) { this.count = count; }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s — copies: %d", bookId, bookName, author, count);
    }
}
