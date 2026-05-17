import java.util.*;
import java.util.concurrent.locks.*;

/**
 * Thread-safe tracker of modified memory blocks for incremental-backup use cases.
 *
 * <p>Each logical block is {@value #BLOCK_BYTES} bytes. Changed ranges are maintained in a
 * {@link TreeMap}{@code <start, end>} that is always kept in fully-merged form: no two stored
 * ranges overlap or are adjacent to one another.
 *
 * <p>Merge invariant: after every {@link #markChanged} call, the map contains the minimal
 * set of non-overlapping, non-adjacent ranges covering all blocks that have been marked.
 * Adjacent ranges ({@code [1,4]} + {@code [5,7]}) are merged into {@code [1,7]}.
 *
 * <p>Thread safety: {@link java.util.concurrent.locks.ReentrantReadWriteLock} — many concurrent
 * readers ({@link #getChangedRanges}, {@link #changedBlockCount}), exclusive writers
 * ({@link #markChanged}, {@link #reset}).
 */
public class MemoryBlockTracker {

    /** Size in bytes of each tracked block. */
    private static final int BLOCK_BYTES = 64;

    /**
     * Merged changed-range index: block-start → block-end (inclusive).
     * Invariant: no two entries overlap or are adjacent; maintained on every write.
     */
    private final TreeMap<Integer, Integer>  ranges  = new TreeMap<>();

    private final ReentrantReadWriteLock     rwLock  = new ReentrantReadWriteLock();
    private final Lock                       rLock   = rwLock.readLock();
    private final Lock                       wLock   = rwLock.writeLock();

    /**
     * Marks blocks {@code [start, end]} (inclusive) as changed, merging with any
     * overlapping or adjacent ranges already in the tracker.
     *
     * @param start first block in the range (must be &le; {@code end})
     * @param end   last block in the range
     * @throws IllegalArgumentException if {@code start > end}
     */
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

    /**
     * Returns a snapshot of all merged changed ranges as {@code [start, end]} pairs.
     * Multiple threads may call this concurrently.
     *
     * @return list of two-element arrays {@code {start, end}} in ascending start order
     */
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

    /** Clears all tracked changes. Subsequent {@link #getChangedRanges} returns an empty list. */
    public void reset() {
        wLock.lock();
        try { ranges.clear(); }
        finally { wLock.unlock(); }
    }

    /**
     * Returns the total number of changed blocks across all merged ranges.
     *
     * @return sum of {@code (end - start + 1)} over every range
     */
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

    /**
     * Prints all changed ranges with block numbers and corresponding byte offsets.
     *
     * @param label heading printed before the range list
     */
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
