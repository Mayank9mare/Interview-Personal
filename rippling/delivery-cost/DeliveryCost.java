import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Entry point demonstrating {@link PaymentTracker}.
 * Compile: {@code javac DeliveryCost.java}  Run: {@code java DeliveryCost}
 */
public class DeliveryCost {

    /**
     * Live-dashboard backend that tracks delivery driver payout costs.
     *
     * <p>Each delivery is priced independently as {@code rate × durationSeconds / 3600}.
     * Concurrent deliveries by the same driver are each paid in full — no overlap deduction.
     *
     * <p>All monetary arithmetic uses {@link BigDecimal} (HALF_UP, 2 dp for storage) to
     * avoid the IEEE-754 rounding errors that accumulate when using {@code double} at scale.
     *
     * <p>Time strings are accepted in {@code "HH:MM"} or {@code "HH:MM:SS"} format.
     *
     * <p>Thread safety: Not thread-safe.
     */
    static class PaymentTracker {
        private static final int SECONDS_PER_HOUR = 3600;

        /** Per-driver state: ID, hourly rate, accumulated cost, and delivery count. */
        private static class DriverRecord {
            /** Driver's unique numeric ID. */
            final int id;

            /** Hourly rate stored as {@link BigDecimal} to avoid floating-point drift. */
            final BigDecimal ratePerHour;

            /** Running sum of costs (rounded to 2 dp) for all recorded deliveries. */
            BigDecimal totalCost = BigDecimal.ZERO;

            /** Number of deliveries recorded for this driver. */
            int deliveryCount = 0;

            /**
             * @param id          driver ID
             * @param ratePerHour exact hourly rate
             */
            DriverRecord(int id, BigDecimal ratePerHour) {
                this.id = id;
                this.ratePerHour = ratePerHour;
            }
        }

        /** All registered drivers keyed by their ID. */
        private final Map<Integer, DriverRecord> drivers = new HashMap<>();

        /** Running sum of costs across all drivers; updated on every {@link #recordDelivery}. */
        private BigDecimal grandTotal = BigDecimal.ZERO;

        /**
         * Registers a new driver. The {@code double} rate is converted to {@link BigDecimal}
         * immediately so all subsequent arithmetic remains exact.
         *
         * @param driverId      unique driver identifier
         * @param usdHourlyRate hourly pay rate in USD
         * @throws IllegalArgumentException if a driver with this ID is already registered
         */
        public void addDriver(int driverId, double usdHourlyRate) {
            if (drivers.containsKey(driverId))
                throw new IllegalArgumentException("Driver already exists: " + driverId);
            drivers.put(driverId, new DriverRecord(driverId, BigDecimal.valueOf(usdHourlyRate)));
        }

        /**
         * Records a completed delivery and accumulates the cost for the driver.
         *
         * @param driverId unique driver identifier (must be registered)
         * @param start    delivery start time in {@code "HH:MM"} or {@code "HH:MM:SS"} format
         * @param end      delivery end time (must be strictly after {@code start})
         * @throws IllegalArgumentException if the driver is unknown or end is not after start
         */
        public void recordDelivery(int driverId, String start, String end) {
            DriverRecord dr = drivers.get(driverId);
            if (dr == null) throw new IllegalArgumentException("Unknown driver: " + driverId);

            long startSec = parseTime(start);
            long endSec   = parseTime(end);
            if (endSec <= startSec)
                throw new IllegalArgumentException("end must be after start");

            long durationSec = endSec - startSec;
            // cost = rate * (durationSec / 3600)  — keep 10 decimal places internally,
            // round to cents (2 dp) for storage so accumulated totals stay in cents.
            BigDecimal cost = dr.ratePerHour
                .multiply(BigDecimal.valueOf(durationSec))
                .divide(BigDecimal.valueOf(SECONDS_PER_HOUR), 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

            dr.totalCost = dr.totalCost.add(cost);
            dr.deliveryCount++;
            grandTotal = grandTotal.add(cost);
        }

        // ── Query ─────────────────────────────────────────────────────────────

        /**
         * Returns the aggregate payout cost across all drivers.
         *
         * @return grand total cost rounded to 2 decimal places
         */
        public BigDecimal getTotalCost() { return grandTotal; }

        /**
         * Returns the total payout cost for a single driver.
         *
         * @param driverId the driver to query (must be registered)
         * @return total cost for this driver, rounded to 2 decimal places
         * @throws IllegalArgumentException if the driver is unknown
         */
        public BigDecimal getDriverCost(int driverId) {
            DriverRecord dr = drivers.get(driverId);
            if (dr == null) throw new IllegalArgumentException("Unknown driver: " + driverId);
            return dr.totalCost;
        }

        /** Prints a per-driver breakdown including rate, delivery count, cost, and grand total. */
        public void printReport() {
            System.out.println("=== Driver Payment Report ===");
            drivers.values().stream()
                .sorted(Comparator.comparingInt(d -> d.id))
                .forEach(d -> System.out.printf(
                    "  Driver %3d | rate $%-6.2f/hr | %2d deliveries | cost $%s%n",
                    d.id, d.ratePerHour, d.deliveryCount, d.totalCost));
            System.out.printf("  %-46s TOTAL  $%s%n", "", grandTotal);
        }

        // ── Utility ───────────────────────────────────────────────────────────

        /**
         * Parses a time string in {@code "HH:MM"} or {@code "HH:MM:SS"} format to seconds
         * since midnight.
         *
         * @param t time string
         * @return total seconds since midnight
         */
        static long parseTime(String t) {
            String[] p = t.split(":");
            long h = Long.parseLong(p[0]);
            long m = Long.parseLong(p[1]);
            long s = (p.length > 2) ? Long.parseLong(p[2]) : 0;
            return h * 3600 + m * 60 + s;
        }
    }

    public static void main(String[] args) {
        PaymentTracker tracker = new PaymentTracker();

        tracker.addDriver(1, 10.00);   // $10/hr
        tracker.addDriver(2, 15.00);   // $15/hr
        tracker.addDriver(3, 12.50);   // $12.50/hr

        // Driver 1: 1.5 h → $15.00
        tracker.recordDelivery(1, "08:00", "09:30");

        // Driver 2: 45 min → $11.25
        tracker.recordDelivery(2, "09:00", "09:45");

        // Driver 1: simultaneous second delivery — 30 min → $5.00
        tracker.recordDelivery(1, "09:00", "09:30");

        // Driver 3: with seconds precision — 1h 1min 12sec = 3672s → $12.75
        tracker.recordDelivery(3, "10:00:00", "11:01:12");

        tracker.printReport();
        System.out.println("\nTotalCost via API: $" + tracker.getTotalCost());

        System.out.println("\n=== Follow-up: float vs BigDecimal ===");
        // Demonstrate why double fails for money:
        double a = 0.1 + 0.2;
        System.out.println("0.1 + 0.2 in double  = " + a);        // 0.30000000000000004
        BigDecimal b = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        System.out.println("0.1 + 0.2 in BigDecimal = " + b);     // 0.3  (exact)

        System.out.println("\n=== Edge: concurrent deliveries ===");
        PaymentTracker t2 = new PaymentTracker();
        t2.addDriver(99, 20.00);
        // Two 1-hour deliveries overlapping completely — both paid in full → $40
        t2.recordDelivery(99, "10:00", "11:00");
        t2.recordDelivery(99, "10:00", "11:00");
        System.out.println("Concurrent same-window × 2: $" + t2.getTotalCost()); // $40.00
    }
}
