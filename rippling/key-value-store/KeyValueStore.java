import java.util.*;

/**
 * Driver class for {@link KVStore}. Contains the demo {@link #main} and
 * houses {@code KVStore} as a static inner class for single-file compilation.
 */
public class KeyValueStore {

    /**
     * In-memory key-value store with nestable, ACID-style transactions.
     *
     * <p>Supports six operations:
     * <ul>
     *   <li>{@link #set} — upsert a key/value pair</li>
     *   <li>{@link #get} — point lookup (O(1), always reads committed + pending state)</li>
     *   <li>{@link #delete} — remove a key</li>
     *   <li>{@link #begin} — open a new (possibly nested) transaction</li>
     *   <li>{@link #commit} — make the innermost transaction's changes permanent</li>
     *   <li>{@link #rollback} — discard all changes made since the matching {@link #begin}</li>
     * </ul>
     *
     * <p><b>Undo-log strategy:</b> all writes go directly to the main store so reads
     * are always O(1). Each active transaction maintains an undo log mapping
     * key → pre-image value. Only the <em>first</em> write to a key per transaction
     * level is recorded; subsequent writes to the same key in the same transaction
     * do not change what needs to be restored on rollback.
     *
     * <p><b>Nesting:</b> {@code txnStack} holds one undo-log frame per open
     * transaction. Rolling back the innermost frame restores state to exactly after
     * the matching {@code begin()}; outer frames are unaffected.
     *
     * <p>Core data structures:
     * <ul>
     *   <li>{@code store}: {@code HashMap<key, value>} — the live key-value state.</li>
     *   <li>{@code txnStack}: stack of {@code LinkedHashMap<key, pre-image>} frames,
     *       one per open transaction. {@code LinkedHashMap} preserves insertion order
     *       for deterministic reverse-replay during rollback.</li>
     * </ul>
     *
     * <p>Complexity:
     * <ul>
     *   <li>{@code get}            — O(1)</li>
     *   <li>{@code set} / {@code delete} — O(1) amortised (O(1) undo-log write on first touch)</li>
     *   <li>{@code commit}         — O(1)</li>
     *   <li>{@code rollback}       — O(k), k = distinct keys touched in that transaction</li>
     * </ul>
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class KVStore {

        /**
         * Sentinel stored in the undo log when a key did <em>not</em> exist before
         * the current transaction touched it. On rollback, any entry whose pre-image
         * is {@code ABSENT} is removed from the store rather than restored to a value.
         */
        private static final Object ABSENT = new Object();

        /** The live key-value state shared across all transaction levels. */
        private final Map<String, String> store = new HashMap<>();

        /**
         * Stack of undo-log frames, one per open transaction (innermost on top).
         * Each frame maps key → the value that key held <em>before</em> this
         * transaction first wrote it ({@link #ABSENT} if the key was missing).
         */
        private final Deque<Map<String, Object>> txnStack = new ArrayDeque<>();

        // ── Core operations ──────────────────────────────────────────────────

        /**
         * Inserts or updates {@code key} with {@code value}.
         * If a transaction is active, records the pre-image before the first write
         * so the change can be undone by {@link #rollback}.
         *
         * @param key   the key to upsert (must not be null)
         * @param value the value to associate with the key (must not be null)
         */
        public void set(String key, String value) {
            saveUndo(key);
            store.put(key, value);
        }

        /**
         * Returns the current value associated with {@code key}, or {@code null}
         * if the key does not exist. Always reflects the latest uncommitted state
         * within an open transaction.
         *
         * @param key the key to look up
         * @return the associated value, or {@code null} if absent
         */
        public String get(String key) {
            return store.get(key);
        }

        /**
         * Removes {@code key} from the store. Has no effect if the key does not exist.
         * If a transaction is active, the removal is recorded in the undo log so
         * {@link #rollback} can restore the key.
         *
         * @param key the key to remove
         */
        public void delete(String key) {
            saveUndo(key);
            store.remove(key);
        }

        // ── Transaction control ───────────────────────────────────────────────

        /**
         * Opens a new transaction. Transactions may be nested; each {@code begin}
         * must be paired with exactly one {@link #commit} or {@link #rollback}.
         */
        public void begin() {
            txnStack.push(new LinkedHashMap<>());
        }

        /**
         * Commits the innermost open transaction, making all its changes permanent.
         * The undo log for this transaction is discarded; the changes remain in the
         * store and become part of the enclosing transaction (if any).
         *
         * @throws IllegalStateException if no transaction is currently open
         */
        public void commit() {
            if (txnStack.isEmpty()) throw new IllegalStateException("No active transaction");
            txnStack.pop();
        }

        /**
         * Rolls back the innermost open transaction, restoring all keys to the
         * values they held at the time of the matching {@link #begin}.
         * Keys created during this transaction are removed; keys deleted during
         * this transaction are restored. The enclosing transaction (if any) is
         * unaffected.
         *
         * <p>Replay is performed in reverse insertion order so compound operations
         * (e.g. set then delete on the same key) unwind correctly.
         *
         * @throws IllegalStateException if no transaction is currently open
         */
        public void rollback() {
            if (txnStack.isEmpty()) throw new IllegalStateException("No active transaction");
            Map<String, Object> undo = txnStack.pop();

            List<Map.Entry<String, Object>> entries = new ArrayList<>(undo.entrySet());
            for (int i = entries.size() - 1; i >= 0; i--) {
                String key  = entries.get(i).getKey();
                Object prev = entries.get(i).getValue();
                if (prev == ABSENT) store.remove(key);
                else                store.put(key, (String) prev);
            }
        }

        /**
         * Returns {@code true} if at least one transaction is currently open.
         *
         * @return {@code true} if inside a {@link #begin}/{@link #commit} or
         *         {@link #begin}/{@link #rollback} block
         */
        public boolean inTransaction() { return !txnStack.isEmpty(); }

        // ── Internal ─────────────────────────────────────────────────────────

        /**
         * Records the pre-image of {@code key} into the current transaction's undo
         * log. Only the first write per key per transaction level is recorded —
         * subsequent writes to the same key within the same transaction do not
         * change what needs to be restored.
         *
         * @param key the key about to be written or deleted
         */
        private void saveUndo(String key) {
            if (txnStack.isEmpty()) return;
            Map<String, Object> frame = txnStack.peek();
            if (!frame.containsKey(key)) {
                frame.put(key, store.containsKey(key) ? store.get(key) : ABSENT);
            }
        }

        /** Returns the string representation of the live store (e.g. {@code {a=1, b=2}}). */
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
