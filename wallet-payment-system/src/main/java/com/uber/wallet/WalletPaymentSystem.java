package com.uber.wallet;

import java.util.*;

public class WalletPaymentSystem {
    public enum Status { SUCCESS, FAILED }
    public enum Type { CREDIT, DEBIT, TRANSFER }

    public static class LedgerEntry {
        public final String idempotencyKey;
        public final String fromWallet;
        public final String toWallet;
        public final int amount;
        public final Type type;
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

    private final Map<String, Integer> balances = new HashMap<>();
    private final Map<String, LedgerEntry> idempotency = new HashMap<>();
    private final List<LedgerEntry> ledger = new ArrayList<>();

    public void createWallet(String walletId) {
        balances.putIfAbsent(walletId, 0);
    }

    public LedgerEntry deposit(String key, String walletId, int amount) {
        if (idempotency.containsKey(key)) return idempotency.get(key);
        requirePositive(amount);
        createWallet(walletId);
        balances.merge(walletId, amount, Integer::sum);
        return record(new LedgerEntry(key, null, walletId, amount, Type.CREDIT, Status.SUCCESS));
    }

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

    public int balance(String walletId) {
        return balances.getOrDefault(walletId, 0);
    }

    public List<LedgerEntry> ledger() {
        return Collections.unmodifiableList(ledger);
    }

    private LedgerEntry record(LedgerEntry entry) {
        ledger.add(entry);
        idempotency.put(entry.idempotencyKey, entry);
        return entry;
    }

    private void requirePositive(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
    }
}
