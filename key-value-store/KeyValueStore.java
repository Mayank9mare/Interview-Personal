import java.util.*;

public class KeyValueStore {

    // ═══════════════════════════════════════════════════════════════════════════
    // Key-Value Store with Nested Transactions
    //
    //   set(key, value)  — upsert
    //   get(key)         — returns value, or null if absent
    //   delete(key)      — remove key
    //   begin()          — start a transaction (nestable)
    //   commit()         — commit innermost transaction
    //   rollback()       — undo all changes made in innermost transaction
    //
    // Strategy: Undo-log
    //   All writes go directly to the main store — reads are always O(1).
    //   Each active transaction maintains an undo log: key → pre-image value.
    //   Only the first write to a key per transaction level is recorded
    //   (subsequent writes to the same key within the same transaction don't
    //   change what we need to restore).
    //
    //   commit()   → discard the undo log (changes stay in store). O(1).
    //   rollback() → replay undo log in reverse, restoring each key. O(touched keys).
    //
    //   Nesting: a stack of undo logs supports arbitrarily deep transactions.
    //   Rolling back the inner txn restores the state to just after begin() of
    //   the outer txn; the outer txn's undo log is unaffected.
    //
    // Complexity:
    //   get             O(1)
    //   set / delete    O(1) + O(1) undo-log write (first touch per key per level)
    //   commit          O(1)
    //   rollback        O(k)   k = distinct keys touched in that transaction
    // ═══════════════════════════════════════════════════════════════════════════
    static class KVStore {

        // Sentinel: stored in undo-log when a key did NOT exist before this txn.
        // Rollback restores it by calling store.remove(key).
        private static final Object ABSENT = new Object();

        private final Map<String, String> store = new HashMap<>();

        // Each frame: key → value this key had BEFORE this transaction touched it.
        // LinkedHashMap preserves insertion order for deterministic rollback replay.
        private final Deque<Map<String, Object>> txnStack = new ArrayDeque<>();

        // ── Core operations ──────────────────────────────────────────────────

        public void set(String key, String value) {
            saveUndo(key);
            store.put(key, value);
        }

        public String get(String key) {
            return store.get(key);  // null if absent
        }

        public void delete(String key) {
            saveUndo(key);
            store.remove(key);
        }

        // ── Transaction control ───────────────────────────────────────────────

        public void begin() {
            txnStack.push(new LinkedHashMap<>());
        }

        public void commit() {
            if (txnStack.isEmpty()) throw new IllegalStateException("No active transaction");
            txnStack.pop();   // changes already in store — nothing else to do
        }

        public void rollback() {
            if (txnStack.isEmpty()) throw new IllegalStateException("No active transaction");
            Map<String, Object> undo = txnStack.pop();

            // Restore in reverse insertion order so compound operations unwind cleanly
            List<Map.Entry<String, Object>> entries = new ArrayList<>(undo.entrySet());
            for (int i = entries.size() - 1; i >= 0; i--) {
                String key  = entries.get(i).getKey();
                Object prev = entries.get(i).getValue();
                if (prev == ABSENT) store.remove(key);
                else                store.put(key, (String) prev);
            }
        }

        public boolean inTransaction() { return !txnStack.isEmpty(); }

        // ── Internal ─────────────────────────────────────────────────────────

        // Record key's pre-image into the current transaction's undo log.
        // Only the first write per key per txn level matters.
        private void saveUndo(String key) {
            if (txnStack.isEmpty()) return;
            Map<String, Object> frame = txnStack.peek();
            if (!frame.containsKey(key)) {
                frame.put(key, store.containsKey(key) ? store.get(key) : ABSENT);
            }
        }

        @Override public String toString() { return store.toString(); }
    }

    public static void main(String[] args) {
        KVStore kv = new KVStore();

        System.out.println("=== Basic operations (no transaction) ===");
        kv.set("a", "1");
        kv.set("b", "2");
        System.out.println("get(a)=" + kv.get("a"));   // 1
        System.out.println("get(b)=" + kv.get("b"));   // 2
        kv.delete("b");
        System.out.println("get(b)=" + kv.get("b"));   // null
        System.out.println("store: " + kv);             // {a=1}

        System.out.println("\n=== Commit ===");
        kv.begin();
        kv.set("a", "100");
        kv.set("c", "3");
        System.out.println("inside txn: " + kv);        // {a=100, c=3}
        kv.commit();
        System.out.println("after commit: " + kv);      // {a=100, c=3}  (changes kept)

        System.out.println("\n=== Rollback ===");
        kv.begin();
        kv.set("a", "999");
        kv.set("d", "4");
        kv.delete("c");
        System.out.println("inside txn: " + kv);        // {a=999, d=4}
        kv.rollback();
        System.out.println("after rollback: " + kv);    // {a=100, c=3}  (fully restored)

        System.out.println("\n=== Nested transactions ===");
        kv.begin();               // outer txn
        kv.set("x", "10");

        kv.begin();               // inner txn
        kv.set("x", "20");
        kv.set("y", "30");
        System.out.println("inner view: " + kv);          // {a=100, c=3, x=20, y=30}
        kv.rollback();            // discard inner — x reverts to "10", y gone
        System.out.println("after inner rollback: " + kv); // {a=100, c=3, x=10}

        kv.set("z", "40");
        kv.commit();              // outer commit — x=10, z=40 persist
        System.out.println("after outer commit: " + kv);  // {a=100, c=3, x=10, z=40}

        System.out.println("\n=== Rollback of a delete ===");
        kv.begin();
        kv.delete("a");
        System.out.println("inside txn (a deleted): " + kv);
        kv.rollback();
        System.out.println("after rollback: " + kv);     // a=100 restored

        System.out.println("\n=== set same key twice in one txn ===");
        kv.begin();
        kv.set("a", "AAA");
        kv.set("a", "BBB");   // undo-log only recorded pre-image on the FIRST set
        System.out.println("inside: " + kv.get("a"));    // BBB
        kv.rollback();
        System.out.println("after rollback: " + kv.get("a")); // 100 (original pre-image)
    }
}
