import java.util.*;
import java.util.concurrent.locks.*;

public class MemoryBlockTracker {

    // ═══════════════════════════════════════════════════════════════════════════
    // Memory Block Tracker
    //
    // Problem: A storage system tracks which memory blocks have been modified
    // (e.g., for incremental backup). Each block = 64 bytes. Multiple threads
    // concurrently mark ranges as changed. At snapshot time, return the minimal
    // set of merged, non-overlapping changed ranges.
    //
    // API:
    //   markChanged(start, end)  — mark blocks [start..end] (inclusive) changed
    //   getChangedRanges()       — returns list of merged [start, end] ranges
    //   reset()                  — clear all tracked changes
    //
    // Design:
    //   • TreeMap<Integer,Integer>: start → end, always maintained in merged form.
    //   • On markChanged(s, e): walk the map to absorb all overlapping or
    //     adjacent ranges into one, then insert the merged range.
    //     "Adjacent" means [1,4] + [5,7] → [1,7] (no gap between blocks).
    //   • ReentrantReadWriteLock: multiple concurrent readers, exclusive writers.
    //
    // Merge invariant: after every write the map has no overlapping/adjacent ranges.
    // Complexity: markChanged O(k log n) where k = ranges absorbed; typically O(log n).
    //             getChangedRanges O(n).
    // ═══════════════════════════════════════════════════════════════════════════

    private static final int BLOCK_BYTES = 64;

    private final TreeMap<Integer, Integer>  ranges  = new TreeMap<>();
    private final ReentrantReadWriteLock     rwLock  = new ReentrantReadWriteLock();
    private final Lock                       rLock   = rwLock.readLock();
    private final Lock                       wLock   = rwLock.writeLock();

    // Mark blocks [start, end] (inclusive) as changed.
    // Merges with any overlapping or adjacent existing ranges.
    public void markChanged(int start, int end) {
        if (start > end) throw new IllegalArgumentException("start must be <= end");
        wLock.lock();
        try {
            // Expand our window to absorb all overlapping or adjacent ranges.
            // A range [lo, hi] overlaps/is-adjacent to [start-1, end+1].
            Integer lo = ranges.floorKey(end + 1);
            while (lo != null && ranges.get(lo) >= start - 1) {
                start = Math.min(start, lo);
                end   = Math.max(end, ranges.get(lo));
                ranges.remove(lo);
                lo = ranges.floorKey(end + 1);
            }
            ranges.put(start, end);
        } finally {
            wLock.unlock();
        }
    }

    // Returns a snapshot of all merged changed ranges as [start, end] pairs.
    public List<int[]> getChangedRanges() {
        rLock.lock();
        try {
            List<int[]> result = new ArrayList<>(ranges.size());
            for (Map.Entry<Integer, Integer> e : ranges.entrySet())
                result.add(new int[]{e.getKey(), e.getValue()});
            return result;
        } finally {
            rLock.unlock();
        }
    }

    public void reset() {
        wLock.lock();
        try { ranges.clear(); }
        finally { wLock.unlock(); }
    }

    // Total number of changed blocks across all ranges
    public long changedBlockCount() {
        rLock.lock();
        try {
            long total = 0;
            for (Map.Entry<Integer, Integer> e : ranges.entrySet())
                total += (e.getValue() - e.getKey() + 1);
            return total;
        } finally {
            rLock.unlock();
        }
    }

    public void printRanges(String label) {
        List<int[]> list = getChangedRanges();
        System.out.println(label);
        if (list.isEmpty()) { System.out.println("  (none)"); return; }
        for (int[] r : list) {
            System.out.printf("  blocks [%3d, %3d]  →  bytes [0x%06X, 0x%06X)%n",
                r[0], r[1],
                (long) r[0] * BLOCK_BYTES,
                (long)(r[1] + 1) * BLOCK_BYTES);
        }
        System.out.printf("  Total changed blocks: %d (%d bytes)%n",
            changedBlockCount(), changedBlockCount() * BLOCK_BYTES);
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Example 1: Basic adjacent merging        ");
        System.out.println("══════════════════════════════════════════");
        MemoryBlockTracker t1 = new MemoryBlockTracker();
        t1.markChanged(1,  2);
        t1.markChanged(3,  4);   // adjacent → merges with [1,2]
        t1.markChanged(6,  9);
        t1.markChanged(10, 12);  // adjacent → merges with [6,9]
        t1.printRanges("After marking [1,2], [3,4], [6,9], [10,12]:");
        // Expected: [1,4] and [6,12]

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 2: Overlapping ranges            ");
        System.out.println("══════════════════════════════════════════");
        MemoryBlockTracker t2 = new MemoryBlockTracker();
        t2.markChanged(5,  10);
        t2.markChanged(8,  15);  // overlaps with [5,10]
        t2.markChanged(1,   3);
        t2.markChanged(2,   6);  // overlaps [1,3] and [5,15]
        t2.printRanges("After marking [5,10], [8,15], [1,3], [2,6]:");
        // Expected: [1,15]

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 3: Concurrent writers            ");
        System.out.println("══════════════════════════════════════════");
        MemoryBlockTracker t3 = new MemoryBlockTracker();
        int THREADS = 8;
        Thread[] threads = new Thread[THREADS];
        for (int i = 0; i < THREADS; i++) {
            final int base = i * 20;
            threads[i] = new Thread(() -> {
                t3.markChanged(base,     base + 10);
                t3.markChanged(base + 8, base + 18); // overlaps previous
            }, "writer-" + i);
        }
        for (Thread th : threads) th.start();
        for (Thread th : threads) th.join();
        t3.printRanges("After " + THREADS + " concurrent writers (each writing 2 overlapping ranges):");

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Example 4: Single block, gap, then fill  ");
        System.out.println("══════════════════════════════════════════");
        MemoryBlockTracker t4 = new MemoryBlockTracker();
        t4.markChanged(0,  0);
        t4.markChanged(5,  5);
        t4.markChanged(10, 10);
        t4.printRanges("Three isolated blocks [0],[5],[10]:");
        t4.markChanged(1, 9);   // fill the gap
        t4.printRanges("After filling [1,9] — should be [0,10]:");
    }
}
