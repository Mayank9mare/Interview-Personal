import java.util.*;

public class LRUCacheWithTTL {

    // ═══════════════════════════════════════════════════════════════════════════
    // LRU Cache with Time-To-Live (TTL) — Google interview problem
    //
    // Extension of standard LRU: each entry carries an expiry timestamp.
    // An expired entry is treated as absent on read and removed lazily.
    //
    // API:
    //   put(key, value, ttlMs)  — insert/update; entry expires after ttlMs ms
    //   get(key)                — return value or -1 if missing or expired
    //   size()                  — count of live (non-expired) entries
    //
    // Design:
    //   • Doubly-linked list (DLL) + HashMap — identical to standard LRU.
    //   • Each Node additionally stores expireAt (epoch ms).
    //   • get(): if node exists but expired → remove from DLL + map, return -1.
    //   • put(): on capacity overflow, evict LRU tail even if not expired
    //     (tail is always the least-recently-used live entry).
    //   • Lazy expiry: no background thread. Expired entries only removed on access.
    //     Call cleanup() explicitly to purge all stale entries if needed.
    //
    // Trade-off: lazy expiry can let stale entries occupy capacity until accessed.
    // Alternative: background cleaner thread (add if asked as follow-up).
    //
    // Complexity: get/put O(1) amortized; cleanup O(n).
    // ═══════════════════════════════════════════════════════════════════════════

    private static class Node {
        int  key, val;
        long expireAt;   // System.currentTimeMillis() + ttlMs; Long.MAX_VALUE = no expiry
        Node prev, next;

        Node(int key, int val, long ttlMs) {
            this.key      = key;
            this.val      = val;
            this.expireAt = (ttlMs == Long.MAX_VALUE) ? Long.MAX_VALUE
                                                       : System.currentTimeMillis() + ttlMs;
        }
    }

    private final int           capacity;
    private final Map<Integer, Node> map = new HashMap<>();
    private final Node          head, tail;   // dummy sentinels

    public LRUCacheWithTTL(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        head = new Node(0, 0, Long.MAX_VALUE);
        tail = new Node(0, 0, Long.MAX_VALUE);
        head.next = tail;
        tail.prev = head;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public int get(int key) {
        Node n = map.get(key);
        if (n == null) return -1;
        if (isExpired(n)) { evict(n); return -1; }
        moveToFront(n);
        return n.val;
    }

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

    // Purge all expired entries (call this to reclaim capacity proactively)
    public void cleanup() {
        Node curr = head.next;
        while (curr != tail) {
            Node next = curr.next;
            if (isExpired(curr)) evict(curr);
            curr = next;
        }
    }

    public int size() { return map.size(); }

    // ── DLL helpers ───────────────────────────────────────────────────────────

    private void addToFront(Node n) {
        n.next       = head.next;
        n.prev       = head;
        head.next.prev = n;
        head.next    = n;
    }

    private void remove(Node n) {
        n.prev.next = n.next;
        n.next.prev = n.prev;
    }

    private void moveToFront(Node n) { remove(n); addToFront(n); }

    private void evict(Node n) { remove(n); map.remove(n.key); }

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
