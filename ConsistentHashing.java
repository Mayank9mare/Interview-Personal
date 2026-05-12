import java.util.*;
import java.security.*;

// Consistent Hashing — maps nodes and keys onto a virtual ring [0, 2^63)
// Key insight: when a node is added/removed, only keys on that node's segment
// get remapped — not the entire keyspace.
//
// Without virtual nodes: each physical node owns one arc → uneven distribution
// With virtual nodes: each physical node owns V arcs → smoother distribution;
//   also means node removal redistributes load across ALL remaining nodes (not just one)
public class ConsistentHashing {

    private final TreeMap<Long, String> ring = new TreeMap<>();
    private final Map<String, List<Long>> nodePositions = new HashMap<>();
    private final int virtualNodes;

    ConsistentHashing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    // MD5-based hash → 64-bit long
    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());
            long h = 0;
            for (int i = 0; i < 8; i++) h = (h << 8) | (digest[i] & 0xFF);
            return h;
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    // Place node on ring at virtualNodes positions: hash("node#0"), hash("node#1"), …
    void addNode(String node) {
        List<Long> positions = new ArrayList<>();
        for (int i = 0; i < virtualNodes; i++) {
            long h = hash(node + "#" + i);
            ring.put(h, node);
            positions.add(h);
        }
        nodePositions.put(node, positions);
        System.out.println("Added " + node + " (" + virtualNodes + " vnodes)");
    }

    // Remove all virtual nodes for this physical node
    void removeNode(String node) {
        List<Long> positions = nodePositions.remove(node);
        if (positions != null) positions.forEach(ring::remove);
        System.out.println("Removed " + node);
    }

    // Walk clockwise from hash(key); wrap around if past last node
    String getNode(String key) {
        if (ring.isEmpty()) throw new IllegalStateException("No nodes");
        long h = hash(key);
        Map.Entry<Long, String> e = ring.ceilingEntry(h);
        return (e != null ? e : ring.firstEntry()).getValue();
    }

    // Return N distinct physical nodes for replication
    List<String> getReplicas(String key, int n) {
        if (ring.isEmpty()) throw new IllegalStateException("No nodes");
        long h = hash(key);
        List<String> replicas = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        // tail from key position, then wrap from beginning
        for (String node : ring.tailMap(h, true).values()) {
            if (seen.add(node) && replicas.size() < n) replicas.add(node);
            if (replicas.size() == n) return replicas;
        }
        for (String node : ring.values()) {
            if (seen.add(node) && replicas.size() < n) replicas.add(node);
            if (replicas.size() == n) return replicas;
        }
        return replicas;
    }

    // Show load distribution across all nodes for a set of keys
    void showDistribution(List<String> keys) {
        Map<String, Integer> counts = new TreeMap<>();
        for (String node : nodePositions.keySet()) counts.put(node, 0);
        for (String key : keys) counts.merge(getNode(key), 1, Integer::sum);
        System.out.println("  Distribution (" + keys.size() + " keys):");
        counts.forEach((node, count) -> {
            int pct = 100 * count / keys.size();
            System.out.printf("    %-12s %4d keys  %3d%%  %s%n",
                node, count, pct, "█".repeat(pct / 2));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendezvous (Highest Random Weight) Hashing
    //   Alternative to ring: for each key, each node gets score = hash(key+node);
    //   assign key to node with highest score.
    //   Simpler, no virtual nodes needed, equally balanced.
    //   Drawback: O(N) per lookup vs O(log N) for ring.
    // ─────────────────────────────────────────────────────────────────────────
    static class RendezvousHashing {
        private final List<String> nodes = new ArrayList<>();

        void addNode(String node) { nodes.add(node); }
        void removeNode(String node) { nodes.remove(node); }

        String getNode(String key) {
            return nodes.stream()
                .max(Comparator.comparingLong(n -> hashRendezvous(key + n)))
                .orElseThrow();
        }

        private long hashRendezvous(String s) {
            long h = 0;
            for (char c : s.toCharArray()) h = h * 31 + c;
            return h;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Jump Consistent Hash (Google 2014)
    //   Maps key to bucket 0..n-1 with uniform distribution
    //   O(log n) time, no extra memory, minimal remapping on resize
    //   Limitation: only supports adding buckets (not arbitrary removal)
    // ─────────────────────────────────────────────────────────────────────────
    static int jumpHash(long key, int numBuckets) {
        long k = key, b = -1, j = 0;
        while (j < numBuckets) {
            b = j;
            k = k * 2862933555777941757L + 1;
            j = (long)((b + 1) * (double)(1L << 31) / (double)((k >>> 33) + 1));
        }
        return (int) b;
    }

    public static void main(String[] args) {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 10_000; i++) keys.add("key:" + i);

        // ── 1 virtual node (uneven) ──────────────────────────────────────────
        System.out.println("\n=== 1 virtual node per server (uneven) ===");
        ConsistentHashing noVnodes = new ConsistentHashing(1);
        noVnodes.addNode("server-A");
        noVnodes.addNode("server-B");
        noVnodes.addNode("server-C");
        noVnodes.showDistribution(keys);

        // ── 150 virtual nodes (even) ─────────────────────────────────────────
        System.out.println("\n=== 150 virtual nodes per server (even) ===");
        ConsistentHashing ch = new ConsistentHashing(150);
        ch.addNode("server-A");
        ch.addNode("server-B");
        ch.addNode("server-C");
        ch.showDistribution(keys);

        // ── Key lookup ───────────────────────────────────────────────────────
        System.out.println("\nKey routing:");
        for (String k : List.of("user:alice", "user:bob", "order:123", "session:xyz"))
            System.out.println("  " + k + " → " + ch.getNode(k));

        // ── Replication ──────────────────────────────────────────────────────
        System.out.println("\nReplication (3 replicas for 'user:alice'):");
        System.out.println("  " + ch.getReplicas("user:alice", 3));

        // ── Add node — minimal remapping ─────────────────────────────────────
        System.out.println("\nAfter adding server-D:");
        ch.addNode("server-D");
        ch.showDistribution(keys);

        // ── Remove node — minimal remapping ──────────────────────────────────
        System.out.println("\nAfter removing server-B:");
        ch.removeNode("server-B");
        ch.showDistribution(keys);

        // ── Rendezvous hashing ───────────────────────────────────────────────
        System.out.println("\n=== Rendezvous (HRW) Hashing ===");
        RendezvousHashing rh = new RendezvousHashing();
        rh.addNode("server-A"); rh.addNode("server-B"); rh.addNode("server-C");
        System.out.println("user:alice → " + rh.getNode("user:alice"));
        System.out.println("order:123  → " + rh.getNode("order:123"));

        // ── Jump Hash ────────────────────────────────────────────────────────
        System.out.println("\n=== Jump Consistent Hash ===");
        for (long k : new long[]{0L, 1L, 42L, 1337L})
            System.out.println("  key=" + k + " → bucket " + jumpHash(k, 3));
    }
}
