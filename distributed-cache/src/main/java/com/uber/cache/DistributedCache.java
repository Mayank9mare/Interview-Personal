package com.uber.cache;

import java.time.Clock;
import java.util.*;

public class DistributedCache {
    private static class Entry {
        final String value;
        final long expiresAtMillis;

        Entry(String value, long expiresAtMillis) {
            this.value = value;
            this.expiresAtMillis = expiresAtMillis;
        }
    }

    private static class CacheNode {
        final String nodeId;
        final int capacity;
        final Clock clock;
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

        void put(String key, String value, long ttlMillis) {
            data.put(key, new Entry(value, clock.millis() + ttlMillis));
        }

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

    private final int virtualNodes;
    private final int nodeCapacity;
    private final Clock clock;
    private final TreeMap<Integer, CacheNode> ring = new TreeMap<>();
    private final Map<String, CacheNode> nodes = new HashMap<>();

    public DistributedCache(int virtualNodes, int nodeCapacity, Clock clock) {
        this.virtualNodes = virtualNodes;
        this.nodeCapacity = nodeCapacity;
        this.clock = clock;
    }

    public void addNode(String nodeId) {
        CacheNode node = new CacheNode(nodeId, nodeCapacity, clock);
        nodes.put(nodeId, node);
        for (int i = 0; i < virtualNodes; i++) {
            ring.put(hash(nodeId + "#" + i), node);
        }
    }

    public void removeNode(String nodeId) {
        nodes.remove(nodeId);
        ring.entrySet().removeIf(entry -> entry.getValue().nodeId.equals(nodeId));
    }

    public void put(String key, String value, long ttlMillis) {
        if (ttlMillis <= 0) throw new IllegalArgumentException("ttl must be positive");
        nodeFor(key).put(key, value, ttlMillis);
    }

    public String get(String key) {
        return nodeFor(key).get(key);
    }

    public String ownerOf(String key) {
        return nodeFor(key).nodeId;
    }

    private CacheNode nodeFor(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("no cache nodes");
        int h = hash(key);
        Map.Entry<Integer, CacheNode> entry = ring.ceilingEntry(h);
        return entry == null ? ring.firstEntry().getValue() : entry.getValue();
    }

    private int hash(String value) {
        return value.hashCode() & 0x7fffffff;
    }
}
