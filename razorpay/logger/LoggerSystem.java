// Companies: Razorpay
// Logger system that routes log messages to one or more sinks based on log level,
// using the Chain of Responsibility pattern. Confirmed in Mar 2024 Razorpay SDE2 round.

import java.util.*;

/**
 * Logger system that routes messages to multiple sinks using Chain of Responsibility.
 *
 * <p>Each {@link LogHandler} in the chain inspects the incoming {@link LogLevel} and
 * decides whether to handle (write to its sink) and whether to pass to the next handler.
 *
 * <p>Design decisions:
 * <ul>
 *   <li><b>Chain of Responsibility</b>: decouples the sender from the receiver; adding a
 *       new sink requires no changes to existing handlers.</li>
 *   <li><b>Threshold model</b>: each handler processes messages at or above its configured
 *       minimum level (e.g., ERROR handler handles ERROR and FATAL).</li>
 *   <li><b>Always-pass flag</b>: by default every handler forwards to the next, enabling
 *       fan-out (e.g., WARN → both console and file). Set {@code stopAfterHandle=true}
 *       to short-circuit (first match wins).</li>
 * </ul>
 *
 * <p>Extensibility: add a new sink by extending {@link LogHandler} — zero changes to
 * the chain-building or routing logic.
 *
 * <p>Thread safety: Not thread-safe. External synchronisation required for concurrent
 * loggers.
 */
public class LoggerSystem {

    // ── Log level ─────────────────────────────────────────────────────────

    /**
     * Severity levels in ascending order. Handlers use ordinal comparison
     * so {@code level.ordinal() >= threshold.ordinal()} means "handle this".
     */
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    // ── Log message ───────────────────────────────────────────────────────

    /** Immutable value object carrying all fields of a single log event. */
    static class LogMessage {
        final LogLevel level;
        final String message;
        final long timestamp;

        LogMessage(LogLevel level, String message) {
            if (level == null) throw new IllegalArgumentException("level cannot be null");
            if (message == null) throw new IllegalArgumentException("message cannot be null");
            this.level = level;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", timestamp, level, message);
        }
    }

    // ── Abstract handler ──────────────────────────────────────────────────

    /**
     * Base class for all log handlers in the chain.
     *
     * <p>Subclasses implement {@link #write} to route to their specific sink.
     * The chaining and threshold logic lives here, not in subclasses.
     */
    static abstract class LogHandler {
        /** Minimum level this handler will process. */
        private final LogLevel minLevel;

        /** Next handler in the chain; null means end of chain. */
        private LogHandler next;

        /**
         * @param minLevel minimum log level this handler cares about
         */
        LogHandler(LogLevel minLevel) {
            this.minLevel = minLevel;
        }

        /**
         * Sets the next handler in the chain and returns it for fluent chaining.
         *
         * @param next handler to delegate to after this one
         * @return {@code next} (for chaining: {@code h1.setNext(h2).setNext(h3)})
         */
        public LogHandler setNext(LogHandler next) {
            this.next = next;
            return next;
        }

        /**
         * Handles the message if its level meets the threshold, then forwards
         * to the next handler unconditionally (fan-out behaviour).
         *
         * @param msg log message to process
         */
        public final void handle(LogMessage msg) {
            if (msg.level.ordinal() >= minLevel.ordinal()) {
                write(msg);
            }
            if (next != null) {
                next.handle(msg);
            }
        }

        /**
         * Writes the log message to this handler's sink.
         *
         * @param msg log message that has already passed the level threshold
         */
        protected abstract void write(LogMessage msg);
    }

    // ── Concrete handlers ─────────────────────────────────────────────────

    /**
     * Writes all messages at or above {@code minLevel} to standard output.
     */
    static class ConsoleHandler extends LogHandler {
        /**
         * @param minLevel minimum level to print to console
         */
        ConsoleHandler(LogLevel minLevel) {
            super(minLevel);
        }

        @Override
        protected void write(LogMessage msg) {
            System.out.println("[CONSOLE] " + msg);
        }
    }

    /**
     * Accumulates messages in an in-memory list (simulates file writing).
     */
    static class FileHandler extends LogHandler {
        private final String filename;

        /** In-memory log buffer (simulates a file). */
        private final List<String> buffer = new ArrayList<>();

        /**
         * @param minLevel minimum level to write to the file
         * @param filename logical file name (for display purposes)
         */
        FileHandler(LogLevel minLevel, String filename) {
            super(minLevel);
            this.filename = filename;
        }

        @Override
        protected void write(LogMessage msg) {
            String entry = "[FILE:" + filename + "] " + msg;
            buffer.add(entry);
            System.out.println(entry);
        }

        /**
         * Returns an unmodifiable view of all buffered log entries.
         *
         * @return buffered log lines
         */
        public List<String> getBuffer() {
            return Collections.unmodifiableList(buffer);
        }
    }

    /**
     * Simulates alerting an on-call team for critical errors.
     */
    static class AlertHandler extends LogHandler {
        private final List<String> alertsSent = new ArrayList<>();

        /** Handles only ERROR and FATAL — threshold is ERROR. */
        AlertHandler() {
            super(LogLevel.ERROR);
        }

        @Override
        protected void write(LogMessage msg) {
            String alert = "[ALERT -> PagerDuty] " + msg.level + ": " + msg.message;
            alertsSent.add(alert);
            System.out.println(alert);
        }

        /** Returns all alerts that have been fired. */
        public List<String> getAlertsSent() {
            return Collections.unmodifiableList(alertsSent);
        }
    }

    // ── Logger facade ─────────────────────────────────────────────────────

    /**
     * Entry point that exposes level-named convenience methods and owns the chain.
     *
     * <p>The chain is built once at construction time. Callers use
     * {@code logger.error("...")} instead of manually constructing LogMessage objects.
     */
    static class Logger {
        private final LogHandler chainHead;

        /**
         * Constructs a Logger with the given handler as the head of the chain.
         *
         * @param chainHead first handler in the chain
         */
        Logger(LogHandler chainHead) {
            if (chainHead == null) throw new IllegalArgumentException("chain head cannot be null");
            this.chainHead = chainHead;
        }

        /** Logs a DEBUG message. */
        public void debug(String msg) { log(LogLevel.DEBUG, msg); }

        /** Logs an INFO message. */
        public void info(String msg) { log(LogLevel.INFO, msg); }

        /** Logs a WARN message. */
        public void warn(String msg) { log(LogLevel.WARN, msg); }

        /** Logs an ERROR message. */
        public void error(String msg) { log(LogLevel.ERROR, msg); }

        /** Logs a FATAL message. */
        public void fatal(String msg) { log(LogLevel.FATAL, msg); }

        /**
         * Routes a log message through the entire handler chain.
         *
         * @param level   severity level
         * @param message log text
         */
        public void log(LogLevel level, String message) {
            chainHead.handle(new LogMessage(level, message));
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Build chain: ConsoleHandler (DEBUG+) → FileHandler (WARN+) → AlertHandler (ERROR+)
        ConsoleHandler console = new ConsoleHandler(LogLevel.DEBUG);
        FileHandler file = new FileHandler(LogLevel.WARN, "app.log");
        AlertHandler alert = new AlertHandler();

        // Wire the chain
        console.setNext(file).setNext(alert);

        Logger logger = new Logger(console);

        System.out.println("=== Sending DEBUG ===");
        logger.debug("Starting up payment service");
        // Expected: ConsoleHandler prints; FileHandler skips (below WARN); AlertHandler skips

        System.out.println("\n=== Sending INFO ===");
        logger.info("Payment request received for order #12345");
        // Expected: ConsoleHandler prints; FileHandler skips; AlertHandler skips

        System.out.println("\n=== Sending WARN ===");
        logger.warn("Payment gateway response slow (>2s)");
        // Expected: ConsoleHandler prints; FileHandler writes to app.log; AlertHandler skips

        System.out.println("\n=== Sending ERROR ===");
        logger.error("Payment failed: insufficient funds for order #12345");
        // Expected: all three handlers fire

        System.out.println("\n=== Sending FATAL ===");
        logger.fatal("Database connection pool exhausted — payments down");
        // Expected: all three handlers fire

        System.out.println("\n=== File buffer contents ===");
        for (String line : file.getBuffer()) {
            System.out.println("  " + line);
        }
        // Expected: 3 entries (WARN, ERROR, FATAL)
        System.out.println("  Buffer size: " + file.getBuffer().size()); // Expected: 3

        System.out.println("\n=== Alert count ===");
        System.out.println("  Alerts fired: " + alert.getAlertsSent().size()); // Expected: 2 (ERROR + FATAL)

        System.out.println("\n=== Add a new DatabaseHandler at runtime (extensibility demo) ===");
        // Extending the chain without modifying existing handlers
        LogHandler dbHandler = new LogHandler(LogLevel.FATAL) {
            @Override
            protected void write(LogMessage msg) {
                System.out.println("[DB:audit_log] " + msg);
            }
        };
        alert.setNext(dbHandler);
        logger.fatal("Disk full on payment server");
        // Expected: Console + File + Alert + DB all fire
    }
}
