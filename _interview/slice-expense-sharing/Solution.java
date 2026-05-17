// ════════════════════════════════════════════════════════════════
// Slice Interview — Expense Sharing
// ════════════════════════════════════════════════════════════════
//
// Problem:
//   Model a Splitwise-style expense sharing system. Users add expenses
//   and split them three ways:
//     EQUAL   — divide amount equally; remainder cent goes to the first participant.
//     EXACT   — each participant is assigned a pre-specified amount (must sum to total).
//     PERCENT — each participant pays their percentage share (must sum to 100).
//
//   Track a net balance per user:
//     positive = others owe this user in aggregate
//     negative = this user owes others in aggregate
//
//   Core invariant: sum(all net balances) == 0 after every operation.
//
// API to implement:
//   String addUser(String name, String email, String phone)
//     — Registers a new user; returns assigned userId ("U1", "U2", ...).
//
//   void showUserData(String userId)
//     — Prints: [U1] Alice | alice@example.com | 9000000001
//
//   void addExpense(String paidBy, double amount, SplitType type,
//                  List<String> participants, List<Double> values, String description)
//     — Records the expense and updates net balances.
//       values is ignored for EQUAL; per-participant amounts for EXACT;
//       per-participant percentages for PERCENT.
//
//   void showUserExpenses(String userId)
//     — Prints all expenses involving this user (payer or participant).
//
//   void showUserBalance(String userId)
//     — Prints net balance for one user.
//
//   void showAllBalances()
//     — Prints "=== All Balances ===" then one balance line per registered user.
//
// Compile & run:
//   javac Solution.java && java Solution
// ════════════════════════════════════════════════════════════════

import java.util.*;

public class Solution {

    enum SplitType { EQUAL, EXACT, PERCENT }

    // ── TODO: add your fields here ───────────────────────────────

    // ── TODO: implement the required methods ─────────────────────

    public String addUser(String name, String email, String phone) {
        // YOUR CODE HERE
        return null;
    }

    public void showUserData(String userId) {
        // YOUR CODE HERE
    }

    public void addExpense(String paidBy, double amount, SplitType type,
                           List<String> participants, List<Double> values,
                           String description) {
        // YOUR CODE HERE
    }

    public void showUserExpenses(String userId) {
        // YOUR CODE HERE
    }

    public void showUserBalance(String userId) {
        // YOUR CODE HERE
    }

    public void showAllBalances() {
        // YOUR CODE HERE
    }

    // ── Test runner — do not modify ──────────────────────────────
    public static void main(String[] args) {
        Solution es = new Solution();

        String alice   = es.addUser("Alice",   "alice@example.com",   "9000000001");
        String bob     = es.addUser("Bob",     "bob@example.com",     "9000000002");
        String charlie = es.addUser("Charlie", "charlie@example.com", "9000000003");

        System.out.println("=== User Data ===");
        es.showUserData(alice);

        es.addExpense(alice, 100.0, SplitType.EQUAL,
                Arrays.asList(alice, bob, charlie), Collections.emptyList(),
                "Dinner");
        System.out.println("\n--- After EQUAL $100 (Alice pays) ---");
        es.showAllBalances();

        es.addExpense(bob, 90.0, SplitType.EXACT,
                Arrays.asList(alice, bob, charlie),
                Arrays.asList(45.0, 30.0, 15.0),
                "Hotel");
        System.out.println("\n--- After EXACT $90 (Bob pays: Alice=$45, Bob=$30, Charlie=$15) ---");
        es.showAllBalances();

        es.addExpense(charlie, 60.0, SplitType.PERCENT,
                Arrays.asList(alice, bob, charlie),
                Arrays.asList(50.0, 30.0, 20.0),
                "Cab");
        System.out.println("\n--- After PERCENT $60 (Charlie pays: Alice=50%, Bob=30%, Charlie=20%) ---");
        es.showAllBalances();

        System.out.println("\n=== Alice's Expenses ===");
        es.showUserExpenses(alice);
    }
}
