import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

public class Java8 {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Functional Interfaces (java.util.function)
    //    Predicate<T>      — T → boolean        test, and, or, negate
    //    Function<T,R>     — T → R              apply, andThen, compose
    //    Consumer<T>       — T → void           accept, andThen
    //    Supplier<T>       — () → T             get
    //    BiFunction<T,U,R> — T,U → R
    //    UnaryOperator<T>  — T → T              extends Function<T,T>
    //    BinaryOperator<T> — T,T → T            extends BiFunction<T,T,T>
    // ─────────────────────────────────────────────────────────────────────────
    static void functionalInterfacesDemo() {
        // Predicate — boolean tests, composable
        Predicate<Integer> isEven     = n -> n % 2 == 0;
        Predicate<Integer> isPositive = n -> n > 0;
        System.out.println(isEven.test(4));                    // true
        System.out.println(isEven.and(isPositive).test(4));    // true  (both true)
        System.out.println(isEven.or(isPositive).test(-3));    // false (both false)
        System.out.println(isEven.negate().test(3));           // true  (odd)

        // Function — transform T to R
        Function<String, Integer> len     = String::length;
        Function<Integer, String> numStr  = n -> "num:" + n;
        System.out.println(len.andThen(numStr).apply("hello")); // num:5  (len then numStr)
        System.out.println(len.compose(String::trim).apply("  hi  ")); // 2 (trim then len)

        // Consumer — side effects
        Consumer<String> print      = System.out::println;
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        print.andThen(printUpper).accept("hello"); // hello \n HELLO

        // Supplier — lazy factory
        Supplier<List<String>> listFactory = ArrayList::new;
        List<String> list = listFactory.get(); // new empty list

        // BiFunction
        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
        System.out.println(repeat.apply("ab", 3)); // ababab

        // UnaryOperator / BinaryOperator
        UnaryOperator<String> trim = String::trim;
        BinaryOperator<Integer> add = Integer::sum;
        System.out.println(trim.apply("  hi  "));   // hi
        System.out.println(add.apply(3, 4));         // 7

        // Compose complex pipelines
        Function<String, String> pipeline = ((Function<String, String>) String::trim)
            .andThen(String::toLowerCase)
            .andThen(s -> s.replace(" ", "_"));
        System.out.println(pipeline.apply("  Hello World  ")); // hello_world
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Method References — shorthand for lambdas that just call a method
    //    Class::staticMethod       → args match method params
    //    instance::instanceMethod  → bound receiver
    //    Class::instanceMethod     → first arg becomes receiver
    //    Class::new                → constructor
    // ─────────────────────────────────────────────────────────────────────────
    static void methodRefDemo() {
        List<String> names = Arrays.asList("Charlie", "Alice", "Bob");

        // static method ref
        names.stream().map(String::valueOf).forEach(System.out::println);

        // bound instance method ref
        String prefix = "Hello, ";
        Function<String, String> greet = prefix::concat;
        System.out.println(greet.apply("Alice")); // Hello, Alice

        // unbound instance method ref — first stream element becomes the instance
        names.stream().map(String::toUpperCase).forEach(System.out::println);
        names.sort(String::compareTo); // unbound Comparator
        System.out.println(names); // [Alice, Bob, Charlie]

        // constructor ref
        Function<String, StringBuilder> sbFactory = StringBuilder::new;
        System.out.println(sbFactory.apply("init")); // init

        // in Comparator.comparing
        names.sort(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        System.out.println("Sorted by length then alpha: " + names);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Streams
    //    Intermediate (lazy): filter, map, flatMap, distinct, sorted,
    //                         limit, skip, peek, mapToInt/Long/Double
    //    Terminal (eager):    forEach, collect, reduce, count,
    //                         findFirst, findAny, anyMatch, allMatch,
    //                         noneMatch, min, max, toArray, sum, average
    //
    //    Key rules:
    //    - Streams are consumed once
    //    - Parallel streams: stateless ops only; avoid shared mutable state
    //    - Short-circuit: findFirst/anyMatch stop early
    // ─────────────────────────────────────────────────────────────────────────
    static void streamDemo() {
        List<Integer> nums = Arrays.asList(5, 3, 8, 1, 9, 2, 7, 4, 6);

        // filter + map + sorted + collect
        List<Integer> evenSquares = nums.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Even squares: " + evenSquares); // [4, 16, 36, 64]

        // reduce — fold all elements into one
        int sum = nums.stream().reduce(0, Integer::sum);
        System.out.println("Sum: " + sum); // 45

        // flatMap — flatten nested collections
        List<List<Integer>> nested = List.of(List.of(1,2), List.of(3,4), List.of(5));
        List<Integer> flat = nested.stream().flatMap(Collection::stream).collect(Collectors.toList());
        System.out.println("Flat: " + flat); // [1,2,3,4,5]

        // distinct / limit / skip
        List<Integer> dups = Arrays.asList(1,2,2,3,3,3);
        System.out.println("Distinct: " + dups.stream().distinct().collect(Collectors.toList()));
        System.out.println("First 3: " + nums.stream().sorted().limit(3).collect(Collectors.toList()));
        System.out.println("After 7: " + nums.stream().sorted().skip(7).collect(Collectors.toList()));

        // matching
        System.out.println(nums.stream().anyMatch(n -> n > 8));  // true
        System.out.println(nums.stream().allMatch(n -> n > 0));  // true
        System.out.println(nums.stream().noneMatch(n -> n > 10)); // true

        // min / max / count
        System.out.println("Min: " + nums.stream().min(Integer::compareTo).get()); // 1
        System.out.println("Max: " + nums.stream().max(Integer::compareTo).get()); // 9
        System.out.println("Count: " + nums.stream().count()); // 9

        // findFirst (ordered) vs findAny (parallel-friendly)
        nums.stream().filter(n -> n > 5).findFirst().ifPresent(n -> System.out.println("First>5: " + n));

        // IntStream / LongStream / DoubleStream — avoid boxing
        int[] arr = IntStream.rangeClosed(1, 5).toArray(); // [1,2,3,4,5]
        System.out.println("IntStream sum: " + IntStream.of(arr).sum()); // 15
        System.out.println("IntStream avg: " + IntStream.of(arr).average().getAsDouble()); // 3.0

        IntSummaryStatistics stats = nums.stream().mapToInt(Integer::intValue).summaryStatistics();
        System.out.printf("Stats — count:%d sum:%d min:%d max:%d avg:%.1f%n",
            stats.getCount(), (long)stats.getSum(), stats.getMin(), stats.getMax(), stats.getAverage());

        // generate / iterate
        List<Integer> generated = Stream.generate(() -> 1).limit(5).collect(Collectors.toList());
        List<Integer> iterated  = Stream.iterate(1, n -> n * 2).limit(5).collect(Collectors.toList());
        System.out.println("Generated: " + generated); // [1,1,1,1,1]
        System.out.println("Iterated:  " + iterated);  // [1,2,4,8,16]

        // String joining
        String joined = Stream.of("a","b","c").collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Joined: " + joined); // [a, b, c]

        // toArray
        String[] words = Stream.of("hello","world").toArray(String[]::new);
        System.out.println(Arrays.toString(words));

        // parallel stream — good for CPU-bound, stateless, large datasets
        long count = nums.parallelStream().filter(n -> n > 3).count();
        System.out.println("Parallel count > 3: " + count); // 6
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Collectors
    //    toList / toSet / toUnmodifiableList
    //    toMap(keyFn, valueFn [, mergeFn])
    //    groupingBy(classifier [, downstream])
    //    partitioningBy(predicate)
    //    joining(delimiter, prefix, suffix)
    //    counting / summingInt / averagingInt / summarizingInt
    //    mapping / collectingAndThen
    // ─────────────────────────────────────────────────────────────────────────
    record Person(String name, String dept, int salary) {}

    static void collectorsDemo() {
        List<Person> people = List.of(
            new Person("Alice", "Eng", 90000),
            new Person("Bob",   "Eng", 85000),
            new Person("Carol", "HR",  70000),
            new Person("Dave",  "HR",  75000),
            new Person("Eve",   "Eng", 95000)
        );

        // toMap — throws on duplicate keys unless merge fn provided
        Map<String, Integer> nameSalary = people.stream()
            .collect(Collectors.toMap(Person::name, Person::salary));
        System.out.println("toMap: " + nameSalary.get("Alice")); // 90000

        // toMap with merge function (take max on duplicate key)
        Map<String, Integer> deptMaxSal = people.stream()
            .collect(Collectors.toMap(Person::dept, Person::salary, Math::max));
        System.out.println("Dept max: " + deptMaxSal); // {Eng=95000, HR=75000}

        // groupingBy — Map<key, List<elements>>
        Map<String, List<Person>> byDept = people.stream()
            .collect(Collectors.groupingBy(Person::dept));
        System.out.println("Groups: " + byDept.keySet());

        // groupingBy with downstream
        Map<String, Long> countByDept = people.stream()
            .collect(Collectors.groupingBy(Person::dept, Collectors.counting()));
        System.out.println("Count by dept: " + countByDept); // {Eng=3, HR=2}

        Map<String, Double> avgSalByDept = people.stream()
            .collect(Collectors.groupingBy(Person::dept, Collectors.averagingInt(Person::salary)));
        System.out.println("Avg sal: " + avgSalByDept);

        Map<String, Optional<Person>> topByDept = people.stream()
            .collect(Collectors.groupingBy(Person::dept,
                Collectors.maxBy(Comparator.comparingInt(Person::salary))));
        topByDept.forEach((dept, p) -> System.out.println(dept + " top: " + p.get().name()));

        // groupingBy → collect names as List<String> per group
        Map<String, List<String>> namesByDept = people.stream()
            .collect(Collectors.groupingBy(Person::dept,
                Collectors.mapping(Person::name, Collectors.toList())));
        System.out.println("Names by dept: " + namesByDept);

        // partitioningBy — always returns Map<Boolean, List>
        Map<Boolean, List<Person>> partition = people.stream()
            .collect(Collectors.partitioningBy(p -> p.salary() >= 85000));
        System.out.println("High earners: " + partition.get(true).stream().map(Person::name).toList());

        // joining
        String names = people.stream().map(Person::name)
            .collect(Collectors.joining(", ", "[", "]"));
        System.out.println("Names: " + names);

        // collectingAndThen — wrap another collector with a finisher
        List<String> unmodifiable = people.stream().map(Person::name)
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        // summarizingInt
        IntSummaryStatistics salStats = people.stream()
            .collect(Collectors.summarizingInt(Person::salary));
        System.out.printf("Salaries — avg:%.0f max:%d min:%d%n",
            salStats.getAverage(), salStats.getMax(), salStats.getMin());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Optional — explicit null-safety; return type only (not params/fields)
    //    of(v)           — wraps non-null; throws NPE if null
    //    ofNullable(v)   — wraps nullable; empty if null
    //    empty()         — empty Optional
    //    get()           — value or NoSuchElementException (avoid without check)
    //    orElse(v)       — value or default (always evaluates default)
    //    orElseGet(fn)   — value or lazy supplier (preferred for expensive defaults)
    //    orElseThrow(fn) — value or throw
    //    map / flatMap / filter
    //    ifPresent / ifPresentOrElse
    //    stream()        — 0 or 1 element stream (Java 9+)
    // ─────────────────────────────────────────────────────────────────────────
    static void optionalDemo() {
        Optional<String> present  = Optional.of("hello");
        Optional<String> empty    = Optional.empty();
        Optional<String> nullable = Optional.ofNullable(null); // same as empty

        System.out.println(present.get());             // hello
        System.out.println(empty.orElse("default"));   // default
        System.out.println(empty.orElseGet(() -> "computed")); // computed (lazy)

        // map — transform if present, stay empty if not
        System.out.println(present.map(String::length));   // Optional[5]
        System.out.println(empty.map(String::length));     // Optional.empty

        // filter — empty if predicate fails
        System.out.println(present.filter(s -> s.length() > 3).isPresent()); // true

        // flatMap — when mapping returns Optional (avoids Optional<Optional<T>>)
        Optional<String> result = present.flatMap(s -> Optional.of(s.toUpperCase()));
        System.out.println(result.get()); // HELLO

        // ifPresent / ifPresentOrElse
        present.ifPresent(s -> System.out.println("Present: " + s));
        empty.ifPresentOrElse(
            s -> System.out.println("Present: " + s),
            () -> System.out.println("Was empty"));

        // stream (Java 9) — useful for filtering out empty optionals in a stream
        List<Optional<String>> opts = List.of(Optional.of("a"), empty, Optional.of("b"));
        long count = opts.stream().flatMap(Optional::stream).count();
        System.out.println("Non-empty: " + count); // 2

        // Real pattern: chain of operations that might fail
        Optional<String> name = Optional.ofNullable(System.getenv("USER"))
            .map(String::trim)
            .filter(s -> !s.isEmpty());
        System.out.println("User: " + name.orElse("unknown"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Default & Static Interface Methods
    //    default — adds behavior to interface without breaking implementors
    //    static  — utility methods on the interface itself
    //    private — (Java 9+) shared helper for default methods
    // ─────────────────────────────────────────────────────────────────────────
    interface Greeter {
        String greet(String name); // abstract

        default String greetLoud(String name) {         // inheritable with override
            return greet(name).toUpperCase();
        }

        default String greetTwice(String name) {         // composed from other defaults
            return greet(name) + "! " + greet(name) + "!";
        }

        static Greeter formal() {                         // factory on the interface
            return name -> "Good day, " + name + ".";
        }
    }

    // Multiple interface defaults: resolve by overriding
    interface A { default String hello() { return "A"; } }
    interface B extends A { default String hello() { return "B"; } }
    static class C implements B {} // inherits B's hello()

    static void interfaceMethodDemo() {
        Greeter casual = name -> "Hey, " + name;
        System.out.println(casual.greet("Alice"));       // Hey, Alice
        System.out.println(casual.greetLoud("Alice"));   // HEY, ALICE
        System.out.println(casual.greetTwice("Alice"));  // Hey, Alice! Hey, Alice!
        System.out.println(Greeter.formal().greet("Bob")); // Good day, Bob.
        System.out.println(new C().hello()); // B
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Date/Time API (java.time) — immutable, thread-safe, no nulls
    //    LocalDate       — date only, no time, no tz
    //    LocalTime       — time only, no date, no tz
    //    LocalDateTime   — date + time, no tz
    //    ZonedDateTime   — date + time + tz
    //    Instant         — machine timestamp (epoch seconds + nanos)
    //    Duration        — time-based amount (seconds/nanos)
    //    Period          — date-based amount (years/months/days)
    //    DateTimeFormatter — parsing and formatting
    // ─────────────────────────────────────────────────────────────────────────
    static void dateTimeDemo() {
        // LocalDate
        LocalDate today = LocalDate.now();
        LocalDate d     = LocalDate.of(2024, 1, 15);
        System.out.println("Date: " + d);
        System.out.println("Day of week: " + d.getDayOfWeek()); // MONDAY
        System.out.println("+10 days: " + d.plusDays(10));
        System.out.println("-1 month: " + d.minusMonths(1));
        System.out.println("Leap year: " + d.isLeapYear());     // true
        System.out.println("Before today: " + d.isBefore(today));

        // LocalTime
        LocalTime t = LocalTime.of(14, 30, 0);
        System.out.println("+2h: " + t.plusHours(2)); // 16:30

        // LocalDateTime
        LocalDateTime ldt = LocalDateTime.of(d, t);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        System.out.println("Formatted: " + ldt.format(fmt)); // 15/01/2024 14:30

        // ZonedDateTime
        ZoneId nyZone = ZoneId.of("America/New_York");
        ZonedDateTime zdt = ZonedDateTime.now(nyZone);
        System.out.println("NY time: " + zdt.toLocalTime().truncatedTo(ChronoUnit.MINUTES));

        // Convert between timezones
        ZonedDateTime utc = zdt.withZoneSameInstant(ZoneId.of("UTC"));
        System.out.println("UTC offset: " + zdt.getOffset());

        // Duration — time-based (hours, minutes, seconds, nanos)
        Duration dur = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 30));
        System.out.printf("Work day: %dh %dm%n", dur.toHours(), dur.toMinutesPart()); // 8h 30m

        // Period — date-based (years, months, days)
        Period period = Period.between(d, today);
        System.out.printf("Elapsed: %dy %dm %dd%n",
            period.getYears(), period.getMonths(), period.getDays());

        // Instant — epoch millis for timestamps, comparisons
        Instant now = Instant.now();
        Instant later = now.plusSeconds(3600);
        System.out.println("Before: " + now.isBefore(later)); // true
        System.out.println("Epoch ms: " + now.toEpochMilli());

        // Parsing
        LocalDate parsed = LocalDate.parse("15/01/2024", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        System.out.println("Parsed: " + parsed); // 2024-01-15

        // ChronoUnit — portable way to measure and add
        long days = ChronoUnit.DAYS.between(d, today);
        System.out.println("Days since d: " + days);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Records (Java 16+) — immutable data carriers
    //    Auto-generated: constructor, accessors, equals, hashCode, toString
    //    Cannot extend classes; can implement interfaces
    //    Compact canonical constructor — validate/normalize without repeating fields
    // ─────────────────────────────────────────────────────────────────────────
    record Point(int x, int y) {
        Point { // compact canonical constructor
            if (x < 0 || y < 0) throw new IllegalArgumentException("Negative coords");
        }
        double distanceTo(Point o) { return Math.sqrt(Math.pow(x-o.x,2) + Math.pow(y-o.y,2)); }
        Point translate(int dx, int dy) { return new Point(x + dx, y + dy); } // "wither"
    }

    static void recordDemo() {
        Point p1 = new Point(3, 4);
        System.out.println(p1);                       // Point[x=3, y=4]
        System.out.println(p1.x() + ", " + p1.y());  // 3, 4 (accessors)
        System.out.println(p1.equals(new Point(3,4))); // true
        System.out.printf("Distance: %.1f%n", p1.distanceTo(new Point(0,0))); // 5.0
        System.out.println(p1.translate(1, 1)); // Point[x=4, y=5]
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. Sealed Classes (Java 17+) — exhaustive hierarchy
    //    sealed — only listed permits can extend
    //    final permits — no further extension
    //    Switch expressions can cover all cases without default
    // ─────────────────────────────────────────────────────────────────────────
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    static double area(Shape s) {
        return switch (s) {
            case Circle c       -> Math.PI * c.radius() * c.radius();
            case Rectangle r    -> r.w() * r.h();
            case Triangle t     -> 0.5 * t.base() * t.height();
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. Text Blocks (Java 15+) & Switch Expressions (Java 14+)
    // ─────────────────────────────────────────────────────────────────────────
    static void modernSyntaxDemo() {
        // Text block — multiline string, auto-dedents, preserves newlines
        String json = """
                {
                  "name": "Alice",
                  "age": 30
                }
                """;
        System.out.println(json);

        // Switch expression — returns a value; arrow syntax prevents fall-through
        int day = 3;
        String dayName = switch (day) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            default -> "Other";
        };
        System.out.println(dayName); // Wednesday

        // Switch with yield — for multi-statement cases
        int result = switch (day) {
            case 1, 2, 3, 4, 5 -> {
                System.out.print("Weekday: ");
                yield day;
            }
            default -> 0;
        };
        System.out.println(result); // Weekday: 3
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. Pattern Matching (Java 16+)
    //    instanceof pattern — eliminates cast
    //    switch pattern (Java 21) — match on type + extract
    // ─────────────────────────────────────────────────────────────────────────
    static void patternMatchingDemo() {
        // instanceof pattern — no explicit cast
        Object obj = "Hello, World!";
        if (obj instanceof String s && s.length() > 5) {
            System.out.println("Long string: " + s.toUpperCase());
        }

        // Pattern switch (Java 21)
        Object val = 42;
        String desc = switch (val) {
            case Integer i when i > 0 -> "positive int: " + i;
            case Integer i            -> "non-positive int: " + i;
            case String s             -> "string: " + s;
            default                   -> "other: " + val;
        };
        System.out.println(desc); // positive int: 42

        // Sealed type — switch with all cases covered, no default needed
        List<Shape> shapes = List.of(new Circle(5), new Rectangle(3,4), new Triangle(6,8));
        shapes.forEach(s -> System.out.printf("Area: %.1f%n", area(s)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("=== Functional Interfaces ===");
        functionalInterfacesDemo();

        System.out.println("\n=== Method References ===");
        methodRefDemo();

        System.out.println("\n=== Streams ===");
        streamDemo();

        System.out.println("\n=== Collectors ===");
        collectorsDemo();

        System.out.println("\n=== Optional ===");
        optionalDemo();

        System.out.println("\n=== Interface Default/Static Methods ===");
        interfaceMethodDemo();

        System.out.println("\n=== Date/Time ===");
        dateTimeDemo();

        System.out.println("\n=== Records ===");
        recordDemo();

        System.out.println("\n=== Modern Syntax ===");
        modernSyntaxDemo();

        System.out.println("\n=== Pattern Matching ===");
        patternMatchingDemo();
    }
}
