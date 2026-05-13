import java.util.*;
import java.util.stream.*;

public class ExpenseRuleEngine {

    // ═══════════════════════════════════════════════════════════════════════════
    // Expense Rule Engine
    //
    // Validates corporate expense submissions against a configurable set of
    // business rules (think Rippling's expense policy management).
    //
    //   addRule(Rule)               — register a policy rule
    //   validate(Expense)           — fail-fast: first violated rule
    //   validateAll(Expense)        — collect ALL violations (for error display)
    //   validateBatch(List<Expense>) — validate a list, return per-expense results
    //
    // Built-in rule types:
    //   MaxAmountRule(limit)               — single expense ≤ limit
    //   AllowedCategoryRule(categories)    — category must be in approved list
    //   MaxPerCategoryRule(cat, limit)     — category-specific cap
    //   RequireReceiptAboveRule(threshold) — receipt required when amount > threshold
    //   BlockedVendorRule(vendors)         — vendor must not be on blocklist
    //
    // Design:
    //   Rule is a functional interface → lambdas or named classes both work.
    //   ValidationResult carries (passed, reason) so callers get actionable messages.
    //   Engine stores rules in insertion order; validate() short-circuits on first fail.
    //
    // Extensibility (open/closed principle):
    //   Add any new rule by implementing Rule — no engine changes needed.
    //   Rules compose: CombinedRule(r1, r2) = r1 AND r2.
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Domain model ─────────────────────────────────────────────────────────

    static class Expense {
        final String id;
        final String category;      // e.g. "TRAVEL", "MEALS", "SOFTWARE"
        final String vendor;
        final double amount;        // USD
        final boolean hasReceipt;

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

    static class ValidationResult {
        final boolean passed;
        final String ruleName;
        final String reason;

        private ValidationResult(boolean passed, String ruleName, String reason) {
            this.passed = passed;
            this.ruleName = ruleName;
            this.reason = reason;
        }

        static ValidationResult ok(String ruleName) {
            return new ValidationResult(true, ruleName, "");
        }

        static ValidationResult fail(String ruleName, String reason) {
            return new ValidationResult(false, ruleName, reason);
        }

        @Override public String toString() {
            return passed ? "PASS [" + ruleName + "]"
                          : "FAIL [" + ruleName + "] — " + reason;
        }
    }

    // ── Rule interface ────────────────────────────────────────────────────────

    @FunctionalInterface
    interface Rule {
        ValidationResult validate(Expense expense);
        default String name() { return getClass().getSimpleName(); }
    }

    // ── Built-in rule implementations ─────────────────────────────────────────

    static class MaxAmountRule implements Rule {
        private final double limit;
        MaxAmountRule(double limit) { this.limit = limit; }

        @Override public ValidationResult validate(Expense e) {
            return e.amount <= limit
                ? ValidationResult.ok(name())
                : ValidationResult.fail(name(),
                    String.format("$%.2f exceeds max $%.2f", e.amount, limit));
        }
        @Override public String name() { return "MaxAmount($" + limit + ")"; }
    }

    static class AllowedCategoryRule implements Rule {
        private final Set<String> allowed;
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

    static class MaxPerCategoryRule implements Rule {
        private final String category;
        private final double limit;
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

    static class RequireReceiptAboveRule implements Rule {
        private final double threshold;
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

    static class BlockedVendorRule implements Rule {
        private final Set<String> blocked;
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

    static class RuleEngine {
        private final List<Rule> rules = new ArrayList<>();

        public void addRule(Rule rule) { rules.add(rule); }

        // Fail-fast: returns first failure, or success if all pass
        public ValidationResult validate(Expense expense) {
            for (Rule rule : rules) {
                ValidationResult result = rule.validate(expense);
                if (!result.passed) return result;
            }
            return ValidationResult.ok("ALL_RULES");
        }

        // Collect ALL violations (better UX: show user everything wrong at once)
        public List<ValidationResult> validateAll(Expense expense) {
            List<ValidationResult> failures = rules.stream()
                .map(r -> r.validate(expense))
                .filter(r -> !r.passed)
                .collect(java.util.stream.Collectors.toList());
            return failures.isEmpty()
                ? List.of(ValidationResult.ok("ALL_RULES"))
                : failures;
        }

        // Validate a batch; returns map of expense-id → list of results
        public Map<String, List<ValidationResult>> validateBatch(List<Expense> expenses) {
            Map<String, List<ValidationResult>> report = new LinkedHashMap<>();
            for (Expense e : expenses) report.put(e.id, validateAll(e));
            return report;
        }

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
