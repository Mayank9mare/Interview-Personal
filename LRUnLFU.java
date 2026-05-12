import java.util.*;

public class LRUnLFU {

    // ═════════════════════════════════════════════════════════════════════════
    // LRU Cache — Least Recently Used
    //   Evict the key that was accessed LEAST RECENTLY.
    //   O(1) get and put.
    //
    //   Data structures:
    //     HashMap<key, Node>   — O(1) lookup by key
    //     Doubly Linked List   — O(1) move-to-tail and remove
    //       head ↔ [oldest … newest] ↔ tail
    //       Evict from head.next; insert/promote to tail.prev.
    // ═════════════════════════════════════════════════════════════════════════
    static class LRUCache {
        private static class Node {
            int key, val;
            Node prev, next;
            Node(int k, int v) { key = k; val = v; }
        }

        private final int capacity;
        private final Map<Integer, Node> map;
        private final Node head, tail; // dummy sentinels — no null checks needed

        LRUCache(int capacity) {
            this.capacity = capacity;
            map  = new HashMap<>();
            head = new Node(0, 0);   // oldest side
            tail = new Node(0, 0);   // newest side
            head.next = tail;
            tail.prev = head;
        }

        public int get(int key) {
            if (!map.containsKey(key)) return -1;
            Node node = map.get(key);
            remove(node);
            addToTail(node);   // promote to most-recently-used
            return node.val;
        }

        public void put(int key, int value) {
            if (map.containsKey(key)) {
                Node node = map.get(key);
                node.val = value;
                remove(node);
                addToTail(node);
            } else {
                if (map.size() == capacity) {
                    Node lru = head.next;  // oldest
                    remove(lru);
                    map.remove(lru.key);
                }
                Node node = new Node(key, value);
                map.put(key, node);
                addToTail(node);
            }
        }

        private void remove(Node n) {
            n.prev.next = n.next;
            n.next.prev = n.prev;
        }

        private void addToTail(Node n) {
            n.prev = tail.prev;
            n.next = tail;
            tail.prev.next = n;
            tail.prev = n;
        }

        @Override public String toString() {
            StringBuilder sb = new StringBuilder("LRU[");
            for (Node n = head.next; n != tail; n = n.next) {
                sb.append(n.key).append("=").append(n.val);
                if (n.next != tail) sb.append(", ");
            }
            return sb.append("] (oldest→newest)").toString();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LRU — Option B: LinkedHashMap shortcut
    //   accessOrder=true makes get() move the entry to the tail automatically.
    //   removeEldestEntry() is called after every put() — evict when over capacity.
    //   One-liner for interviews when the DLL isn't explicitly required.
    // ─────────────────────────────────────────────────────────────────────────
    static class LRUSimple<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;
        LRUSimple(int cap) { super(cap, 0.75f, true); capacity = cap; }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LFU Cache — Least Frequently Used
    //   Evict the key with LOWEST access count.
    //   Tie-break: evict the LEAST RECENTLY USED among min-freq keys.
    //   O(1) get and put.
    //
    //   Data structures:
    //     keyVal  : key → value
    //     keyFreq : key → frequency
    //     freqKeys: freq → LinkedHashSet<key>   ← insertion order = LRU within same freq
    //     minFreq : current minimum frequency   ← updated on every operation
    //
    //   Why LinkedHashSet?
    //     Set (no dups) + O(1) add/remove + iteration gives us the oldest key at front.
    // ═════════════════════════════════════════════════════════════════════════
    static class LFUCache {
        private final int capacity;
        private int minFreq;
        private final Map<Integer, Integer> keyVal;
        private final Map<Integer, Integer> keyFreq;
        private final Map<Integer, LinkedHashSet<Integer>> freqKeys;

        LFUCache(int capacity) {
            this.capacity = capacity;
            minFreq  = 0;
            keyVal   = new HashMap<>();
            keyFreq  = new HashMap<>();
            freqKeys = new HashMap<>();
        }

        public int get(int key) {
            if (!keyVal.containsKey(key)) return -1;
            promote(key);
            return keyVal.get(key);
        }

        public void put(int key, int value) {
            if (capacity <= 0) return;
            if (keyVal.containsKey(key)) {
                keyVal.put(key, value);
                promote(key);
                return;
            }
            if (keyVal.size() == capacity) evict();
            keyVal.put(key, value);
            keyFreq.put(key, 1);
            freqKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
            minFreq = 1; // new key always starts at freq=1
        }

        // Move key from its current bucket to the next frequency bucket
        private void promote(int key) {
            int freq = keyFreq.get(key);
            keyFreq.put(key, freq + 1);
            LinkedHashSet<Integer> bucket = freqKeys.get(freq);
            bucket.remove(key);
            if (bucket.isEmpty()) {
                freqKeys.remove(freq);
                if (minFreq == freq) minFreq++; // minFreq can only increase by 1
            }
            freqKeys.computeIfAbsent(freq + 1, k -> new LinkedHashSet<>()).add(key);
        }

        // Evict the LRU key among keys with minFreq
        private void evict() {
            LinkedHashSet<Integer> bucket = freqKeys.get(minFreq);
            int evict = bucket.iterator().next(); // oldest in this bucket
            bucket.remove(evict);
            if (bucket.isEmpty()) freqKeys.remove(minFreq);
            keyVal.remove(evict);
            keyFreq.remove(evict);
        }

        @Override public String toString() {
            return "LFU{vals=" + keyVal + ", freqs=" + keyFreq + ", minFreq=" + minFreq + "}";
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LFU — Alternative using a min-heap
    //   Simpler code but O(log n) per operation (heap rebalancing).
    //   Useful when O(log n) is acceptable and simplicity matters.
    // ═════════════════════════════════════════════════════════════════════════
    static class LFUCacheHeap {
        private static class Entry {
            int key, val, freq, order;
            Entry(int k, int v, int f, int o) { key=k; val=v; freq=f; order=o; }
        }

        private final int capacity;
        private int clock; // global insertion order for LRU tie-break
        private final Map<Integer, Entry> map;
        // min-heap: sort by (freq, order) → lowest freq + oldest entry at top
        private final PriorityQueue<Entry> pq;

        LFUCacheHeap(int capacity) {
            this.capacity = capacity;
            map = new HashMap<>();
            pq  = new PriorityQueue<>(Comparator.comparingInt((Entry e) -> e.freq)
                                                 .thenComparingInt(e -> e.order));
        }

        public int get(int key) {
            if (!map.containsKey(key)) return -1;
            Entry e = map.get(key);
            pq.remove(e); // O(n) removal — this is why it's O(log n) amortized
            e.freq++; e.order = clock++;
            pq.offer(e);
            return e.val;
        }

        public void put(int key, int value) {
            if (capacity <= 0) return;
            if (map.containsKey(key)) {
                Entry e = map.get(key);
                pq.remove(e);
                e.val = value; e.freq++; e.order = clock++;
                pq.offer(e);
            } else {
                if (map.size() == capacity) {
                    Entry evict = pq.poll();
                    map.remove(evict.key);
                }
                Entry e = new Entry(key, value, 1, clock++);
                map.put(key, e);
                pq.offer(e);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Quick comparison:
    //
    //          │  Eviction policy                │ get  │ put
    //  ────────┼─────────────────────────────────┼──────┼──────
    //  LRU     │ Least recently accessed          │ O(1) │ O(1)
    //  LFU     │ Least frequently accessed (LRU   │ O(1) │ O(1)
    //          │ tie-break with LinkedHashSet)    │      │
    //  LFU heap│ Same policy                      │ O(log n) │ O(log n)
    //  FIFO    │ First inserted                   │ O(1) │ O(1)  (Queue + Map)
    //  MRU     │ Most recently used (media streaming) │ reverse LRU │
    //
    //  LRU use cases : web caches, CDN, CPU page cache
    //  LFU use cases : when popularity matters (hot items stay cached longer)
    //                  e.g. database buffer pool, DNS cache
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("=== LRU Cache (capacity=3) ===");
        LRUCache lru = new LRUCache(3);
        lru.put(1, 10); System.out.println("put(1,10) → " + lru);
        lru.put(2, 20); System.out.println("put(2,20) → " + lru);
        lru.put(3, 30); System.out.println("put(3,30) → " + lru);
        System.out.println("get(1)=" + lru.get(1) + " → " + lru); // 1 moves to newest
        lru.put(4, 40); System.out.println("put(4,40) → " + lru); // evicts 2 (oldest)
        System.out.println("get(2)=" + lru.get(2)); // -1  (evicted)
        System.out.println("get(3)=" + lru.get(3)); //  30

        System.out.println("\n=== LRU Simple (LinkedHashMap, capacity=3) ===");
        LRUSimple<Integer, Integer> simple = new LRUSimple<>(3);
        simple.put(1, 10); simple.put(2, 20); simple.put(3, 30);
        simple.get(1); // touch 1 → moves to newest
        simple.put(4, 40); // evicts 2 (oldest after get(1))
        System.out.println("After eviction: " + simple); // {3=30, 1=10, 4=40}

        System.out.println("\n=== LFU Cache (capacity=3) ===");
        LFUCache lfu = new LFUCache(3);
        lfu.put(1, 10); lfu.put(2, 20); lfu.put(3, 30);
        System.out.println("Initial: " + lfu);
        lfu.get(1); lfu.get(1); // key 1 freq=3
        lfu.get(2);              // key 2 freq=2
        System.out.println("After accesses: " + lfu);
        lfu.put(4, 40); // key 3 has minFreq=1 → evict 3
        System.out.println("After put(4): " + lfu);
        System.out.println("get(3)=" + lfu.get(3)); // -1  (evicted)
        System.out.println("get(1)=" + lfu.get(1)); //  10

        System.out.println("\n=== LFU Tie-break (LRU among same freq) ===");
        LFUCache lfu2 = new LFUCache(2);
        lfu2.put(1, 1); lfu2.put(2, 2);
        lfu2.get(1);             // key 1 freq=2, key 2 freq=1
        lfu2.put(3, 3);          // both 2 and 3 would have freq=1; 2 is older → evict 2
        System.out.println("get(2)=" + lfu2.get(2)); // -1  (evicted)
        System.out.println("get(1)=" + lfu2.get(1)); //  1
        System.out.println("get(3)=" + lfu2.get(3)); //  3

        System.out.println("\n=== LFU Heap variant ===");
        LFUCacheHeap lfuH = new LFUCacheHeap(2);
        lfuH.put(1, 1); lfuH.put(2, 2);
        lfuH.get(1);
        lfuH.put(3, 3);
        System.out.println("get(2)=" + lfuH.get(2)); // -1
        System.out.println("get(1)=" + lfuH.get(1)); //  1
        System.out.println("get(3)=" + lfuH.get(3)); //  3
    }
}
