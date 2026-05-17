import java.util.*;
import java.util.stream.*;

/**
 * Entry point demonstrating {@link RuleEngine}.
 * Compile: {@code javac ExpenseRuleEngine.java}  Run: {@code java ExpenseRuleEngine}
 */
public class ExpenseRuleEngine {

    // ── Domain model ─────────────────────────────────────────────────────────

    /**
     * Immutable representation of a corporate expense submission.
     *
     * <p>Category and vendor are normalised to uppercase on construction so rule
     * implementations can use case-insensitive comparisons without extra ceremony.
     */
    static class Expense {
        /** Unique expense identifier. */
        final String id;

        /** Expense category, e.g. {@code "TRAVEL"}, {@code "MEALS"}. Stored uppercase. */
        final String category;

        /** Vendor name. Stored uppercase. */
        final String vendor;

        /** Total expense amount in USD. */
        final double amount;

        /** Whether the submission includes a receipt. */
        final boolean hasReceipt;

        /**
         * @param id         unique identifier
         * @param category   expense category (normalised to uppercase)
         * @param vendor     vendor name (normalised to uppercase)
         * @param amount     total amount in USD
         * @param hasReceipt whether a receipt was attached
         */
        Expense(String id, String category, String vendor, double amount, boolean hasReceipt) {
            this.id = id;
            this.category = category.toUpperCase();
            this.vendor = vendor.toUpperCase();
            this.amount = amount;
            this.hasReceipt = hasReceipt;
        }

        @Override public String toString() {
            return String.format("[%s | %s | %s | $%.2f | receipt=%s]",
                                 id, category, vendor, amount, hasReceipt);
        }
    }

    /**
     * Outcome of applying one rule to one expense.
     *
     * <p>Carries a human-readable {@code reason} when the rule fails, enabling
     * batch UIs to display all policy violations at once.
     */
    static class ValidationResult {
        /** Whether the expense satisfied this rule. */
        final boolean passed;

        /** Name of the rule that produced this result. */
        final String ruleName;

        /** Explanation of the failure; empty string on success. */
        final String reason;

        /** Private — use {@link #ok} or {@link #fail} factory methods. */
        private ValidationResult(boolean passed, String ruleName, String reason) {
            this.passed = passed;
            this.ruleName = ruleName;
            this.reason = reason;
        }

        /**
         * @param ruleName the rule that passed
         * @return a passing result
         */
        static ValidationResult ok(String ruleName) {
            return new ValidationResult(true, ruleName, "");
        }

        /**
         * @param ruleName the rule that failed
         * @param reason   human-readable explanation
         * @return a failing result
         */
        static ValidationResult fail(String ruleName, String reason) {
            return new ValidationResult(false, ruleName, reason);
        }

        @Override public String toString() {
            return passed ? "PASS [" + ruleName + "]"
                          : "FAIL [" + ruleName + "] — " + reason;
        }
    }

    // ── Rule interface ────────────────────────────────────────────────────────

    /**
     * A single validation policy applied to an {@link Expense}.
     *
     * <p>A functional interface — implementations can be lambdas, anonymous classes,
     * or named classes. Adding a new rule requires only a new implementation; the
     * {@link RuleEngine} needs no modification (Open-Closed Principle).
     */
    @FunctionalInterface
    interface Rule {
        /**
         * Evaluates whether {@code expense} satisfies this rule.
         *
         * @param expense the expense to evaluate
         * @return a {@link ValidationResult} indicating pass or fail with reason
         */
        ValidationResult validate(Expense expense);

        /** Returns a display name for this rule; defaults to the class simple name. */
        default String name() { return getClass().getSimpleName(); }
    }

    // ── Built-in rule implementations ─────────────────────────────────────────

    /** Rejects expenses whose {@code amount} exceeds the configured limit. */
    static class MaxAmountRule implements Rule {
        private final double limit;

        /** @param limit maximum allowed expense amount in USD */
        MaxAmountRule(double limit) { this.limit = limit; }

        @Override public ValidationResult validate(Expense e) {
            return e.amount <= limit
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(),
                    String.format("$%.2f exceeds max $%.2f", e.amount, limit));
        }
        @Override public String name() { return "MaxAmount($" + limit + ")"; }
    }

    /** Rejects expenses whose category is not in the approved set. */
    static class AllowedCategoryRule implements Rule {
        private final Set<String> allowed;

        /** @param categories varargs of approved category strings (case-insensitive) */
        AllowedCategoryRule(String... categories) {
            allowed = new HashSet<>(Arrays.asList(categories));
        }

        @Override public ValidationResult validate(Expense e) {
            return allowed.contains(e.category)
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(),
                    "Category '" + e.category + "' not in allowed set " + allowed);
        }
        @Override public String name() { return "AllowedCategory(" + allowed + ")"; }
    }

    /** Rejects expenses in a specific category that exceed a per-category cap. */
    static class MaxPerCategoryRule implements Rule {
        private final String category;
        private final double limit;

        /**
         * @param category the category this cap applies to
         * @param limit    maximum allowed amount for that category in USD
         */
        MaxPerCategoryRule(String category, double limit) {
            this.category = category.toUpperCase();
            this.limit = limit;
        }

        @Override public ValidationResult validate(Expense e) {
            if (!e.category.equals(category)) return ValidationResult.ok(name()); // not applicable
            return e.amount <= limit
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(),
                    String.format("%s expense $%.2f exceeds per-category cap $%.2f",
                                  category, e.amount, limit));
        }
        @Override public String name() { return "MaxPerCategory(" + category + ",$" + limit + ")"; }
    }

    /** Requires a receipt for any expense exceeding the configured threshold. */
    static class RequireReceiptAboveRule implements Rule {
        private final double threshold;

        /** @param threshold minimum amount (exclusive) that requires a receipt */
        RequireReceiptAboveRule(double threshold) { this.threshold = threshold; }

        @Override public ValidationResult validate(Expense e) {
            if (e.amount <= threshold) return ValidationResult.ok(name());
            return e.hasReceipt
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(),
                    String.format("Receipt required for amounts > $%.2f (submitted $%.2f)",
                                  threshold, e.amount));
        }
        @Override public String name() { return "RequireReceiptAbove($" + threshold + ")"; }
    }

    /** Rejects expenses from vendors on a blocklist. */
    static class BlockedVendorRule implements Rule {
        private final Set<String> blocked;

        /** @param vendors varargs of blocked vendor names (case-insensitive) */
        BlockedVendorRule(String... vendors) {
            blocked = new HashSet<>(Arrays.asList(vendors));
        }

        @Override public ValidationResult validate(Expense e) {
            return !blocked.contains(e.vendor)
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(), "Vendor '" + e.vendor + "' is blocked");
        }
        @Override public String name() { return "BlockedVendor(" + blocked + ")"; }
    }

    // ── Engine ────────────────────────────────────────────────────────────────

    /**
     * Policy engine that evaluates a set of {@link Rule}s against expense submissions.
     *
     * <p>Rules are stored in insertion order. {@link #validate} short-circuits on the
     * first failure; {@link #validateAll} collects every violation for richer error UIs.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class RuleEngine {
        /** Ordered list of registered policy rules. */
        private final List<Rule> rules = new ArrayList<>();

        /**
         * Registers a policy rule. Rules are evaluated in the order they are added.
         *
         * @param rule the rule to add
         */
        public void addRule(Rule rule) { rules.add(rule); }

        /**
         * Validates {@code expense} against all rules, returning on the first failure.
         *
         * @param expense the expense to validate
         * @return the first failing {@link ValidationResult}, or a passing result if all pass
         */
        public ValidationResult validate(Expense expense) {
            for (Rule rule : rules) {
                ValidationResult result = rule.validate(expense);
                if (!result.passed) return result;
            }
            return ValidationResult.ok("ALL_RULES");
        }

        /**
         * Validates {@code expense} against all rules, collecting every violation.
         *
         * @param expense the expense to validate
         * @return list of all failing results, or a single passing result if all rules pass
         */
        public List<ValidationResult> validateAll(Expense expense) {
            List<ValidationResult> failures = rules.stream()
                .map(r -> r.validate(expense))
                .filter(r -> !r.passed)
                .collect(java.util.stream.Collectors.toList());
            return failures.isEmpty()
                ? List.of(ValidationResult.ok("ALL_RULES"))
                : failures;
        }

        /**
         * Validates a list of expenses, returning a map of expenseId → violations.
         *
         * @param expenses the expenses to validate
         * @return insertion-ordered map from expense ID to all its validation results
         */
        public Map<String, List<ValidationResult>> validateBatch(List<Expense> expenses) {
            Map<String, List<ValidationResult>> report = new LinkedHashMap<>();
            for (Expense e : expenses) report.put(e.id, validateAll(e));
            return report;
        }

        /**
         * Prints a human-readable approval/rejection report for each expense.
         *
         * @param expenses the expenses to report on
         */
        public void printBatchReport(List<Expense> expenses) {
            Map<String, List<ValidationResult>> report = validateBatch(expenses);
            System.out.println("=== Expense Validation Report ===");
            for (Expense e : expenses) {
                List<ValidationResult> results = report.get(e.id);
                boolean allPassed = results.stream().allMatch(r -> r.passed);
                System.out.printf("%-35s %s%n", e, allPassed ? "✓ APPROVED" : "✗ REJECTED");
                if (!allPassed) results.forEach(r -> System.out.println("    " + r));
            }
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        RuleEngine engine = new RuleEngine();
        engine.addRule(new MaxAmountRule(500.00));
        engine.addRule(new AllowedCategoryRule("TRAVEL", "MEALS", "SOFTWARE", "OFFICE"));
        engine.addRule(new MaxPerCategoryRule("MEALS", 75.00));
        engine.addRule(new RequireReceiptAboveRule(25.00));
        engine.addRule(new BlockedVendorRule("CASINO_LV", "LUXURY_SPA"));

        List<Expense> expenses = List.of(
            new Expense("EXP-001", "TRAVEL",   "DELTA_AIR",  450.00, true),   // PASS
            new Expense("EXP-002", "MEALS",    "MCDONALDS",   12.50, false),   // PASS (no receipt ok, ≤25)
            new Expense("EXP-003", "MEALS",    "NOBU",       150.00, true),    // FAIL: meals cap $75
            new Expense("EXP-004", "SOFTWARE", "GITHUB",      80.00, false),   // FAIL: no receipt (>$25)
            new Expense("EXP-005", "GAMBLING", "CASINO_LV",  200.00, true),    // FAIL: bad category + blocked vendor
            new Expense("EXP-006", "TRAVEL",   "MARRIOTT",   600.00, true),    // FAIL: exceeds $500
            new Expense("EXP-007", "OFFICE",   "STAPLES",     18.00, false)    // PASS (≤25, no receipt ok)
        );

        engine.printBatchReport(expenses);

        System.out.println("\n=== Lambda rule (custom, one-off) ===");
        RuleEngine customEngine = new RuleEngine();
        // Add a lambda rule inline — shows extensibility without new class
        customEngine.addRule(e -> e.category.equals("TRAVEL") && e.amount > 200
            ? ValidationResult.fail("PreApprovalRequired",
                "Travel expenses > $200 require pre-approval")
            : ValidationResult.ok("PreApprovalRequired"));

        System.out.println(customEngine.validate(
            new Expense("E1", "TRAVEL", "UNITED", 350.00, true)));
        System.out.println(customEngine.validate(
            new Expense("E2", "TRAVEL", "LYFT",    45.00, true)));
    }
}
