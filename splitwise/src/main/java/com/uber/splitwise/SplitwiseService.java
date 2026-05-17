package com.uber.splitwise;

import java.util.*;

/**
 * Expense-splitting service (Splitwise-style) with debt simplification.
 *
 * <p>Each user's state is a single integer {@code netBalance}: positive means
 * others owe that user money; negative means the user owes others.
 * {@link #simplifyDebts()} produces a minimal set of payments using a greedy
 * max-heap / min-heap algorithm.
 *
 * <p>Data structures: {@code HashMap<userId, netBalance>} for O(1) balance updates;
 * two {@code PriorityQueue}s for debt simplification.
 *
 * <p>Not thread-safe.
 */
public class SplitwiseService {
    /**
     * Represents a single payment that settles a debt.
     */
    public static class Settlement {
        /** The user who pays. */
        public final String from;
        /** The user who receives the payment. */
        public final String to;
        /** Payment amount. */
        public final int amount;

        /**
         * @param from   payer user ID
         * @param to     payee user ID
         * @param amount amount transferred
         */
        public Settlement(String from, String to, int amount) {
            this.from = from;
            this.to = to;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return from + " pays " + to + " " + amount;
        }
    }

    /** All registered user IDs. */
    private final Set<String> users = new HashSet<>();
    /** Group ID → member user IDs. */
    private final Map<String, Set<String>> groups = new HashMap<>();
    /** User ID → net balance (positive = owed to user, negative = user owes). */
    private final Map<String, Integer> netBalance = new HashMap<>();

    /**
     * Registers a user. Idempotent — re-registering does not reset the balance.
     *
     * @param userId unique user identifier
     */
    public void addUser(String userId) {
        users.add(userId);
        netBalance.putIfAbsent(userId, 0);
    }

    /**
     * Creates a group and auto-registers any new members.
     *
     * @param groupId unique group identifier
     * @param userIds members of the group
     */
    public void addGroup(String groupId, Collection<String> userIds) {
        for (String userId : userIds) addUser(userId);
        groups.put(groupId, new HashSet<>(userIds));
    }

    /**
     * Splits an expense equally among all group members.
     *
     * @param groupId the group sharing the expense
     * @param paidBy  user who paid (must be a group member)
     * @param amount  total amount paid (must be evenly divisible by group size)
     * @throws IllegalArgumentException if the group does not exist, the payer is not a
     *                                  member, or the amount cannot be split evenly
     */
    public void addEqualExpense(String groupId, String paidBy, int amount) {
        Set<String> members = requireGroup(groupId);
        if (!members.contains(paidBy)) throw new IllegalArgumentException("payer not in group");
        if (amount % members.size() != 0) throw new IllegalArgumentException("amount must split evenly");

        int share = amount / members.size();
        Map<String, Integer> owed = new HashMap<>();
        for (String member : members) owed.put(member, share);
        addExpense(paidBy, owed);
    }

    /**
     * Records an arbitrary expense with per-user breakdown.
     *
     * <p>Auto-registers any participants not yet in the system.
     *
     * @param paidBy      user who paid the full amount
     * @param owedByUser  map from each participant to the amount they owe
     */
    public void addExpense(String paidBy, Map<String, Integer> owedByUser) {
        addUser(paidBy);
        int total = owedByUser.values().stream().mapToInt(Integer::intValue).sum();
        netBalance.merge(paidBy, total, Integer::sum);

        for (Map.Entry<String, Integer> entry : owedByUser.entrySet()) {
            addUser(entry.getKey());
            netBalance.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
    }

    /**
     * @param userId the user to query
     * @return net balance (positive = owed to user, negative = user owes, 0 if unknown)
     */
    public int getNetBalance(String userId) {
        return netBalance.getOrDefault(userId, 0);
    }

    /**
     * Computes the minimum number of payments needed to settle all debts.
     *
     * <p>Uses a greedy approach: at each step the largest creditor receives from the
     * largest debtor.
     *
     * @return list of {@link Settlement} objects describing who pays whom and how much
     */
    public List<Settlement> simplifyDebts() {
        PriorityQueue<Map.Entry<String, Integer>> creditors =
                new PriorityQueue<>((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        PriorityQueue<Map.Entry<String, Integer>> debtors =
                new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : netBalance.entrySet()) {
            if (entry.getValue() > 0) creditors.add(new AbstractMap.SimpleEntry<>(entry));
            if (entry.getValue() < 0) debtors.add(new AbstractMap.SimpleEntry<>(entry));
        }

        List<Settlement> result = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Map.Entry<String, Integer> creditor = creditors.poll();
            Map.Entry<String, Integer> debtor = debtors.poll();
            int amount = Math.min(creditor.getValue(), -debtor.getValue());

            result.add(new Settlement(debtor.getKey(), creditor.getKey(), amount));
            creditor.setValue(creditor.getValue() - amount);
            debtor.setValue(debtor.getValue() + amount);

            if (creditor.getValue() > 0) creditors.add(creditor);
            if (debtor.getValue() < 0) debtors.add(debtor);
        }
        return result;
    }

    /** @throws IllegalArgumentException if the group does not exist */
    private Set<String> requireGroup(String groupId) {
        Set<String> members = groups.get(groupId);
        if (members == null) throw new IllegalArgumentException("unknown group");
        return members;
    }
}
