import java.util.*;

/**
 * Entry point demonstrating both logger variants asked at Google.
 * Compile: {@code javac LoggerMessagePrinter.java}  Run: {@code java LoggerMessagePrinter}
 */
public class LoggerMessagePrinter {

    // ── Variant A: Rate-limited logger ────────────────────────────────────────

    /**
     * Rate-limiting logger that suppresses duplicate messages within a 10-second cooldown
     * window (LeetCode 359).
     *
     * <p>Core data structure: {@code HashMap<message, lastTimestamp>} — records the last
     * second at which each message was allowed to print.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class RateLimitLogger {
        private static final int COOLDOWN_SEC = 10;

        /** Maps each message to the timestamp at which it was last printed. */
        private final Map<String, Integer> lastPrinted = new HashMap<>();

        /**
         * Returns {@code true} and records the timestamp if the message has not been printed
         * in the last 10 seconds. Suppresses the message otherwise.
         *
         * @param timestamp current time in seconds (non-decreasing)
         * @param message   the log message
         * @return true if the message should be printed now
         */
        public boolean shouldPrintMessage(int timestamp, String message) {
            Integer last = lastPrinted.get(message);
            if (last == null || timestamp - last >= COOLDOWN_SEC) {
                lastPrinted.put(message, timestamp);
                return true;
            }
            return false;
        }
    }

    // ── Variant B: Request tracker ────────────────────────────────────────────

    /**
     * Request tracker that prints finished requests in strict start-time order (Google onsite).
     *
     * <p>Key insight: requests complete out of order; we can only emit a contiguous prefix
     * of the start-time ordering where every earlier request is also done.
     *
     * <p>Core data structures:
     * <ul>
     *   <li>Min-heap on {@code startTime} — always exposes the earliest-started request.</li>
     *   <li>{@code HashMap<reqId, Req>} — O(1) {@link #finishReq} lookup.</li>
     * </ul>
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class RequestLogger {
        /** Mutable state for a single in-flight request. */
        private static class Req {
            /** Unique request identifier. */
            final String id;

            /** Wall-clock time when the request started. */
            final long   startTime;

            /** Set to {@code true} by {@link RequestLogger#finishReq}. */
            volatile boolean finished = false;

            /** @param id unique identifier; @param startTime epoch time the request started */
            Req(String id, long startTime) { this.id = id; this.startTime = startTime; }
        }

        /** O(1) lookup of a request's mutable state by ID. */
        private final Map<String, Req>       active  = new HashMap<>();

        /** Min-heap on startTime so the earliest-started request is always at the front. */
        private final PriorityQueue<Req>     heap    =
            new PriorityQueue<>(Comparator.comparingLong(r -> r.startTime));

        /**
         * Logs the start of a new request.
         *
         * @param reqId     unique request identifier
         * @param startTime time the request began (used for ordering)
         */
        public void startReq(String reqId, long startTime) {
            Req r = new Req(reqId, startTime);
            active.put(reqId, r);
            heap.offer(r);
        }

        /**
         * Marks the request as finished.
         *
         * @param reqId the request that completed
         * @throws IllegalArgumentException if the request was never started
         */
        public void finishReq(String reqId) {
            Req r = active.get(reqId);
            if (r == null) throw new IllegalArgumentException("Unknown request: " + reqId);
            r.finished = true;
        }

        /**
         * Drains and prints all finished requests from the front of the heap in start-time
         * order, stopping as soon as the earliest-started request is still running.
         * This guarantees no gap in the emitted ordering.
         */
        public void printFinished() {
            List<Req> printed = new ArrayList<>();
            while (!heap.isEmpty() && heap.peek().finished) {
                Req r = heap.poll();
                printed.add(r);
                active.remove(r.id);
            }
            if (printed.isEmpty()) {
                System.out.println("  (nothing to print yet — earliest request still running)");
            } else {
                for (Req r : printed)
                    System.out.printf("  [t=%3d] %-10s finished%n", r.startTime, r.id);
            }
            if (!heap.isEmpty())
                System.out.printf("  (earliest unfinished started at t=%d)%n",
                    heap.peek().startTime);
        }

        /** Returns the number of requests (finished or running) still in the heap. */
        public int pendingCount() { return heap.size(); }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Variant A: Rate-Limited Logger           ");
        System.out.println("══════════════════════════════════════════");
        RateLimitLogger rl = new RateLimitLogger();
        // message "foo": first at t=1, next attempt t=3 (< 10s) → false, t=11 (≥10s) → true
        System.out.println(rl.shouldPrintMessage(1,  "foo")); // true
        System.out.println(rl.shouldPrintMessage(2,  "bar")); // true
        System.out.println(rl.shouldPrintMessage(3,  "foo")); // false — cooldown
        System.out.println(rl.shouldPrintMessage(8,  "bar")); // false — cooldown
        System.out.println(rl.shouldPrintMessage(11, "foo")); // true  — 10s since t=1
        System.out.println(rl.shouldPrintMessage(12, "bar")); // true  — 10s since t=2

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Variant B: Request Tracker               ");
        System.out.println("══════════════════════════════════════════");
        RequestLogger logger = new RequestLogger();

        // 4 requests start in order
        logger.startReq("A", 1);
        logger.startReq("B", 2);
        logger.startReq("C", 3);
        logger.startReq("D", 4);

        // D and C finish first (out of order)
        logger.finishReq("D");
        logger.finishReq("C");
        System.out.println("After C, D finish (A, B still running):");
        logger.printFinished(); // nothing — A(t=1) is heap front and unfinished

        logger.finishReq("A");
        System.out.println("\nAfter A finishes:");
        logger.printFinished(); // prints A only — B(t=2) still running

        logger.finishReq("B");
        System.out.println("\nAfter B finishes:");
        logger.printFinished(); // prints B, C, D (all were finished and queued)

        System.out.println("Pending: " + logger.pendingCount()); // 0
    }
}
