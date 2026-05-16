// Companies: Amazon, Adobe, Atlassian
// Thread-safe hit counter — counts page hits within a rolling time window.

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Thread-safe hit counter that reports how many hits occurred within the last
 * {@code windowSeconds} seconds at any point in time.
 *
 * <p>Uses a circular buffer of size {@code windowSeconds} to store (timestamp, count)
 * buckets. Each second maps to one slot. This gives O(1) {@code hit} and {@code getHits}
 * regardless of the number of hits recorded.
 *
 * <p>Key design decisions:
 * <ul>
 *   <li><b>Circular buffer over TreeMap:</b> O(1) vs O(log n) per operation; memory is
 *       bounded to exactly {@code windowSeconds} slots.</li>
 *   <li><b>Stale-slot invalidation:</b> A slot is valid only if its stored timestamp
 *       matches the current second; otherwise it belongs to a previous cycle and is
 *       treated as 0.</li>
 *   <li><b>ReentrantReadWriteLock:</b> concurrent reads are allowed; only writes are
 *       exclusive. This avoids the blocked-read problem of a plain {@code synchronized}
 *       method when read traffic dominates.</li>
 * </ul>
 *
 * <p>Core invariant: for each slot {@code i}, {@code times[i]} holds the last timestamp
 * that wrote to it. If {@code times[i] != currentTime}, the slot is stale.
 *
 * <p>Thread safety: fully thread-safe via {@code ReentrantReadWriteLock}.
 */
public class HitCounter {

    /** Number of seconds in the rolling window (default 300 = 5 minutes). */
    private final int windowSeconds;

    /**
     * Circular buffer of hit counts. Slot {@code t % windowSeconds} stores hits for
     * second {@code t}. Stale when {@code times[t % windowSeconds] != t}.
     */
    private final int[] hits;

    /**
     * Stores the timestamp that last wrote to each slot.
     * Used to detect stale slots from a previous window cycle.
     */
    private final int[] times;

    /** Guards concurrent access — read lock for getHits, write lock for hit. */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Constructs a HitCounter with the default 300-second rolling window.
     */
    public HitCounter() {
        this(300);
    }

    /**
     * Constructs a HitCounter with a custom rolling window.
     *
     * @param windowSeconds length of the rolling window in seconds (must be > 0)
     */
    public HitCounter(int windowSeconds) {
        this.windowSeconds = windowSeconds;
        this.hits = new int[windowSeconds];
        this.times = new int[windowSeconds];
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Records one hit at the given timestamp.
     *
     * <p>If the slot for this timestamp is stale (belongs to a previous window cycle),
     * it is cleared before incrementing. Multiple hits at the same second accumulate.
     *
     * @param timestamp current time in seconds (must be non-decreasing across calls
     *                  from the same logical stream; concurrent callers may pass any
     *                  valid timestamp)
     */
    public void hit(int timestamp) {
        int slot = timestamp % windowSeconds;
        rwLock.writeLock().lock();
        try {
            if (times[slot] != timestamp) {
                // stale slot from a previous window cycle — reset it
                hits[slot] = 0;
                times[slot] = timestamp;
            }
            hits[slot]++;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Returns the total number of hits within the rolling window
     * {@code [timestamp - windowSeconds + 1, timestamp]}.
     *
     * <p>Iterates all slots and sums only those whose stored timestamp falls within
     * the current window. Stale slots contribute 0.
     *
     * @param timestamp the query time in seconds
     * @return hit count in the rolling window
     */
    public int getHits(int timestamp) {
        rwLock.readLock().lock();
        try {
            int total = 0;
            for (int i = 0; i < windowSeconds; i++) {
                // include slot only if its timestamp is within the current window
                if (timestamp - times[i] < windowSeconds) {
                    total += hits[i];
                }
            }
            return total;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        HitCounter counter = new HitCounter(300);

        counter.hit(1);
        counter.hit(1);
        counter.hit(300);
        System.out.println("getHits(300) = " + counter.getHits(300));
        // Expected: 3  (hits at t=1 and t=300 all within [1, 300])

        System.out.println("getHits(301) = " + counter.getHits(301));
        // Expected: 1  (hit at t=1 is now outside [2, 301]; only t=300 counts)

        counter.hit(302);
        System.out.println("getHits(302) = " + counter.getHits(302));
        // Expected: 2  (t=300 and t=302 are within [3, 302])

        // Concurrency smoke test: 10 threads each recording 100 hits at t=1
        HitCounter concurrent = new HitCounter(300);
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < 100; j++) concurrent.hit(1);
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("concurrent 10x100 hits at t=1: getHits(1) = " + concurrent.getHits(1));
        // Expected: 1000
    }
}
