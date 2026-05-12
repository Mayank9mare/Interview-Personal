package com.uber.splitwise;

import java.util.*;

public class SplitwiseService {
    public static class Settlement {
        public final String from;
        public final String to;
        public final int amount;

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

    private final Set<String> users = new HashSet<>();
    private final Map<String, Set<String>> groups = new HashMap<>();
    private final Map<String, Integer> netBalance = new HashMap<>();

    public void addUser(String userId) {
        users.add(userId);
        netBalance.putIfAbsent(userId, 0);
    }

    public void addGroup(String groupId, Collection<String> userIds) {
        for (String userId : userIds) addUser(userId);
        groups.put(groupId, new HashSet<>(userIds));
    }

    public void addEqualExpense(String groupId, String paidBy, int amount) {
        Set<String> members = requireGroup(groupId);
        if (!members.contains(paidBy)) throw new IllegalArgumentException("payer not in group");
        if (amount % members.size() != 0) throw new IllegalArgumentException("amount must split evenly");

        int share = amount / members.size();
        Map<String, Integer> owed = new HashMap<>();
        for (String member : members) owed.put(member, share);
        addExpense(paidBy, owed);
    }

    public void addExpense(String paidBy, Map<String, Integer> owedByUser) {
        addUser(paidBy);
        int total = owedByUser.values().stream().mapToInt(Integer::intValue).sum();
        netBalance.merge(paidBy, total, Integer::sum);

        for (Map.Entry<String, Integer> entry : owedByUser.entrySet()) {
            addUser(entry.getKey());
            netBalance.merge(entry.getKey(), -entry.getValue(), Integer::sum);
        }
    }

    public int getNetBalance(String userId) {
        return netBalance.getOrDefault(userId, 0);
    }

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

    private Set<String> requireGroup(String groupId) {
        Set<String> members = groups.get(groupId);
        if (members == null) throw new IllegalArgumentException("unknown group");
        return members;
    }
}
