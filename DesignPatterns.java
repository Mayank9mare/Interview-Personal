import java.util.*;
import java.util.function.*;

// Design Patterns — GoF: Creational | Structural | Behavioral
// Each section: concept → when to use → code
public class DesignPatterns {

    // ═════════════════════════════════════════════════════════════════════════
    // ── CREATIONAL PATTERNS ──────────────────────────────────────────────────
    // Control how objects are created
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // Singleton — exactly ONE instance, globally accessible
    //   Use: config, logger, connection pool, thread pool
    //   Pitfall: hides dependencies; hard to test without DI → prefer injecting it
    //
    //   Option A — Double-Checked Locking (lazy, thread-safe)
    //   Option B — Enum Singleton (Bill Pugh; serialization-safe, reflection-safe)
    // ─────────────────────────────────────────────────────────────────────────
    static class Config {
        private static volatile Config instance;  // volatile: prevents reorder
        private final Map<String, String> props = new HashMap<>();

        private Config() { props.put("env", "prod"); }  // private constructor

        public static Config getInstance() {
            if (instance == null) {                        // first check (no lock)
                synchronized (Config.class) {
                    if (instance == null) instance = new Config(); // second check (locked)
                }
            }
            return instance;
        }
        public String get(String key) { return props.getOrDefault(key, ""); }
    }

    enum AppLogger {               // enum variant — simplest, safest singleton
        INSTANCE;
        public void log(String msg) { System.out.println("[LOG] " + msg); }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory Method — subclasses decide which object to create
    //   Use: when the caller should not know the concrete type;
    //        when new product types are added frequently
    //   vs. static factory: Factory Method is polymorphic (override per subclass)
    // ─────────────────────────────────────────────────────────────────────────
    interface Notification {
        void send(String message);
    }
    static class EmailNotif implements Notification {
        public void send(String m) { System.out.println("Email: " + m); }
    }
    static class SMSNotif implements Notification {
        public void send(String m) { System.out.println("SMS: " + m); }
    }
    static class PushNotif implements Notification {
        public void send(String m) { System.out.println("Push: " + m); }
    }

    // Abstract creator — subclasses override factory method
    static abstract class NotificationService {
        abstract Notification createNotification();     // ← factory method

        public void notify(String msg) {
            Notification n = createNotification();      // call subclass impl
            n.send(msg);
        }
    }
    static class EmailService  extends NotificationService { Notification createNotification() { return new EmailNotif(); } }
    static class SMSService    extends NotificationService { Notification createNotification() { return new SMSNotif(); } }

    // Simple factory (not a GoF pattern, but common in interviews)
    static Notification notificationOf(String type) {
        return switch (type) {
            case "email" -> new EmailNotif();
            case "sms"   -> new SMSNotif();
            case "push"  -> new PushNotif();
            default      -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Abstract Factory — factory of factories; create families of related objects
    //   Use: UI toolkits (Light/Dark theme), cross-platform widgets, test vs prod infra
    //   Key insight: swap the factory → entire product family swaps
    // ─────────────────────────────────────────────────────────────────────────
    interface Button  { void render(); }
    interface Checkbox { void render(); }

    static class LightButton  implements Button   { public void render() { System.out.println("Light Button");   } }
    static class LightCheckbox implements Checkbox { public void render() { System.out.println("Light Checkbox"); } }
    static class DarkButton   implements Button   { public void render() { System.out.println("Dark Button");    } }
    static class DarkCheckbox implements Checkbox { public void render() { System.out.println("Dark Checkbox");  } }

    interface UIFactory {           // abstract factory
        Button createButton();
        Checkbox createCheckbox();
    }
    static class LightThemeFactory implements UIFactory {
        public Button createButton()    { return new LightButton(); }
        public Checkbox createCheckbox(){ return new LightCheckbox(); }
    }
    static class DarkThemeFactory implements UIFactory {
        public Button createButton()    { return new DarkButton(); }
        public Checkbox createCheckbox(){ return new DarkCheckbox(); }
    }

    static void renderUI(UIFactory factory) {
        factory.createButton().render();
        factory.createCheckbox().render();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Builder — construct complex objects step by step
    //   Use: when constructor has many optional parameters (avoid telescoping ctors)
    //   Key: builder holds mutable state; build() returns immutable product
    //   Spot: Pizza, HttpRequest, QueryBuilder, SqlQuery
    // ─────────────────────────────────────────────────────────────────────────
    static class HttpRequest {
        private final String method, url;
        private final Map<String, String> headers;
        private final String body;

        private HttpRequest(Builder b) {
            method  = b.method;
            url     = b.url;
            headers = Collections.unmodifiableMap(b.headers);
            body    = b.body;
        }

        @Override public String toString() {
            return method + " " + url + " headers=" + headers + " body=" + body;
        }

        static class Builder {
            private final String method, url;         // required
            private final Map<String, String> headers = new HashMap<>();
            private String body = "";                 // optional

            Builder(String method, String url) { this.method = method; this.url = url; }

            Builder header(String k, String v) { headers.put(k, v); return this; }
            Builder body(String b) { body = b; return this; }
            HttpRequest build() { return new HttpRequest(this); }
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // ── STRUCTURAL PATTERNS ──────────────────────────────────────────────────
    // Compose objects/classes into larger structures
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // Adapter — convert an incompatible interface into the one callers expect
    //   Use: integrating third-party libraries, legacy code, plug-in APIs
    //   Two flavors: Object Adapter (composition) and Class Adapter (inheritance)
    //   Prefer composition (Object Adapter) — more flexible
    // ─────────────────────────────────────────────────────────────────────────
    interface MediaPlayer { void play(String filename); }

    // legacy third-party class we cannot modify
    static class VLCPlayer {
        void playVLC(String f)  { System.out.println("VLC playing: " + f); }
    }
    static class MP4Player {
        void playMP4(String f)  { System.out.println("MP4 playing: " + f); }
    }

    // adapter wraps legacy objects to match MediaPlayer interface
    static class MediaAdapter implements MediaPlayer {
        private final VLCPlayer vlc;
        private final MP4Player mp4;
        MediaAdapter() { vlc = new VLCPlayer(); mp4 = new MP4Player(); }

        public void play(String f) {
            if (f.endsWith(".vlc")) vlc.playVLC(f);
            else if (f.endsWith(".mp4")) mp4.playMP4(f);
            else System.out.println("Unsupported: " + f);
        }
    }
    static class AudioPlayer implements MediaPlayer {
        private final MediaAdapter adapter = new MediaAdapter();
        public void play(String f) {
            if (f.endsWith(".mp3")) System.out.println("MP3 playing: " + f);
            else adapter.play(f);
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Decorator — add responsibilities to objects at RUNTIME without subclassing
    //   Use: I/O streams, logging wrappers, middleware, feature flags
    //   Key: decorator implements the same interface and wraps an existing object
    //   vs. inheritance: inheritance = compile-time; decorator = runtime, stackable
    // ─────────────────────────────────────────────────────────────────────────
    interface Coffee { String description(); double cost(); }

    static class SimpleCoffee implements Coffee {
        public String description() { return "Coffee"; }
        public double cost() { return 1.00; }
    }

    static abstract class CoffeeDecorator implements Coffee {
        protected final Coffee coffee;
        CoffeeDecorator(Coffee c) { coffee = c; }
    }
    static class Milk extends CoffeeDecorator {
        Milk(Coffee c) { super(c); }
        public String description() { return coffee.description() + ", Milk"; }
        public double cost() { return coffee.cost() + 0.25; }
    }
    static class Sugar extends CoffeeDecorator {
        Sugar(Coffee c) { super(c); }
        public String description() { return coffee.description() + ", Sugar"; }
        public double cost() { return coffee.cost() + 0.10; }
    }
    static class WhipCream extends CoffeeDecorator {
        WhipCream(Coffee c) { super(c); }
        public String description() { return coffee.description() + ", Whip"; }
        public double cost() { return coffee.cost() + 0.50; }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Composite — treat individual objects and compositions uniformly (tree)
    //   Use: file system (file=leaf, directory=composite), UI hierarchies,
    //        org charts, expression trees
    //   Key: component interface shared by both Leaf and Composite
    // ─────────────────────────────────────────────────────────────────────────
    interface FileSystemItem {
        String name();
        int size();
        void print(String indent);
    }

    static class File implements FileSystemItem {
        private final String name; private final int size;
        File(String n, int s) { name = n; size = s; }
        public String name() { return name; }
        public int size() { return size; }
        public void print(String indent) { System.out.println(indent + name + " (" + size + "b)"); }
    }

    static class Directory implements FileSystemItem {
        private final String name;
        private final List<FileSystemItem> children = new ArrayList<>();
        Directory(String n) { name = n; }
        public String name() { return name; }
        public void add(FileSystemItem item) { children.add(item); }
        public int size() { return children.stream().mapToInt(FileSystemItem::size).sum(); }
        public void print(String indent) {
            System.out.println(indent + name + "/");
            children.forEach(c -> c.print(indent + "  "));
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // ── BEHAVIORAL PATTERNS ──────────────────────────────────────────────────
    // Define how objects communicate and distribute responsibilities
    // ═════════════════════════════════════════════════════════════════════════

    // ─────────────────────────────────────────────────────────────────────────
    // Observer — one-to-many dependency; when subject changes, all observers notified
    //   Use: event systems, UI data binding, pub/sub, reactive streams
    //   Java built-in: PropertyChangeListener, Flow API (Java 9)
    // ─────────────────────────────────────────────────────────────────────────
    interface Observer { void update(String event, Object data); }

    static class EventBus {
        private final Map<String, List<Observer>> listeners = new HashMap<>();

        public void subscribe(String event, Observer o) {
            listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(o);
        }
        public void unsubscribe(String event, Observer o) {
            listeners.getOrDefault(event, List.of()).remove(o);
        }
        public void publish(String event, Object data) {
            listeners.getOrDefault(event, List.of()).forEach(o -> o.update(event, data));
        }
    }

    static class StockTicker {
        private final EventBus bus;
        private final String symbol;
        StockTicker(EventBus bus, String symbol) { this.bus = bus; this.symbol = symbol; }
        public void setPrice(double price) { bus.publish("price:" + symbol, price); }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Strategy — encapsulate interchangeable algorithms behind an interface
    //   Use: sorting strategies, payment processors, compression algorithms,
    //        route-finding (Uber!), discount rules
    //   vs. if/else chains: Strategy eliminates branching and is Open/Closed
    // ─────────────────────────────────────────────────────────────────────────
    interface RoutingStrategy { List<String> route(String from, String to); }

    static class FastestRoute implements RoutingStrategy {
        public List<String> route(String f, String t) {
            return List.of(f, "highway", t);
        }
    }
    static class ShortestRoute implements RoutingStrategy {
        public List<String> route(String f, String t) {
            return List.of(f, "local road", t);
        }
    }
    static class EcoRoute implements RoutingStrategy {
        public List<String> route(String f, String t) {
            return List.of(f, "bike lane", t);
        }
    }

    static class Navigator {
        private RoutingStrategy strategy;
        Navigator(RoutingStrategy s) { strategy = s; }
        void setStrategy(RoutingStrategy s) { strategy = s; }  // swap at runtime
        List<String> navigate(String from, String to) { return strategy.route(from, to); }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Command — encapsulate a request as an object; supports undo/redo and queuing
    //   Use: undo stacks (text editors), job queues, macro recording, transactions
    //   Key: Command stores receiver + parameters; invoker calls execute() without knowing impl
    // ─────────────────────────────────────────────────────────────────────────
    interface Command { void execute(); void undo(); }

    static class TextEditor {
        private final StringBuilder text = new StringBuilder();
        void write(String s) { text.append(s); }
        void delete(int n) { if (n <= text.length()) text.delete(text.length()-n, text.length()); }
        String getText() { return text.toString(); }
    }

    static class WriteCommand implements Command {
        private final TextEditor editor; private final String text;
        WriteCommand(TextEditor e, String t) { editor = e; text = t; }
        public void execute() { editor.write(text); }
        public void undo()    { editor.delete(text.length()); }
    }

    static class CommandHistory {
        private final Deque<Command> history = new ArrayDeque<>();
        public void execute(Command cmd) { cmd.execute(); history.push(cmd); }
        public void undo() { if (!history.isEmpty()) history.pop().undo(); }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Template Method — define the algorithm skeleton in a base class;
    //                   subclasses fill in specific steps
    //   Use: data processing pipelines, report generation, game loops
    //   vs. Strategy: Template Method uses inheritance; Strategy uses composition
    //   Prefer Strategy when you want runtime swapping; Template Method for fixed steps
    // ─────────────────────────────────────────────────────────────────────────
    static abstract class DataPipeline {
        // template method — fixed skeleton
        public final void process(List<String> data) {
            List<String> filtered = filter(data);    // step 1: hook
            List<String> transformed = transform(filtered); // step 2: hook
            save(transformed);                       // step 3: hook
        }
        protected abstract List<String> filter(List<String> data);
        protected abstract List<String> transform(List<String> data);
        protected void save(List<String> data) {     // default implementation
            System.out.println("Saving " + data.size() + " records: " + data);
        }
    }

    static class LogPipeline extends DataPipeline {
        protected List<String> filter(List<String> data) {
            return data.stream().filter(s -> s.contains("ERROR")).toList();
        }
        protected List<String> transform(List<String> data) {
            return data.stream().map(s -> "[" + s + "]").toList();
        }
    }
    static class MetricsPipeline extends DataPipeline {
        protected List<String> filter(List<String> data) { return data; } // no filter
        protected List<String> transform(List<String> data) {
            return data.stream().map(String::toUpperCase).toList();
        }
    }


    // ═════════════════════════════════════════════════════════════════════════
    // Pattern Quick-Reference
    // ═════════════════════════════════════════════════════════════════════════
    //
    //  Creational
    //    Singleton         1 instance, lazy or enum
    //    Factory Method    subclass decides what to create
    //    Abstract Factory  family of related objects, swap factory = swap family
    //    Builder           many optional params → fluent builder, immutable product
    //
    //  Structural
    //    Adapter           incompatible interface → wrap with adapter (prefer composition)
    //    Decorator         add behavior at runtime by wrapping (stackable)
    //    Composite         tree of same-type nodes (Leaf + Composite share interface)
    //    Proxy             (not shown) same interface, controls access/caching/lazy init
    //
    //  Behavioral
    //    Observer          subject notifies many listeners (event bus / pub-sub)
    //    Strategy          swap algorithm at runtime behind shared interface
    //    Command           encapsulate request as object; supports undo/queue
    //    Template Method   fixed skeleton in base class, hooks in subclasses
    //    Iterator          (Java Iterable) sequential access without exposing internals
    //
    //  Interview hint:
    //    "Adding features without editing existing code" → Strategy or Decorator
    //    "Object creation logic is complex or conditional" → Factory or Builder
    //    "Need undo / job queue" → Command
    //    "One change notifies many" → Observer
    //    "Legacy API doesn't match what we need" → Adapter
    //
    // ═════════════════════════════════════════════════════════════════════════

    public static void main(String[] args) {
        System.out.println("=== Singleton ===");
        Config.getInstance().get("env");
        System.out.println(Config.getInstance() == Config.getInstance()); // true
        AppLogger.INSTANCE.log("App started");

        System.out.println("\n=== Factory Method ===");
        new EmailService().notify("Welcome!");
        notificationOf("sms").send("Your code: 1234");

        System.out.println("\n=== Abstract Factory (Light theme) ===");
        renderUI(new LightThemeFactory());
        System.out.println("=== Abstract Factory (Dark theme) ===");
        renderUI(new DarkThemeFactory());

        System.out.println("\n=== Builder ===");
        HttpRequest req = new HttpRequest.Builder("POST", "https://api.example.com/order")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer token")
            .body("{\"item\":\"widget\"}")
            .build();
        System.out.println(req);

        System.out.println("\n=== Adapter ===");
        AudioPlayer player = new AudioPlayer();
        player.play("song.mp3");
        player.play("clip.vlc");
        player.play("video.mp4");

        System.out.println("\n=== Decorator ===");
        Coffee c = new WhipCream(new Sugar(new Milk(new SimpleCoffee())));
        System.out.println(c.description() + " $" + c.cost());

        System.out.println("\n=== Composite (file system) ===");
        Directory root = new Directory("root");
        Directory src = new Directory("src");
        src.add(new File("Main.java", 500));
        src.add(new File("Utils.java", 300));
        root.add(src);
        root.add(new File("README.md", 120));
        root.print("");
        System.out.println("Total size: " + root.size() + "b");

        System.out.println("\n=== Observer (EventBus) ===");
        EventBus bus = new EventBus();
        StockTicker aapl = new StockTicker(bus, "AAPL");
        bus.subscribe("price:AAPL", (e, d) -> System.out.println("Alert: AAPL=" + d));
        bus.subscribe("price:AAPL", (e, d) -> System.out.println("Log: " + e + " → " + d));
        aapl.setPrice(185.50);

        System.out.println("\n=== Strategy (Navigator) ===");
        Navigator nav = new Navigator(new FastestRoute());
        System.out.println(nav.navigate("A", "B"));
        nav.setStrategy(new EcoRoute());
        System.out.println(nav.navigate("A", "B"));

        System.out.println("\n=== Command + Undo ===");
        TextEditor editor = new TextEditor();
        CommandHistory history = new CommandHistory();
        history.execute(new WriteCommand(editor, "Hello"));
        history.execute(new WriteCommand(editor, " World"));
        System.out.println(editor.getText());   // Hello World
        history.undo();
        System.out.println(editor.getText());   // Hello

        System.out.println("\n=== Template Method ===");
        List<String> logs = List.of("INFO start", "ERROR null ptr", "INFO stop", "ERROR timeout");
        new LogPipeline().process(logs);
        new MetricsPipeline().process(List.of("metric1", "metric2"));
    }
}
