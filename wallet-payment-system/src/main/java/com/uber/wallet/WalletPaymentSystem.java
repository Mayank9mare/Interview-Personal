package com.uber.wallet;

import java.util.*;

/**
 * Simple wallet payment system with idempotency, balance tracking, and a full ledger.
 *
 * <p>Every mutating operation requires a caller-supplied {@code idempotencyKey}. If the
 * same key is submitted again the original {@link LedgerEntry} is returned unchanged,
 * guaranteeing exactly-once semantics.
 *
 * <p>Data structures: {@code HashMap<walletId, balance>} for O(1) balance reads/writes;
 * {@code HashMap<key, LedgerEntry>} for O(1) idempotency checks;
 * {@code ArrayList<LedgerEntry>} as an append-only ledger.
 *
 * <p>Not thread-safe.
 */
public class WalletPaymentSystem {
    /** Outcome of a payment operation. */
    public enum Status { SUCCESS, FAILED }
    /** Category of a ledger entry. */
    public enum Type { CREDIT, DEBIT, TRANSFER }

    /**
     * Immutable record of one financial operation stored in the ledger.
     */
    public static class LedgerEntry {
        /** Caller-supplied idempotency key. */
        public final String idempotencyKey;
        /** Source wallet ({@code null} for deposits). */
        public final String fromWallet;
        /** Destination wallet ({@code null} for withdrawals). */
        public final String toWallet;
        /** Amount involved in the operation. */
        public final int amount;
        /** Type of operation. */
        public final Type type;
        /** Whether the operation succeeded. */
        public final Status status;

        private LedgerEntry(String key, String from, String to, int amount, Type type, Status status) {
            this.idempotencyKey = key;
            this.fromWallet = from;
            this.toWallet = to;
            this.amount = amount;
            this.type = type;
            this.status = status;
        }
    }

    /** walletId → current balance. */
    private final Map<String, Integer> balances = new HashMap<>();
    /** idempotencyKey → previously recorded entry for deduplication. */
    private final Map<String, LedgerEntry> idempotency = new HashMap<>();
    /** Append-only transaction log. */
    private final List<LedgerEntry> ledger = new ArrayList<>();

    /**
     * Creates a wallet with a zero balance. Idempotent — calling again does not reset
     * an existing balance.
     *
     * @param walletId unique wallet identifier
     */
    public void createWallet(String walletId) {
        balances.putIfAbsent(walletId, 0);
    }

    /**
     * Credits a wallet. Creates the wallet if it does not already exist.
     *
     * @param key      idempotency key; duplicate calls return the original entry
     * @param walletId wallet to credit
     * @param amount   positive amount to add
     * @return the resulting ledger entry
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public LedgerEntry deposit(String key, String walletId, int amount) {
        if (idempotency.containsKey(key)) return idempotency.get(key);
        requirePositive(amount);
        createWallet(walletId);
        balances.merge(walletId, amount, Integer::sum);
        return record(new LedgerEntry(key, null, walletId, amount, Type.CREDIT, Status.SUCCESS));
    }

    /**
     * Debits a wallet. Records a FAILED entry if the balance is insufficient.
     *
     * @param key      idempotency key
     * @param walletId wallet to debit
     * @param amount   positive amount to remove
     * @return the resulting ledger entry (status SUCCESS or FAILED)
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public LedgerEntry withdraw(String key, String walletId, int amount) {
        if (idempotency.containsKey(key)) return idempotency.get(key);
        requirePositive(amount);
        createWallet(walletId);
        if (balances.get(walletId) < amount) {
            return record(new LedgerEntry(key, walletId, null, amount, Type.DEBIT, Status.FAILED));
        }
        balances.merge(walletId, -amount, Integer::sum);
        return record(new LedgerEntry(key, walletId, null, amount, Type.DEBIT, Status.SUCCESS));
    }

    /**
     * Transfers funds between two wallets atomically. Records a FAILED entry if the
     * source has insufficient balance.
     *
     * @param key        idempotency key
     * @param fromWallet source wallet
     * @param toWallet   destination wallet
     * @param amount     positive amount to transfer
     * @return the resulting ledger entry (status SUCCESS or FAILED)
     * @throws IllegalArgumentException if {@code amount} is not positive
     */
    public LedgerEntry transfer(String key, String fromWallet, String toWallet, int amount) {
        if (idempotency.containsKey(key)) return idempotency.get(key);
        requirePositive(amount);
        createWallet(fromWallet);
        createWallet(toWallet);
        if (balances.get(fromWallet) < amount) {
            return record(new LedgerEntry(key, fromWallet, toWallet, amount, Type.TRANSFER, Status.FAILED));
        }
        balances.merge(fromWallet, -amount, Integer::sum);
        balances.merge(toWallet, amount, Integer::sum);
        return record(new LedgerEntry(key, fromWallet, toWallet, amount, Type.TRANSFER, Status.SUCCESS));
    }

    /**
     * @param walletId the wallet to query
     * @return current balance, or 0 if the wallet does not exist
     */
    public int balance(String walletId) {
        return balances.getOrDefault(walletId, 0);
    }

    /**
     * @return unmodifiable view of all ledger entries in insertion order
     */
    public List<LedgerEntry> ledger() {
        return Collections.unmodifiableList(ledger);
    }

    /** Appends an entry to the ledger and registers it for idempotency checking. */
    private LedgerEntry record(LedgerEntry entry) {
        ledger.add(entry);
        idempotency.put(entry.idempotencyKey, entry);
        return entry;
    }

    /** @throws IllegalArgumentException if {@code amount} is not positive */
    private void requirePositive(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
