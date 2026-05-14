import java.util.*;

public class LoggerMessagePrinter {

    // ═══════════════════════════════════════════════════════════════════════════
    // Logger — two variants asked at Google
    //
    // Variant A — Rate-Limited Logger (LeetCode 359)
    //   shouldPrintMessage(timestamp, message)
    //   Returns true only if the message hasn't been printed in the last 10s.
    //   Use case: suppress duplicate log spam.
    //
    // Variant B — Request Tracker (Google onsite confirmed)
    //   startReq(reqId, startTime)   — log when a request began
    //   finishReq(reqId)             — mark a request as done
    //   printFinished()              — print finished requests in start-time order
    //
    //   Key insight: requests finish out of order. We can only emit a contiguous
    //   prefix of the start-time ordering where ALL requests are done.
    //   A min-heap on startTime lets us drain greedily from the front.
    //
    //   Example: A(t=1), B(t=2), C(t=3), D(t=4) start.
    //   D finishes, C finishes → can't print yet (A is still running).
    //   A finishes → print A; B still running, stop.
    //   B finishes → print B, C, D.
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Variant A: Rate-limited logger ────────────────────────────────────────

    static class RateLimitLogger {
        private static final int COOLDOWN_SEC = 10;
        private final Map<String, Integer> lastPrinted = new HashMap<>();

        // Returns true and records timestamp if message was not printed in last 10s.
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

    static class RequestLogger {
        private static class Req {
            final String id;
            final long   startTime;
            volatile boolean finished = false;

            Req(String id, long startTime) { this.id = id; this.startTime = startTime; }
        }

        private final Map<String, Req>       active  = new HashMap<>();
        // Min-heap ordered by startTime so we always print in start-time order
        private final PriorityQueue<Req>     heap    =
            new PriorityQueue<>(Comparator.comparingLong(r -> r.startTime));

        public void startReq(String reqId, long startTime) {
            Req r = new Req(reqId, startTime);
            active.put(reqId, r);
            heap.offer(r);
        }

        public void finishReq(String reqId) {
            Req r = active.get(reqId);
            if (r == null) throw new IllegalArgumentException("Unknown request: " + reqId);
            r.finished = true;
        }

        // Drain the heap while the earliest-started request is also finished.
        // This ensures we never emit a gap in start-time order.
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
