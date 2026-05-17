// Companies: Google, Amazon, Adobe
package com.uber.hitcounter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Click counter that tracks the total number of clicks within a rolling 300-second window.
 * <p>
 * Consecutive clicks at the same timestamp are coalesced into a single {@code [timestamp, count]}
 * entry in the deque to reduce memory usage. The window is evaluated lazily on each query
 * rather than actively evicting stale entries; this keeps {@link #recordClick} O(1).
 * Not thread-safe.
 */
public class ClickCounter {
    /** Ordered deque of {@code [timestamp, count]} pairs; tail is the most recent entry. */
    private final Deque<long[]> window = new ArrayDeque<>();

    /**
     * Records a single click at the given timestamp. Consecutive clicks at the same timestamp
     * are merged into the existing tail entry.
     *
     * @param timestamp the time of the click (must be non-decreasing across calls)
     */
    public void recordClick(int timestamp) {
        if (!window.isEmpty() && window.peekLast()[0] == timestamp) {
            window.peekLast()[1]++;
        } else {
            window.addLast(new long[]{timestamp, 1});
        }
    }

    /**
     * Returns the total number of clicks that occurred in the half-open interval
     * {@code (timestamp - 300, timestamp]}.
     *
     * @param timestamp the current time used as the right boundary of the window
     * @return total click count within the 300-second window
     */
    public int getRecentClicks(int timestamp) {
        int total = 0;
        for (long[] entry : window) {
            if (entry[0] > timestamp - 300 && entry[0] <= timestamp) {
                total += entry[1];
            }
        }
        return total;
    }
}
