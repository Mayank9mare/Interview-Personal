// Companies: Amazon, Adobe, Atlassian, Uber, DoorDash
package com.uber.hitcountermultithreaded;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Thread-safe per-page visit counter backed by an {@link AtomicIntegerArray}.
 * <p>
 * Each element of the array corresponds to a page identified by its zero-based index.
 * All operations are lock-free and safe for concurrent access by multiple threads.
 */
public class WebpageVisitCounter {
    /** Atomic array holding the visit count for each page, indexed by page index. */
    private final AtomicIntegerArray counts;

    /**
     * @param totalPages total number of pages to track (indices {@code 0} to {@code totalPages - 1})
     */
    public WebpageVisitCounter(int totalPages) {
        counts = new AtomicIntegerArray(totalPages);
    }

    /**
     * Atomically increments the visit count for the given page.
     *
     * @param pageIndex zero-based page index
     */
    public void incrementVisitCount(int pageIndex) {
        counts.incrementAndGet(pageIndex);
    }

    /**
     * Returns the current visit count for the given page.
     *
     * @param pageIndex zero-based page index
     * @return visit count for that page
     */
    public int getVisitCount(int pageIndex) {
        return counts.get(pageIndex);
    }
}
