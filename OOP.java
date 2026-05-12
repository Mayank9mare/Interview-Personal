import java.util.*;

// OOP Pillars + SOLID Principles
// Each principle shows: concept → violation → fix
public class OOP {

    // ═════════════════════════════════════════════════════════════════════════
    // PILLAR 1 — Encapsulation
    //   Bundle state + behavior into a unit; expose only what callers need.
    //   Prevents invalid state; lets you change internals without breaking callers.
    // ═════════════════════════════════════════════════════════════════════════
    static class BankAccount {
        private double balance; // private — no direct access

        public void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Must be positive");
            balance += amount;
        }
        public boolean withdraw(double amount) {
            if (amount > 0 && balance >= amount) { balance -= amount; return true; }
            return false;
        }
        public double getBalance() { return balance; } // read-only access
        // no setBalance() — callers can't set arbitrary values
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PILLAR 2 — Inheritance
    //   Model IS-A relationships; reuse behavior via parent class.
    //   Rule: only extend when subtype truly IS-A supertype everywhere.
    // ═════════════════════════════════════════════════════════════════════════
    static abstract class Animal {
        String name;
        Animal(String name) { this.name = name; }
        abstract String sound();               // must override
        void breathe() { System.out.println(name + " breathes"); } // shared behavior
    }
    static class Dog extends Animal {
        Dog(String name) { super(name); }
        @Override String sound() { return "Woof"; }
        void fetch() { System.out.println(name + " fetches!"); } // Dog-specific
    }
    static class Cat extends Animal {
        Cat(String name) { super(name); }
        @Override String sound() { return "Meow"; }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PILLAR 3 — Polymorphism
    //   Runtime (dynamic dispatch): overriding — resolved at runtime
    //   Compile-time (static):      overloading — resolved at compile time
    // ═════════════════════════════════════════════════════════════════════════
    static void makeSound(Animal a) { System.out.println(a.sound()); } // polymorphic

    // Overloading — same name, different signature
    static int    add(int a, int b)       { return a + b; }
    static double add(double a, double b) { return a + b; }
    static String add(String a, String b) { return a + b; }

    // ═════════════════════════════════════════════════════════════════════════
    // PILLAR 4 — Abstraction
    //   Expose WHAT, hide HOW. Interface = contract; implementation = detail.
    //   Abstract class: when you want shared state or partial implementation.
    //   Interface: when you want pure contract (multiple inheritance of type).
    // ═════════════════════════════════════════════════════════════════════════
    interface PaymentProcessor {
        boolean process(double amount);
        default String currency() { return "USD"; } // Java 8 default
    }
    static class StripeProcessor implements PaymentProcessor {
        public boolean process(double amount) {
            System.out.println("Stripe: $" + amount + " " + currency()); return true;
        }
    }
    static class PayPalProcessor implements PaymentProcessor {
        public boolean process(double amount) {
            System.out.println("PayPal: $" + amount + " " + currency()); return true;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // S — Single Responsibility Principle
    //   A class should have ONE reason to change.
    //   Symptom of violation: class name ends in "Manager", "Util", "Helper";
    //   class does IO, business logic, AND formatting.
    // ═════════════════════════════════════════════════════════════════════════

    // VIOLATION — UserService does everything
    static class UserServiceBad {
        void register(String email, String pw) { /* validate */ }
        void sendWelcomeEmail(String email)    { /* SMTP logic */ }  // ← email concern
        void saveToDb(String email)            { /* SQL queries */ } // ← persistence concern
        String toJson(String email)            { return "{\"email\":\""+email+"\"}"; } // ← serialization
        // 4 reasons to change: validation rules, email provider, DB schema, JSON format
    }

    // FIX — one class per concern
    static class UserValidator   { boolean isValid(String e, String p) { return e.contains("@") && p.length() >= 8; } }
    static class UserRepository  { void save(String e)      { System.out.println("DB: saved " + e); } }
    static class EmailService    { void sendWelcome(String e){ System.out.println("Email → " + e); } }
    static class UserService {
        private final UserValidator  v  = new UserValidator();
        private final UserRepository r  = new UserRepository();
        private final EmailService   es = new EmailService();
        void register(String email, String pw) {
            if (!v.isValid(email, pw)) throw new IllegalArgumentException("Invalid credentials");
            r.save(email);
            es.sendWelcome(email);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // O — Open/Closed Principle
    //   Open for extension, closed for modification.
    //   Symptom: every new type requires editing existing code (switch/if-else chain).
    // ═════════════════════════════════════════════════════════════════════════

    // VIOLATION — adding new shape requires editing this method
    static double areaBad(String type, double a, double b) {
        return switch (type) {
            case "circle"    -> Math.PI * a * a;
            case "rectangle" -> a * b;
            // must keep editing for every new shape
            default          -> 0;
        };
    }

    // FIX — each shape knows its own area; no existing code changes
    interface Shape { double area(); }
    record Circle(double r)           implements Shape { public double area() { return Math.PI * r * r; } }
    record Rect(double w, double h)   implements Shape { public double area() { return w * h; } }
    record Triangle(double b, double h) implements Shape { public double area() { return 0.5 * b * h; } }
    // Adding Hexagon = new class only; nothing else changes
    static double totalArea(List<Shape> shapes) { return shapes.stream().mapToDouble(Shape::area).sum(); }

    // ═════════════════════════════════════════════════════════════════════════
    // L — Liskov Substitution Principle
    //   Subtypes must be substitutable for their base type without breaking behavior.
    //   Rule: subclass must honor the POSTCONDITIONS of the parent.
    //   Classic violation: Square extends Rectangle (setting width changes height).
    // ═════════════════════════════════════════════════════════════════════════

    // VIOLATION — Square breaks the Rectangle contract
    static class Rectangle {
        protected int w, h;
        void setWidth(int w) { this.w = w; }
        void setHeight(int h) { this.h = h; }
        int area() { return w * h; }
    }
    static class SquareBad extends Rectangle {
        @Override void setWidth(int w)  { this.w = this.h = w; } // ← side effect!
        @Override void setHeight(int h) { this.w = this.h = h; } // caller expects independent dims
    }
    // Test that breaks: r.setWidth(5); r.setHeight(4); assert r.area()==20 → fails for SquareBad (gives 16)

    // FIX — neither extends the other; share an interface
    interface Quadrilateral { int area(); }
    record RectFix(int w, int h) implements Quadrilateral { public int area() { return w * h; } }
    record SquareFix(int s)      implements Quadrilateral { public int area() { return s * s; } }

    // ═════════════════════════════════════════════════════════════════════════
    // I — Interface Segregation Principle
    //   Clients should not be forced to implement methods they don't use.
    //   Symptom: class throws UnsupportedOperationException for some methods.
    // ═════════════════════════════════════════════════════════════════════════

    // VIOLATION — Robot must implement eat/sleep it doesn't need
    interface WorkerBad {
        void work();
        void eat();   // robots don't eat
        void sleep(); // robots don't sleep
    }

    // FIX — small, focused interfaces; implement only what applies
    interface Workable  { void work(); }
    interface Eatable   { void eat();  }
    interface Sleepable { void sleep(); }

    static class HumanWorker implements Workable, Eatable, Sleepable {
        public void work()  { System.out.println("Human works");  }
        public void eat()   { System.out.println("Human eats");   }
        public void sleep() { System.out.println("Human sleeps"); }
    }
    static class RobotWorker implements Workable {
        public void work() { System.out.println("Robot works"); }
        // no eat/sleep — interface doesn't force it
    }

    // ═════════════════════════════════════════════════════════════════════════
    // D — Dependency Inversion Principle
    //   High-level modules should not depend on low-level modules.
    //   Both should depend on abstractions.
    //   Symptom: high-level class directly instantiates a concrete low-level class.
    //   Benefit: easy to swap implementations (MySQL ↔ Postgres ↔ InMemory for tests).
    // ═════════════════════════════════════════════════════════════════════════

    // VIOLATION — OrderService hardcodes its dependency
    static class MySQLDatabase { void save(String d) { System.out.println("MySQL: " + d); } }
    static class OrderServiceBad {
        private final MySQLDatabase db = new MySQLDatabase(); // new = hard coupling
        void placeOrder(String item) { db.save(item); }
        // can't test without MySQL; can't switch to Postgres without editing this class
    }

    // FIX — depend on abstraction; inject the implementation
    interface Database { void save(String data); }
    static class MySQLDb    implements Database { public void save(String d) { System.out.println("MySQL: " + d); } }
    static class PostgresDb implements Database { public void save(String d) { System.out.println("Postgres: " + d); } }
    static class InMemoryDb implements Database { // lightweight stand-in for unit tests
        List<String> store = new ArrayList<>();
        public void save(String d) { store.add(d); System.out.println("InMemory: " + d); }
    }
    static class OrderService {
        private final Database db;
        OrderService(Database db) { this.db = db; } // constructor injection
        void placeOrder(String item) { db.save(item); }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Demo
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        System.out.println("=== Encapsulation ===");
        BankAccount acc = new BankAccount();
        acc.deposit(100); acc.withdraw(30);
        System.out.println("Balance: " + acc.getBalance()); // 70.0

        System.out.println("\n=== Inheritance ===");
        Dog dog = new Dog("Rex");
        dog.breathe(); dog.fetch();

        System.out.println("\n=== Polymorphism ===");
        List.of(new Dog("A"), new Cat("B")).forEach(OOP::makeSound); // Woof, Meow
        System.out.println(add(1, 2) + " / " + add(1.5, 2.5) + " / " + add("foo", "bar"));

        System.out.println("\n=== Abstraction ===");
        List.<PaymentProcessor>of(new StripeProcessor(), new PayPalProcessor())
            .forEach(p -> p.process(9.99));

        System.out.println("\n=== S: Single Responsibility ===");
        new UserService().register("alice@example.com", "password1");

        System.out.println("\n=== O: Open/Closed ===");
        List<Shape> shapes = List.of(new Circle(5), new Rect(3, 4), new Triangle(6, 8));
        System.out.printf("Total area: %.2f%n", totalArea(shapes)); // ~102.54

        System.out.println("\n=== L: Liskov Substitution ===");
        Quadrilateral r = new RectFix(5, 4);
        Quadrilateral s = new SquareFix(5);
        System.out.println("Rect area: " + r.area());   // 20
        System.out.println("Square area: " + s.area()); // 25
        // Both usable as Quadrilateral without surprises

        System.out.println("\n=== I: Interface Segregation ===");
        new HumanWorker().work();
        new RobotWorker().work();

        System.out.println("\n=== D: Dependency Inversion ===");
        new OrderService(new MySQLDb()).placeOrder("widget");
        new OrderService(new PostgresDb()).placeOrder("gadget");
        InMemoryDb testDb = new InMemoryDb();
        new OrderService(testDb).placeOrder("test-item");
        System.out.println("Test DB contains: " + testDb.store);
    }
}
