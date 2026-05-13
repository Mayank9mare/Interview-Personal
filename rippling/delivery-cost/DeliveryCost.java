import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class DeliveryCost {

    // ═══════════════════════════════════════════════════════════════════════════
    // Delivery Driver Payment Tracker
    //
    // Food delivery companies employ tens of thousands of drivers who each
    // submit hundreds of deliveries per week. Build a live-dashboard backend
    // that tracks total payout cost.
    //
    //   addDriver(driverId, usdHourlyRate) — register a driver
    //   recordDelivery(driverId, start, end) — log a completed delivery
    //   getTotalCost()   — aggregate cost across ALL drivers  (live dashboard)
    //   getDriverCost(id) — cost for a single driver
    //   getDriverReport() — per-driver breakdown
    //
    // Key rules:
    //   • Each delivery is paid independently: cost = rate × duration_hours
    //   • Drivers CAN work multiple deliveries simultaneously — each is paid in full.
    //   • No overlap de-duplication: two 1-hour concurrent deliveries → 2 h of pay.
    //
    // Why NOT double/float for money?
    //   IEEE-754 cannot represent most decimal fractions exactly.
    //   e.g. 0.1 + 0.2 == 0.30000000000000004 in double.
    //   Accumulated rounding errors cause real accounting discrepancies at scale.
    //   Fix: store rates as BigDecimal; accumulate in cents (long) and convert only
    //   for display, OR keep everything in BigDecimal with explicit scale + HALF_UP.
    //   Here we use BigDecimal throughout for correctness.
    //
    // Time format: "HH:MM" or "HH:MM:SS"  (seconds-level precision per spec)
    // Complexity:
    //   addDriver        O(1)
    //   recordDelivery   O(1)  — just accumulate cost into a running total
    //   getTotalCost     O(1)  — single read of the running total
    //   getDriverCost    O(1)
    // ═══════════════════════════════════════════════════════════════════════════

    static class PaymentTracker {
        private static final int SECONDS_PER_HOUR = 3600;

        private static class DriverRecord {
            final int id;
            final BigDecimal ratePerHour;
            BigDecimal totalCost = BigDecimal.ZERO;
            int deliveryCount = 0;

            DriverRecord(int id, BigDecimal ratePerHour) {
                this.id = id;
                this.ratePerHour = ratePerHour;
            }
        }

        private final Map<Integer, DriverRecord> drivers = new HashMap<>();
        private BigDecimal grandTotal = BigDecimal.ZERO;

        // usdHourlyRate accepted as double from caller (API boundary) → immediately
        // converted to BigDecimal so all internal arithmetic stays exact.
        public void addDriver(int driverId, double usdHourlyRate) {
            if (drivers.containsKey(driverId))
                throw new IllegalArgumentException("Driver already exists: " + driverId);
            drivers.put(driverId, new DriverRecord(driverId, BigDecimal.valueOf(usdHourlyRate)));
        }

        // start / end: "HH:MM" or "HH:MM:SS"
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

        public BigDecimal getTotalCost() { return grandTotal; }

        public BigDecimal getDriverCost(int driverId) {
            DriverRecord dr = drivers.get(driverId);
            if (dr == null) throw new IllegalArgumentException("Unknown driver: " + driverId);
            return dr.totalCost;
        }

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

        // Parse "HH:MM" or "HH:MM:SS" → seconds since midnight
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
