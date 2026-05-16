// Companies: Amazon
// Pizza pricing system — models a customisable pizza using the Decorator pattern.

import java.util.*;

/**
 * Pizza pricing system that models a customisable pizza order using the Decorator pattern.
 *
 * <p>A base pizza (e.g. Margherita, Farmhouse) has a fixed cost and description. Each
 * topping (e.g. Cheese, Pepperoni, Mushrooms) wraps the pizza, adding to its cost and
 * description without modifying the wrapped object. This allows arbitrary topping
 * combinations to be composed at runtime.
 *
 * <p>Key design decision: using Decorator over a flat-list approach means:
 * <ul>
 *   <li>Adding a new topping requires only a new class — no {@code if/else} chains.</li>
 *   <li>Double-toppings are naturally supported by wrapping twice.</li>
 *   <li>The cost calculation is a recursive chain — each decorator adds its own cost
 *       to {@code wrappee.cost()}.</li>
 * </ul>
 *
 * <p>Core invariant: {@code cost()} and {@code description()} always reflect the
 * complete chain from innermost base to outermost topping.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class PizzaPricing {

    // ── Component interface ───────────────────────────────────────────────

    /**
     * The base component interface implemented by both base pizzas and toppings.
     * Every node in the decorator chain implements this.
     */
    interface Pizza {
        /** @return total cost of this pizza with all current toppings */
        double cost();
        /** @return human-readable description of base + all toppings */
        String description();
    }

    // ── Concrete base pizzas ──────────────────────────────────────────────

    /**
     * Plain Margherita — the simplest base pizza.
     */
    static class Margherita implements Pizza {
        public double cost() { return 5.00; }
        public String description() { return "Margherita"; }
    }

    /**
     * Farmhouse base pizza — a richer base with more toppings baked in.
     */
    static class Farmhouse implements Pizza {
        public double cost() { return 7.00; }
        public String description() { return "Farmhouse"; }
    }

    // ── Abstract decorator ────────────────────────────────────────────────

    /**
     * Base class for all topping decorators. Wraps a {@link Pizza} and delegates
     * {@code cost()} and {@code description()} to it, letting subclasses add their own.
     *
     * <p>Subclasses must call {@code super(wrapped)} and override both methods.
     */
    static abstract class ToppingDecorator implements Pizza {
        /** The pizza being wrapped (could itself be a decorator). */
        protected final Pizza wrapped;

        /**
         * @param wrapped the pizza to decorate (must not be null)
         */
        ToppingDecorator(Pizza wrapped) { this.wrapped = wrapped; }
    }

    // ── Concrete toppings ─────────────────────────────────────────────────

    /** Extra cheese topping — adds $1.50. */
    static class ExtraCheese extends ToppingDecorator {
        ExtraCheese(Pizza wrapped) { super(wrapped); }
        public double cost() { return wrapped.cost() + 1.50; }
        public String description() { return wrapped.description() + " + ExtraCheese"; }
    }

    /** Pepperoni topping — adds $2.00. */
    static class Pepperoni extends ToppingDecorator {
        Pepperoni(Pizza wrapped) { super(wrapped); }
        public double cost() { return wrapped.cost() + 2.00; }
        public String description() { return wrapped.description() + " + Pepperoni"; }
    }

    /** Mushrooms topping — adds $1.00. */
    static class Mushrooms extends ToppingDecorator {
        Mushrooms(Pizza wrapped) { super(wrapped); }
        public double cost() { return wrapped.cost() + 1.00; }
        public String description() { return wrapped.description() + " + Mushrooms"; }
    }

    /** Jalapenos topping — adds $0.75. */
    static class Jalapenos extends ToppingDecorator {
        Jalapenos(Pizza wrapped) { super(wrapped); }
        public double cost() { return wrapped.cost() + 0.75; }
        public String description() { return wrapped.description() + " + Jalapenos"; }
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Plain base pizza
        Pizza p1 = new Margherita();
        System.out.printf("%s = $%.2f%n", p1.description(), p1.cost());
        // Expected: Margherita = $5.00

        // Margherita + ExtraCheese + Pepperoni
        Pizza p2 = new Pepperoni(new ExtraCheese(new Margherita()));
        System.out.printf("%s = $%.2f%n", p2.description(), p2.cost());
        // Expected: Margherita + ExtraCheese + Pepperoni = $8.50

        // Farmhouse + Mushrooms + Jalapenos + double ExtraCheese
        Pizza p3 = new ExtraCheese(new ExtraCheese(new Jalapenos(new Mushrooms(new Farmhouse()))));
        System.out.printf("%s = $%.2f%n", p3.description(), p3.cost());
        // Expected: Farmhouse + Mushrooms + Jalapenos + ExtraCheese + ExtraCheese = $11.75

        // Follow-up: adding a new topping (Olives) requires zero changes here
        Pizza p4 = new Margherita() {
            // Anonymous inline topping to simulate a new type without a new file
            public double cost() { return super.cost() + 0.50; }
            public String description() { return super.description() + " + Olives"; }
        };
        System.out.printf("%s = $%.2f%n", p4.description(), p4.cost());
        // Expected: Margherita + Olives = $5.50
    }
}
