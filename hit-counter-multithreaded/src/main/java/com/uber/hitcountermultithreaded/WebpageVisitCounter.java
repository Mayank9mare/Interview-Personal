// Companies: Amazon, Adobe, Atlassian, Uber, DoorDash
package com.uber.hitcountermultithreaded;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class WebpageVisitCounter {
    private final AtomicIntegerArray counts;

    public WebpageVisitCounter(int totalPages) {
        counts = new AtomicIntegerArray(totalPages);
    }

    public void incrementVisitCount(int pageIndex) {
        counts.incrementAndGet(pageIndex);
    }

    public int getVisitCount(int pageIndex) {
        return counts.get(pageIndex);
    }
}
