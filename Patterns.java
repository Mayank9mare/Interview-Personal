import java.util.*;

public class Patterns {

    public static void main(String[] args) {
        System.out.println("=== Creational ===");
        singletonDemo();
        factoryDemo();
        abstractFactoryDemo();
        builderDemo();
        prototypeDemo();

        System.out.println("\n=== Structural ===");
        adapterDemo();
        decoratorDemo();
        facadeDemo();
        proxyDemo();
        compositeDemo();

        System.out.println("\n=== Behavioral ===");
        observerDemo();
        strategyDemo();
        commandDemo();
        templateMethodDemo();
        stateDemo();
        chainOfResponsibilityDemo();
        iteratorDemo();
    }

    // =========================================================================
    // CREATIONAL — how objects are created
    // =========================================================================

    // ─────────────────────────────────────────────────────────────────────────
    // Singleton
    // Ensures only ONE instance exists globally.
    // Use when: config manager, logger, connection pool, rate limiter.
    // Pitfall: hard to unit test (global state). Use dependency injection instead
    //          in production; singleton shines in interview LLD (e.g. ParkingLot).
    // ─────────────────────────────────────────────────────────────────────────
    static class ConfigManager {
        private static volatile ConfigManager instance;  // volatile for double-check
        private final Map<String, String> config = new HashMap<>();

        private ConfigManager() { config.put("env", "prod"); }

        // Thread-safe double-checked locking
        public static ConfigManager getInstance() {
            if (instance == null) {
                synchronized (ConfigManager.class) {
                    if (instance == null) instance = new ConfigManager();
                }
            }
            return instance;
        }

        public String get(String key) { return config.getOrDefault(key, ""); }
    }

    static void singletonDemo() {
        ConfigManager a = ConfigManager.getInstance();
        ConfigManager b = ConfigManager.getInstance();
        System.out.println("Singleton same instance: " + (a == b)); // true
        System.out.println("Config env: " + a.get("env"));          // prod
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory Method
    // Defines an interface for creating objects; subclasses decide which class
    // to instantiate. Decouples creation from usage.
    // Use when: you don't know upfront which class to instantiate,
    //           or you want subclasses to control creation (e.g. notification types).
    // ─────────────────────────────────────────────────────────────────────────
    interface Notification {
        void send(String message);
    }
    static class EmailNotification  implements Notification { public void send(String m) { System.out.println("Email: " + m);  } }
    static class SMSNotification    implements Notification { public void send(String m) { System.out.println("SMS: " + m);    } }
    static class PushNotification   implements Notification { public void send(String m) { System.out.println("Push: " + m);   } }

    static Notification createNotification(String type) {
        switch (type) {
            case "EMAIL": return new EmailNotification();
            case "SMS":   return new SMSNotification();
            default:      return new PushNotification();
        }
    }

    static void factoryDemo() {
        createNotification("EMAIL").send("Hello");  // Email: Hello
        createNotification("SMS").send("Hello");    // SMS: Hello
        createNotification("PUSH").send("Hello");   // Push: Hello
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Abstract Factory
    // Factory of factories — creates families of related objects.
    // Use when: UI toolkit (Windows vs Mac buttons+checkboxes), DB drivers.
    // ─────────────────────────────────────────────────────────────────────────
    interface Button   { void render(); }
    interface Checkbox { void render(); }

    static class WinButton   implements Button   { public void render() { System.out.println("Windows Button");   } }
    static class WinCheckbox implements Checkbox { public void render() { System.out.println("Windows Checkbox"); } }
    static class MacButton   implements Button   { public void render() { System.out.println("Mac Button");       } }
    static class MacCheckbox implements Checkbox { public void render() { System.out.println("Mac Checkbox");     } }

    interface UIFactory {
        Button   createButton();
        Checkbox createCheckbox();
    }
    static class WindowsFactory implements UIFactory {
        public Button   createButton()   { return new WinButton();   }
        public Checkbox createCheckbox() { return new WinCheckbox(); }
    }
    static class MacFactory implements UIFactory {
        public Button   createButton()   { return new MacButton();   }
        public Checkbox createCheckbox() { return new MacCheckbox(); }
    }

    static void abstractFactoryDemo() {
        UIFactory factory = new MacFactory();      // swap to WindowsFactory for other OS
        factory.createButton().render();           // Mac Button
        factory.createCheckbox().render();         // Mac Checkbox
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Builder
    // Constructs complex objects step by step.
    // Use when: object has many optional fields, telescoping constructors are ugly.
    //           Common in: HTTP requests, query builders, test data setup.
    // ─────────────────────────────────────────────────────────────────────────
    static class Pizza {
        private final String size, crust, sauce;
        private final List<String> toppings;

        private Pizza(Builder b) { size=b.size; crust=b.crust; sauce=b.sauce; toppings=b.toppings; }

        public String toString() { return size + " pizza, " + crust + " crust, " + sauce + ", toppings: " + toppings; }

        static class Builder {
            private String size, crust = "thin", sauce = "tomato";
            private List<String> toppings = new ArrayList<>();

            Builder(String size)               { this.size = size; }
            Builder crust(String crust)        { this.crust = crust; return this; }
            Builder sauce(String sauce)        { this.sauce = sauce; return this; }
            Builder topping(String topping)    { this.toppings.add(topping); return this; }
            Pizza build()                      { return new Pizza(this); }
        }
    }

    static void builderDemo() {
        Pizza pizza = new Pizza.Builder("large")
                .crust("stuffed")
                .sauce("bbq")
                .topping("cheese")
                .topping("mushroom")
                .build();
        System.out.println("Builder: " + pizza);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Prototype
    // Creates new objects by cloning an existing one.
    // Use when: object creation is expensive (DB fetch, heavy init);
    //           clone and tweak instead of building from scratch.
    // ─────────────────────────────────────────────────────────────────────────
    static class UserProfile implements Cloneable {
        String name; List<String> roles;

        UserProfile(String name, List<String> roles) { this.name = name; this.roles = new ArrayList<>(roles); }

        @Override
        public UserProfile clone() {
            return new UserProfile(name, new ArrayList<>(roles)); // deep copy
        }
    }

    static void prototypeDemo() {
        UserProfile original = new UserProfile("alice", Arrays.asList("admin", "user"));
        UserProfile copy = original.clone();
        copy.name = "bob";
        copy.roles.remove("admin");
        System.out.println("Prototype original: " + original.name + " " + original.roles); // alice [admin, user]
        System.out.println("Prototype copy:     " + copy.name + " " + copy.roles);          // bob [user]
    }

    // =========================================================================
    // STRUCTURAL — how objects are composed
    // =========================================================================

    // ─────────────────────────────────────────────────────────────────────────
    // Adapter
    // Converts an incompatible interface into one the client expects.
    // Use when: integrating third-party or legacy code without modifying it.
    // ─────────────────────────────────────────────────────────────────────────
    static class LegacyPayment {
        public void makePayment(double amount) { System.out.println("Legacy payment: $" + amount); }
    }
    interface PaymentProcessor { void pay(int amountCents); }

    static class PaymentAdapter implements PaymentProcessor {
        private final LegacyPayment legacy;
        PaymentAdapter(LegacyPayment l) { this.legacy = l; }
        public void pay(int cents) { legacy.makePayment(cents / 100.0); } // adapt cents → dollars
    }

    static void adapterDemo() {
        PaymentProcessor processor = new PaymentAdapter(new LegacyPayment());
        processor.pay(1999); // Adapter converts 1999 cents → $19.99
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Decorator
    // Adds behaviour to an object dynamically without changing its class.
    // Use when: logging, caching, auth wrapping, compression — layerable concerns.
    //           Prefer over subclassing when combinations would explode classes.
    // ─────────────────────────────────────────────────────────────────────────
    interface Coffee { String description(); int cost(); }

    static class SimpleCoffee implements Coffee {
        public String description() { return "Coffee"; }
        public int cost() { return 100; }
    }
    static class MilkDecorator implements Coffee {
        private final Coffee c;
        MilkDecorator(Coffee c) { this.c = c; }
        public String description() { return c.description() + ", Milk"; }
        public int cost() { return c.cost() + 20; }
    }
    static class SugarDecorator implements Coffee {
        private final Coffee c;
        SugarDecorator(Coffee c) { this.c = c; }
        public String description() { return c.description() + ", Sugar"; }
        public int cost() { return c.cost() + 10; }
    }

    static void decoratorDemo() {
        Coffee coffee = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));
        System.out.println("Decorator: " + coffee.description() + " = " + coffee.cost() + "¢");
        // Coffee, Milk, Sugar = 130¢
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Facade
    // Provides a simplified interface to a complex subsystem.
    // Use when: hiding complexity of subsystems (e.g. home theater, compiler).
    // ─────────────────────────────────────────────────────────────────────────
    static class DVDPlayer  { void on() { System.out.println("DVD on"); }  void play() { System.out.println("DVD play"); } }
    static class Projector  { void on() { System.out.println("Projector on"); } }
    static class SoundSystem{ void on() { System.out.println("Sound on"); } void setVolume(int v) { System.out.println("Volume " + v); } }

    static class HomeTheaterFacade {
        private final DVDPlayer dvd; private final Projector proj; private final SoundSystem sound;
        HomeTheaterFacade(DVDPlayer d, Projector p, SoundSystem s) { dvd=d; proj=p; sound=s; }

        void watchMovie() {          // single call hides 5 subsystem calls
            proj.on(); sound.on(); sound.setVolume(10); dvd.on(); dvd.play();
        }
    }

    static void facadeDemo() {
        new HomeTheaterFacade(new DVDPlayer(), new Projector(), new SoundSystem()).watchMovie();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Proxy
    // Controls access to another object (lazy init, caching, access control).
    // Use when: virtual proxy (lazy load), protection proxy (auth check),
    //           caching proxy (memoize expensive calls).
    // ─────────────────────────────────────────────────────────────────────────
    interface ImageLoader { void display(); }

    static class RealImage implements ImageLoader {
        private final String file;
        RealImage(String file) { this.file = file; System.out.println("Loading " + file); }
        public void display() { System.out.println("Displaying " + file); }
    }
    static class ImageProxy implements ImageLoader {
        private final String file; private RealImage real;
        ImageProxy(String file) { this.file = file; }
        public void display() {
            if (real == null) real = new RealImage(file); // lazy load
            real.display();
        }
    }

    static void proxyDemo() {
        ImageLoader img = new ImageProxy("photo.jpg"); // no load yet
        img.display(); // loads + displays
        img.display(); // only displays (already loaded)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Composite
    // Treats individual objects and compositions uniformly (tree structure).
    // Use when: file system (file + folder), org chart, UI component tree.
    // ─────────────────────────────────────────────────────────────────────────
    interface FileSystemItem { void print(String indent); }

    static class File implements FileSystemItem {
        private final String name;
        File(String name) { this.name = name; }
        public void print(String indent) { System.out.println(indent + "- " + name); }
    }
    static class Folder implements FileSystemItem {
        private final String name;
        private final List<FileSystemItem> children = new ArrayList<>();
        Folder(String name) { this.name = name; }
        void add(FileSystemItem item) { children.add(item); }
        public void print(String indent) {
            System.out.println(indent + "+ " + name);
            for (FileSystemItem c : children) c.print(indent + "  ");
        }
    }

    static void compositeDemo() {
        Folder root = new Folder("root");
        Folder src  = new Folder("src");
        src.add(new File("Main.java"));
        src.add(new File("Utils.java"));
        root.add(src);
        root.add(new File("README.md"));
        root.print("");
        // + root
        //   + src
        //     - Main.java
        //     - Utils.java
        //   - README.md
    }

    // =========================================================================
    // BEHAVIORAL — how objects communicate
    // =========================================================================

    // ─────────────────────────────────────────────────────────────────────────
    // Observer (Pub/Sub)
    // Notifies multiple objects when one object changes state.
    // Use when: event systems, UI listeners, stock tickers, notification services.
    // ─────────────────────────────────────────────────────────────────────────
    interface Observer { void update(String event, Object data); }

    static class EventBus {
        private final Map<String, List<Observer>> listeners = new HashMap<>();

        void subscribe(String event, Observer o) {
            listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(o);
        }
        void unsubscribe(String event, Observer o) {
            listeners.getOrDefault(event, Collections.emptyList()).remove(o);
        }
        void publish(String event, Object data) {
            for (Observer o : listeners.getOrDefault(event, Collections.emptyList()))
                o.update(event, data);
        }
    }

    static void observerDemo() {
        EventBus bus = new EventBus();
        bus.subscribe("order.placed", (e, d) -> System.out.println("Email: " + d));
        bus.subscribe("order.placed", (e, d) -> System.out.println("SMS: "   + d));
        bus.publish("order.placed", "Order #42");
        // Email: Order #42
        // SMS:   Order #42
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Strategy
    // Defines a family of algorithms, encapsulates each, makes them swappable.
    // Use when: sorting strategies, payment methods, routing algorithms,
    //           discount rules — pick behaviour at runtime.
    // ─────────────────────────────────────────────────────────────────────────
    interface SortStrategy { void sort(int[] arr); }

    static class BubbleSortStrategy implements SortStrategy {
        public void sort(int[] arr) {
            for (int i=0;i<arr.length-1;i++)
                for (int j=0;j<arr.length-1-i;j++)
                    if (arr[j]>arr[j+1]) { int t=arr[j]; arr[j]=arr[j+1]; arr[j+1]=t; }
        }
    }
    static class BuiltInSortStrategy implements SortStrategy {
        public void sort(int[] arr) { Arrays.sort(arr); }
    }

    static class Sorter {
        private SortStrategy strategy;
        Sorter(SortStrategy s) { this.strategy = s; }
        void setStrategy(SortStrategy s) { this.strategy = s; }
        void sort(int[] arr) { strategy.sort(arr); }
    }

    static void strategyDemo() {
        int[] arr = {5, 3, 8, 1};
        Sorter sorter = new Sorter(new BubbleSortStrategy());
        sorter.sort(arr);
        System.out.println("Strategy (bubble): " + Arrays.toString(arr));

        int[] arr2 = {5, 3, 8, 1};
        sorter.setStrategy(new BuiltInSortStrategy());
        sorter.sort(arr2);
        System.out.println("Strategy (builtin): " + Arrays.toString(arr2));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Command
    // Encapsulates a request as an object — enables undo/redo, queuing, logging.
    // Use when: text editor undo/redo, transaction rollback, task queue.
    // ─────────────────────────────────────────────────────────────────────────
    interface Command { void execute(); void undo(); }

    static class TextBuffer {
        StringBuilder text = new StringBuilder();
        void append(String s) { text.append(s); }
        void deleteLast(int n) { text.delete(text.length() - n, text.length()); }
        public String toString() { return text.toString(); }
    }
    static class AppendCommand implements Command {
        private final TextBuffer buf; private final String text;
        AppendCommand(TextBuffer b, String t) { buf=b; text=t; }
        public void execute() { buf.append(text); }
        public void undo()    { buf.deleteLast(text.length()); }
    }

    static void commandDemo() {
        TextBuffer buf = new TextBuffer();
        Deque<Command> history = new ArrayDeque<>();

        Command c1 = new AppendCommand(buf, "Hello");
        Command c2 = new AppendCommand(buf, " World");
        c1.execute(); history.push(c1);
        c2.execute(); history.push(c2);
        System.out.println("Command after: " + buf);        // Hello World

        history.pop().undo();
        System.out.println("Command undo: "  + buf);        // Hello
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Template Method
    // Defines skeleton of algorithm in base class; subclasses fill in steps.
    // Use when: parsing pipeline, game loop, report generation — fixed structure,
    //           variable steps.
    // ─────────────────────────────────────────────────────────────────────────
    static abstract class DataProcessor {
        // template method — final so subclasses can't change the skeleton
        final void process() { readData(); processData(); writeData(); }
        abstract void readData();
        abstract void processData();
        void writeData() { System.out.println("Writing to DB"); } // default step
    }
    static class CSVProcessor extends DataProcessor {
        void readData()    { System.out.println("Reading CSV"); }
        void processData() { System.out.println("Parsing CSV rows"); }
    }
    static class JSONProcessor extends DataProcessor {
        void readData()    { System.out.println("Reading JSON"); }
        void processData() { System.out.println("Parsing JSON nodes"); }
        @Override void writeData() { System.out.println("Writing to S3"); } // override step
    }

    static void templateMethodDemo() {
        System.out.println("-- CSV --");  new CSVProcessor().process();
        System.out.println("-- JSON --"); new JSONProcessor().process();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // Lets an object alter its behaviour when its internal state changes.
    // Use when: vending machine, traffic light, order lifecycle, ATM.
    // ─────────────────────────────────────────────────────────────────────────
    interface OrderState { void next(OrderContext ctx); String name(); }

    static class OrderContext {
        private OrderState state = new PlacedState();
        void next()             { state.next(this); }
        void setState(OrderState s) { this.state = s; }
        String getState()       { return state.name(); }
    }
    static class PlacedState   implements OrderState { public void next(OrderContext c) { c.setState(new ShippedState()); }  public String name() { return "PLACED"; }   }
    static class ShippedState  implements OrderState { public void next(OrderContext c) { c.setState(new DeliveredState()); } public String name() { return "SHIPPED"; }  }
    static class DeliveredState implements OrderState { public void next(OrderContext c) { System.out.println("Already delivered"); } public String name() { return "DELIVERED"; } }

    static void stateDemo() {
        OrderContext order = new OrderContext();
        System.out.println("State: " + order.getState()); // PLACED
        order.next();
        System.out.println("State: " + order.getState()); // SHIPPED
        order.next();
        System.out.println("State: " + order.getState()); // DELIVERED
        order.next();                                      // Already delivered
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chain of Responsibility
    // Passes a request along a chain of handlers; each decides to handle or pass.
    // Use when: middleware pipeline, logging levels, approval workflows,
    //           HTTP request filters.
    // ─────────────────────────────────────────────────────────────────────────
    static abstract class AuthHandler {
        protected AuthHandler next;
        AuthHandler setNext(AuthHandler h) { next = h; return h; }
        abstract boolean handle(String role, String action);
    }
    static class AuthenticationHandler extends AuthHandler {
        public boolean handle(String role, String action) {
            if (role == null) { System.out.println("Rejected: not authenticated"); return false; }
            return next == null || next.handle(role, action);
        }
    }
    static class AuthorizationHandler extends AuthHandler {
        public boolean handle(String role, String action) {
            if (action.equals("DELETE") && !role.equals("admin")) {
                System.out.println("Rejected: only admin can DELETE"); return false;
            }
            return next == null || next.handle(role, action);
        }
    }
    static class LoggingHandler extends AuthHandler {
        public boolean handle(String role, String action) {
            System.out.println("Allowed: " + role + " -> " + action);
            return next == null || next.handle(role, action);
        }
    }

    static void chainOfResponsibilityDemo() {
        AuthHandler chain = new AuthenticationHandler();
        chain.setNext(new AuthorizationHandler()).setNext(new LoggingHandler());

        chain.handle("user",  "READ");   // Allowed: user -> READ
        chain.handle("user",  "DELETE"); // Rejected: only admin can DELETE
        chain.handle("admin", "DELETE"); // Allowed: admin -> DELETE
        chain.handle(null,    "READ");   // Rejected: not authenticated
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Iterator
    // Provides a way to traverse a collection without exposing its internals.
    // Use when: custom data structure traversal (tree, graph, range).
    // ─────────────────────────────────────────────────────────────────────────
    static class Range implements Iterable<Integer> {
        private final int start, end, step;
        Range(int start, int end, int step) { this.start=start; this.end=end; this.step=step; }

        public Iterator<Integer> iterator() {
            return new Iterator<Integer>() {
                int current = start;
                public boolean hasNext() { return current < end; }
                public Integer next()    { int val = current; current += step; return val; }
            };
        }
    }

    static void iteratorDemo() {
        Range range = new Range(0, 10, 2); // 0,2,4,6,8
        List<Integer> result = new ArrayList<>();
        for (int n : range) result.add(n);
        System.out.println("Iterator range(0,10,2): " + result);
    }
}
