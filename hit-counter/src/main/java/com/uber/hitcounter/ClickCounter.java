// Companies: Google, Amazon, Adobe
package com.uber.hitcounter;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClickCounter {
    // Store as [timestamp, count] pairs
    private final Deque<long[]> window = new ArrayDeque<>();

    public void recordClick(int timestamp) {
        if (!window.isEmpty() && window.peekLast()[0] == timestamp) {
            window.peekLast()[1]++;
        } else {
            window.addLast(new long[]{timestamp, 1});
        }
    }

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
