package com.uber.cache;

import java.time.Clock;
import java.util.*;

/**
 * Distributed cache built on consistent hashing with virtual nodes and per-node LRU + TTL eviction.
 *
 * <p>Each logical node is represented by {@code virtualNodes} positions on the hash ring
 * (a {@link TreeMap} of hash → node). A key is routed to the node whose ring position is
 * the first one ≥ the key's hash (clockwise wrap-around if none exists).
 *
 * <p>Each {@link CacheNode} is an access-ordered {@link java.util.LinkedHashMap} that
 * evicts the least-recently-used entry when capacity is exceeded. TTL is checked lazily
 * on every {@code get}; expired entries are removed at that point.
 *
 * <p>Not thread-safe.
 */
public class DistributedCache {
    /** A single cached value with its expiry timestamp. */
    private static class Entry {
        final String value;
        /** Wall-clock milliseconds after which this entry is considered expired. */
        final long expiresAtMillis;

        Entry(String value, long expiresAtMillis) {
            this.value = value;
            this.expiresAtMillis = expiresAtMillis;
        }
    }

    /**
     * One physical cache node: access-ordered LRU map with lazy TTL expiry.
     */
    private static class CacheNode {
        /** Logical identifier for this node. */
        final String nodeId;
        /** Maximum number of entries before LRU eviction kicks in. */
        final int capacity;
        /** Clock used for TTL calculation; injectable for testing. */
        final Clock clock;
        /** Access-ordered map; eldest entry is evicted automatically when capacity is exceeded. */
        final LinkedHashMap<String, Entry> data;

        CacheNode(String nodeId, int capacity, Clock clock) {
            this.nodeId = nodeId;
            this.capacity = capacity;
            this.clock = clock;
            this.data = new LinkedHashMap<String, Entry>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                    return size() > CacheNode.this.capacity;
                }
            };
        }

        /** Stores a key-value pair with the given TTL, overwriting any existing entry. */
        void put(String key, String value, long ttlMillis) {
            data.put(key, new Entry(value, clock.millis() + ttlMillis));
        }

        /** Returns the value for {@code key}, or {@code null} if absent or expired (lazy eviction). */
        String get(String key) {
            Entry entry = data.get(key);
            if (entry == null) return null;
            if (entry.expiresAtMillis < clock.millis()) {
                data.remove(key);
                return null;
            }
            return entry.value;
        }
    }

    /** Number of virtual ring positions per physical node. */
    private final int virtualNodes;
    /** Maximum entries per node before LRU eviction. */
    private final int nodeCapacity;
    /** Shared clock injected for deterministic testing. */
    private final Clock clock;
    /** Consistent hash ring: hash value → owning node. */
    private final TreeMap<Integer, CacheNode> ring = new TreeMap<>();
    /** nodeId → CacheNode for O(1) removal by ID. */
    private final Map<String, CacheNode> nodes = new HashMap<>();

    /**
     * @param virtualNodes number of ring positions per physical node (higher = more uniform distribution)
     * @param nodeCapacity maximum LRU capacity per node
     * @param clock        clock used for TTL expiry (use {@link Clock#systemUTC()} in production)
     */
    public DistributedCache(int virtualNodes, int nodeCapacity, Clock clock) {
        this.virtualNodes = virtualNodes;
        this.nodeCapacity = nodeCapacity;
        this.clock = clock;
    }

    /**
     * Adds a physical node to the cluster, placing {@code virtualNodes} positions on the ring.
     *
     * @param nodeId unique node identifier
     */
    public void addNode(String nodeId) {
        CacheNode node = new CacheNode(nodeId, nodeCapacity, clock);
        nodes.put(nodeId, node);
        for (int i = 0; i < virtualNodes; i++) {
            ring.put(hash(nodeId + "#" + i), node);
        }
    }

    /**
     * Removes a node from the cluster. Existing data on that node is lost.
     *
     * @param nodeId the node to remove
     */
    public void removeNode(String nodeId) {
        nodes.remove(nodeId);
        ring.entrySet().removeIf(entry -> entry.getValue().nodeId.equals(nodeId));
    }

    /**
     * Stores a key-value pair on the responsible node.
     *
     * @param key       cache key
     * @param value     value to store
     * @param ttlMillis positive time-to-live in milliseconds
     * @throws IllegalArgumentException if {@code ttlMillis} is not positive
     * @throws IllegalStateException    if no nodes have been added
     */
    public void put(String key, String value, long ttlMillis) {
        if (ttlMillis <= 0) throw new IllegalArgumentException("ttl must be positive");
        nodeFor(key).put(key, value, ttlMillis);
    }

    /**
     * Retrieves a value from the cache.
     *
     * @param key cache key
     * @return the value, or {@code null} if not present or expired
     * @throws IllegalStateException if no nodes have been added
     */
    public String get(String key) {
        return nodeFor(key).get(key);
    }

    /**
     * Returns the node ID responsible for the given key according to the consistent hash ring.
     *
     * @param key cache key
     * @return node ID that owns the key
     * @throws IllegalStateException if no nodes have been added
     */
    public String ownerOf(String key) {
        return nodeFor(key).nodeId;
    }

    /** Locates the responsible node via clockwise ring lookup. */
    private CacheNode nodeFor(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("no cache nodes");
        int h = hash(key);
        Map.Entry<Integer, CacheNode> entry = ring.ceilingEntry(h);
        return entry == null ? ring.firstEntry().getValue() : entry.getValue();
    }

    /** Returns a non-negative hash of {@code value} suitable for ring placement. */
    private int hash(String value) {
        return value.hashCode() & 0x7fffffff;
    }
}
