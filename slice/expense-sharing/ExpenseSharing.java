// Companies: Slice
// Expense-sharing system — tracks shared expenses and who owes whom.

import java.util.*;

/**
 * Models a Splitwise-style expense sharing system where users add expenses
 * and split them by equal shares, exact amounts, or percentages.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code users}: ordered map of userId → User, for O(1) lookup and stable iteration.</li>
 *   <li>{@code expenses}: list of all recorded expenses for per-user history queries.</li>
 *   <li>{@code netBalance}: maps userId → net amount (positive = owed to them,
 *       negative = they owe). Updated on every {@link #addExpense} call.</li>
 * </ul>
 *
 * <p>Supported split types:
 * <ul>
 *   <li>{@code EQUAL} — divide amount equally; remainder (in cents) goes to the first participant.</li>
 *   <li>{@code EXACT} — each participant is assigned a pre-specified amount; must sum to total.</li>
 *   <li>{@code PERCENT} — each participant pays their percentage share; percentages must sum to 100.</li>
 * </ul>
 *
 * <p>Core invariant: {@code sum(netBalance.values()) == 0} after every operation — money
 * is only redistributed, never created or destroyed.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class ExpenseSharing {

    // ── Types ─────────────────────────────────────────────────────────────

    /** Split strategy for an expense. */
    enum SplitType { EQUAL, EXACT, PERCENT }

    /** Immutable snapshot of a single user. */
    static class User {
        final String userId, name, email, phone;
        User(String userId, String name, String email, String phone) {
            this.userId = userId; this.name = name;
            this.email  = email;  this.phone = phone;
        }
    }

    /** Record of one expense with its participant shares. */
    static class Expense {
        final String expenseId, paidBy, description;
        final double amount;
        final SplitType type;
        /** Maps each participant userId → their share of this expense. */
        final Map<String, Double> shares;

        Expense(String id, String paidBy, double amount, SplitType type,
                Map<String, Double> shares, String description) {
            this.expenseId   = id;
            this.paidBy      = paidBy;
            this.amount      = amount;
            this.type        = type;
            this.shares      = Collections.unmodifiableMap(shares);
            this.description = description;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────

    /** Registry of all users; insertion-ordered for deterministic display. */
    private final Map<String, User> users = new LinkedHashMap<>();

    /** Full history of all expenses for per-user queries. */
    private final List<Expense> expenses = new ArrayList<>();

    /**
     * Net balance per user. Positive means this user is owed money in aggregate;
     * negative means they owe money in aggregate.
     */
    private final Map<String, Double> netBalance = new LinkedHashMap<>();

    private int userCount    = 0;
    private int expenseCount = 0;

    // ── User management ───────────────────────────────────────────────────

    /**
     * Registers a new user and returns their generated userId.
     *
     * @param name  display name
     * @param email email address
     * @param phone mobile number
     * @return the assigned userId (e.g. "U1", "U2", …)
     */
    public String addUser(String name, String email, String phone) {
        String id = "U" + (++userCount);
        users.put(id, new User(id, name, email, phone));
        netBalance.put(id, 0.0);
        return id;
    }

    /**
     * Prints the profile (userId, name, email, phone) of the given user.
     *
     * @param userId the user to display
     */
    public void showUserData(String userId) {
        User u = users.get(userId);
        if (u == null) { System.out.println("Unknown user: " + userId); return; }
        System.out.printf("[%s] %s | %s | %s%n", u.userId, u.name, u.email, u.phone);
    }

    // ── Expense management ────────────────────────────────────────────────

    /**
     * Records an expense paid by {@code paidBy} and splits it among {@code participants}
     * according to {@code type}.
     *
     * <p>Balance update rule: for every participant p,
     * {@code netBalance[p] -= share[p]}; then {@code netBalance[paidBy] += amount}.
     * This works whether or not the payer is also a participant.
     *
     * @param paidBy       userId of the person who paid
     * @param amount       total expense amount (must be positive)
     * @param type         how to split ({@link SplitType})
     * @param participants list of userIds sharing the cost
     * @param values       for EQUAL: ignored; for EXACT: per-participant amounts;
     *                     for PERCENT: per-participant percentages (must sum to 100)
     * @param description  human-readable label for the expense
     * @throws IllegalArgumentException if amounts/percentages do not match the total
     */
    public void addExpense(String paidBy, double amount, SplitType type,
                           List<String> participants, List<Double> values,
                           String description) {
        Map<String, Double> shares = computeShares(amount, type, participants, values);

        String eid = "E" + (++expenseCount);
        expenses.add(new Expense(eid, paidBy, amount, type, shares, description));

        // Debit each participant their share, then credit the payer the full amount.
        for (Map.Entry<String, Double> e : shares.entrySet()) {
            netBalance.merge(e.getKey(), -e.getValue(), Double::sum);
        }
        netBalance.merge(paidBy, amount, Double::sum);
    }

    /**
     * Prints all expenses that involve the given user (either as payer or participant).
     *
     * @param userId the user whose expense history to display
     */
    public void showUserExpenses(String userId) {
        System.out.println("Expenses for " + userId + ":");
        boolean found = false;
        for (Expense ex : expenses) {
            if (ex.paidBy.equals(userId) || ex.shares.containsKey(userId)) {
                double myShare = ex.shares.containsKey(userId) ? ex.shares.get(userId) : 0.0;
                System.out.printf("  [%s] %s - total $%.2f (paid by %s, your share $%.2f)%n",
                        ex.expenseId, ex.description, ex.amount, ex.paidBy, myShare);
                found = true;
            }
        }
        if (!found) System.out.println("  (none)");
    }

    /**
     * Prints the net balance for the given user.
     * Positive balance = others owe this user; negative = this user owes others.
     *
     * @param userId the user to display
     */
    public void showUserBalance(String userId) {
        double bal = netBalance.getOrDefault(userId, 0.0);
        User u = users.get(userId);
        String name = (u != null) ? u.name : userId;
        if (Math.abs(bal) < 0.01) {
            System.out.printf("%s is settled up ($0.00)%n", name);
        } else if (bal > 0) {
            System.out.printf("%s is owed $%.2f%n", name, bal);
        } else {
            System.out.printf("%s owes $%.2f%n", name, -bal);
        }
    }

    /**
     * Prints the net balance for every registered user.
     */
    public void showAllBalances() {
        System.out.println("=== All Balances ===");
        for (String uid : users.keySet()) {
            showUserBalance(uid);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /**
     * Computes each participant's share according to the split type.
     * EQUAL: remainder cent goes to the first participant.
     * EXACT: validates that provided amounts sum to total (within $0.01).
     * PERCENT: validates that percentages sum to 100.
     */
    private Map<String, Double> computeShares(double amount, SplitType type,
                                               List<String> participants,
                                               List<Double> values) {
        Map<String, Double> shares = new LinkedHashMap<>();
        int n = participants.size();

        if (type == SplitType.EQUAL) {
            // floor to 2 decimal places; remainder goes to the first participant
            double base = Math.floor(amount * 100.0 / n) / 100.0;
            double first = round2(amount - base * (n - 1));
            for (int i = 0; i < n; i++) {
                shares.put(participants.get(i), i == 0 ? first : base);
            }

        } else if (type == SplitType.EXACT) {
            double total = 0;
            for (int i = 0; i < n; i++) {
                shares.put(participants.get(i), round2(values.get(i)));
                total += values.get(i);
            }
            if (Math.abs(total - amount) > 0.01)
                throw new IllegalArgumentException(
                        "EXACT shares sum to " + total + " but expense is " + amount);

        } else {  // PERCENT
            double totalPct = 0;
            for (double v : values) totalPct += v;
            if (Math.abs(totalPct - 100.0) > 0.01)
                throw new IllegalArgumentException(
                        "PERCENT shares sum to " + totalPct + "%, must be 100%");
            for (int i = 0; i < n; i++) {
                shares.put(participants.get(i), round2(amount * values.get(i) / 100.0));
            }
        }
        return shares;
    }

    /** Rounds a monetary value to 2 decimal places. */
    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        ExpenseSharing es = new ExpenseSharing();

        // Register users
        String alice   = es.addUser("Alice",   "alice@example.com",   "9000000001");
        String bob     = es.addUser("Bob",     "bob@example.com",     "9000000002");
        String charlie = es.addUser("Charlie", "charlie@example.com", "9000000003");

        System.out.println("=== User Data ===");
        es.showUserData(alice);
        // Expected: [U1] Alice | alice@example.com | 9000000001

        // EQUAL split: Alice pays $100 for all three
        es.addExpense(alice, 100.0, SplitType.EQUAL,
                Arrays.asList(alice, bob, charlie), Collections.emptyList(),
                "Dinner");
        System.out.println("\n--- After EQUAL $100 (Alice pays) ---");
        es.showAllBalances();
        // Expected: Alice is owed $66.66, Bob owes $33.33, Charlie owes $33.33

        // EXACT split: Bob pays $90; Alice=$45, Bob=$30, Charlie=$15
        es.addExpense(bob, 90.0, SplitType.EXACT,
                Arrays.asList(alice, bob, charlie),
                Arrays.asList(45.0, 30.0, 15.0),
                "Hotel");
        System.out.println("\n--- After EXACT $90 (Bob pays: Alice=$45, Bob=$30, Charlie=$15) ---");
        es.showAllBalances();
        // Expected:
        //   Alice is owed $21.67  (66.67 - 45.00 = 21.67)
        //   Bob   owes   $ 3.33   (-33.33 + 90.00 - 30.00 = +26.67... wait let me recalc)
        //
        // After dinner: Alice=+66.67, Bob=-33.33, Charlie=-33.33
        // Hotel: debit Alice -45, debit Bob -30, debit Charlie -15, credit Bob +90
        // Alice: 66.67 - 45.00 = +21.67 (owed to her)
        // Bob:  -33.33 - 30.00 + 90.00 = +26.67 (owed to him)
        // Charlie: -33.33 - 15.00 = -48.33 (owes)

        // PERCENT split: Charlie pays $60; Alice=50%, Bob=30%, Charlie=20%
        es.addExpense(charlie, 60.0, SplitType.PERCENT,
                Arrays.asList(alice, bob, charlie),
                Arrays.asList(50.0, 30.0, 20.0),
                "Cab");
        System.out.println("\n--- After PERCENT $60 (Charlie pays: Alice=50%, Bob=30%, Charlie=20%) ---");
        es.showAllBalances();
        // Alice:   21.67 - 30.00 = -8.33 (owes)
        // Bob:     26.67 - 18.00 = +8.67 (owed)
        // Charlie: -48.33 - 12.00 + 60.00 = -0.33 (owes)

        System.out.println("\n=== Alice's Expenses ===");
        es.showUserExpenses(alice);
        // Expected: all 3 expenses (Alice paid Dinner, and is participant in Hotel & Cab)
    }
}
