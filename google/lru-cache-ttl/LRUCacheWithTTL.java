import java.util.*;

/**
 * LRU Cache extended with per-entry time-to-live (TTL) expiry.
 *
 * <p>Extends the standard doubly-linked-list + HashMap LRU design: each node additionally
 * stores an {@code expireAt} epoch-ms timestamp. Expired entries are evicted lazily on access
 * rather than by a background thread.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>Doubly-linked list with dummy head/tail sentinels — O(1) move-to-front and tail removal.</li>
 *   <li>{@code HashMap<key, Node>} — O(1) lookup.</li>
 * </ul>
 *
 * <p>Core invariants:
 * <ul>
 *   <li>Head-side of the list = most recently used; tail-side = least recently used.</li>
 *   <li>On capacity overflow the LRU tail is evicted regardless of whether it is expired.</li>
 *   <li>An expired entry found during {@link #get} is silently removed and {@code -1} returned.</li>
 * </ul>
 *
 * <p>Thread safety: Not thread-safe.
 */
public class LRUCacheWithTTL {

    /** A single cache entry in the doubly-linked list. */
    private static class Node {
        int  key, val;

        /** Epoch-ms at which this entry expires; {@link Long#MAX_VALUE} = no expiry. */
        long expireAt;

        Node prev, next;

        /**
         * @param key   cache key
         * @param val   cache value
         * @param ttlMs time-to-live in milliseconds; use {@link Long#MAX_VALUE} for no expiry
         */
        Node(int key, int val, long ttlMs) {
            this.key      = key;
            this.val      = val;
            this.expireAt = (ttlMs == Long.MAX_VALUE) ? Long.MAX_VALUE
                                                       : System.currentTimeMillis() + ttlMs;
        }
    }

    /** Maximum number of live entries this cache can hold. */
    private final int           capacity;

    /** Key-to-node index for O(1) lookup. */
    private final Map<Integer, Node> map = new HashMap<>();

    /** Dummy head sentinel (MRU side). */
    private final Node          head;

    /** Dummy tail sentinel (LRU side). */
    private final Node          tail;

    /**
     * Constructs an LRU cache with TTL support.
     *
     * @param capacity maximum number of live entries (must be &gt; 0)
     * @throws IllegalArgumentException if capacity is not positive
     */
    public LRUCacheWithTTL(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        head = new Node(0, 0, Long.MAX_VALUE);
        tail = new Node(0, 0, Long.MAX_VALUE);
        head.next = tail;
        tail.prev = head;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the value for {@code key}, or {@code -1} if absent or expired.
     * Moves the entry to the MRU position on a successful (non-expired) hit.
     *
     * @param key the cache key
     * @return the associated value, or {@code -1}
     */
    public int get(int key) {
        Node n = map.get(key);
        if (n == null) return -1;
        if (isExpired(n)) { evict(n); return -1; }
        moveToFront(n);
        return n.val;
    }

    /**
     * Inserts or updates the entry. On a re-put the TTL is refreshed to {@code ttlMs} from now.
     * If the cache is at capacity, the LRU tail entry is evicted before inserting.
     *
     * @param key   the cache key
     * @param val   the value to store
     * @param ttlMs time-to-live in milliseconds
     */
    public void put(int key, int val, long ttlMs) {
        if (map.containsKey(key)) {
            Node n = map.get(key);
            n.val      = val;
            n.expireAt = System.currentTimeMillis() + ttlMs;
            moveToFront(n);
        } else {
            if (map.size() == capacity) {
                // Evict LRU (tail.prev) — may or may not be expired; doesn't matter
                evict(tail.prev);
            }
            Node n = new Node(key, val, ttlMs);
            map.put(key, n);
            addToFront(n);
        }
    }

    /**
     * Scans the entire list and evicts all expired entries.
     * Use this to proactively reclaim capacity; otherwise stale entries persist
     * until they are accessed (lazy expiry).
     */
    public void cleanup() {
        Node curr = head.next;
        while (curr != tail) {
            Node next = curr.next;
            if (isExpired(curr)) evict(curr);
            curr = next;
        }
    }

    /** Returns the number of entries currently in the cache (including any unexpired stale ones). */
    public int size() { return map.size(); }

    // ── DLL helpers ───────────────────────────────────────────────────────────

    /** Inserts {@code n} immediately after the head sentinel (MRU position). */
    private void addToFront(Node n) {
        n.next       = head.next;
        n.prev       = head;
        head.next.prev = n;
        head.next    = n;
    }

    /** Unlinks {@code n} from the doubly-linked list. */
    private void remove(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    /** Moves {@code n} to the MRU position. */
    private void moveToFront(Node n) { remove(n); addToFront(n); }

    /** Removes {@code n} from the list and the map. */
    private void evict(Node n) { remove(n); map.remove(n.key); }

    /** Returns {@code true} if the entry's TTL has elapsed. */
    private boolean isExpired(Node n) {
        return System.currentTimeMillis() > n.expireAt;
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Basic LRU + TTL behaviour               ");
        System.out.println("══════════════════════════════════════════");
        LRUCacheWithTTL cache = new LRUCacheWithTTL(3);

        cache.put(1, 100, 500);   // expires in 500ms
        cache.put(2, 200, 2000);  // expires in 2s
        cache.put(3, 300, 1000);  // expires in 1s

        System.out.println("get(1) = " + cache.get(1)); // 100
        System.out.println("get(2) = " + cache.get(2)); // 200
        System.out.println("get(3) = " + cache.get(3)); // 300

        Thread.sleep(600); // key 1 now expired

        System.out.println("\nAfter 600ms (key 1 should be expired):");
        System.out.println("get(1) = " + cache.get(1)); // -1 (expired)
        System.out.println("get(2) = " + cache.get(2)); // 200 (still valid)
        System.out.println("get(3) = " + cache.get(3)); // 300 (still valid)

        Thread.sleep(500); // key 3 now expired

        System.out.println("\nAfter 1100ms total (keys 1,3 expired):");
        System.out.println("get(3) = " + cache.get(3)); // -1 (expired)
        System.out.println("get(2) = " + cache.get(2)); // 200 (still valid)

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" LRU eviction (capacity = 2)              ");
        System.out.println("══════════════════════════════════════════");
        LRUCacheWithTTL small = new LRUCacheWithTTL(2);

        small.put(1, 10, 60_000);
        small.put(2, 20, 60_000);
        small.get(1);             // touch 1 → 2 is now LRU
        small.put(3, 30, 60_000); // evicts 2 (LRU), even though it hasn't expired

        System.out.println("get(1) = " + small.get(1)); // 10
        System.out.println("get(2) = " + small.get(2)); // -1 (evicted)
        System.out.println("get(3) = " + small.get(3)); // 30

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" TTL update on re-put                     ");
        System.out.println("══════════════════════════════════════════");
        LRUCacheWithTTL c2 = new LRUCacheWithTTL(2);
        c2.put(1, 10, 300);
        Thread.sleep(200);
        c2.put(1, 99, 1000); // refresh TTL
        Thread.sleep(200);   // original TTL would have expired by now
        System.out.println("get(1) = " + c2.get(1)); // 99 (refreshed TTL, still alive)
    }
}
