// Companies: Amazon
// Multi-floor parking lot — OOP hierarchy for vehicle/spot assignment.

import java.util.*;

/**
 * Multi-floor parking lot supporting three vehicle sizes (Motorcycle, Car, Bus)
 * and three spot sizes (Small, Medium, Large).
 *
 * <p>Each {@link ParkingFloor} maintains per-size queues of available spots for O(1)
 * assignment. A vehicle is assigned the <em>smallest fitting spot</em> on the
 * <em>lowest available floor</em> to maximise utilisation.
 *
 * <p>Size-compatibility matrix (vehicle → acceptable spot sizes, preferred first):
 * <ul>
 *   <li>Motorcycle → Small, Medium, Large</li>
 *   <li>Car         → Medium, Large</li>
 *   <li>Bus         → Large only</li>
 * </ul>
 *
 * <p>Core invariant: every spot appears in exactly one of (i) a floor's available
 * queue or (ii) the active ticket map — never both, never neither.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class ParkingLot {

    // ── Enums ─────────────────────────────────────────────────────────────

    enum VehicleType { MOTORCYCLE, CAR, BUS }
    enum SpotSize    { SMALL, MEDIUM, LARGE }

    // ── Domain classes ────────────────────────────────────────────────────

    /** An incoming vehicle with a type and licence plate. */
    static class Vehicle {
        final VehicleType type;
        final String      plate;
        Vehicle(VehicleType type, String plate) { this.type = type; this.plate = plate; }
    }

    /** One physical parking spot on a specific floor. */
    static class ParkingSpot {
        /** Human-readable identifier, e.g. "F1-S3". */
        final String   spotId;
        final SpotSize size;
        final int      floorNum;  // needed so unpark can return spot to the right floor
        Vehicle parkedVehicle;    // null when empty

        ParkingSpot(String spotId, SpotSize size, int floorNum) {
            this.spotId   = spotId;
            this.size     = size;
            this.floorNum = floorNum;
        }
    }

    /**
     * Issued when a vehicle parks; must be presented to unpark.
     * Holds a direct reference to the occupied spot for O(1) release.
     */
    static class ParkingTicket {
        final String      ticketId;
        final Vehicle     vehicle;
        final ParkingSpot spot;
        final long        entryTimeMs;

        ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpot spot) {
            this.ticketId    = ticketId;
            this.vehicle     = vehicle;
            this.spot        = spot;
            this.entryTimeMs = System.currentTimeMillis();
        }
    }

    // ── ParkingFloor ──────────────────────────────────────────────────────

    /**
     * One floor of the parking lot. Maintains per-size queues of available spots
     * so assignment is O(1) rather than a linear scan.
     */
    static class ParkingFloor {
        final int floorNum;

        /** Available spots grouped by size for fast O(1) retrieval. */
        private final Map<SpotSize, Queue<ParkingSpot>> available = new EnumMap<>(SpotSize.class);

        /**
         * Constructs a floor with the given number of spots of each size.
         *
         * @param floorNum   floor number (1-based, used in spotId generation)
         * @param small      number of small spots
         * @param medium     number of medium spots
         * @param large      number of large spots
         */
        ParkingFloor(int floorNum, int small, int medium, int large) {
            this.floorNum = floorNum;
            for (SpotSize s : SpotSize.values())
                available.put(s, new LinkedList<>());
            for (int i = 1; i <= small;  i++)
                available.get(SpotSize.SMALL).add(new ParkingSpot("F"+floorNum+"-S"+i, SpotSize.SMALL, floorNum));
            for (int i = 1; i <= medium; i++)
                available.get(SpotSize.MEDIUM).add(new ParkingSpot("F"+floorNum+"-M"+i, SpotSize.MEDIUM, floorNum));
            for (int i = 1; i <= large;  i++)
                available.get(SpotSize.LARGE).add(new ParkingSpot("F"+floorNum+"-L"+i, SpotSize.LARGE, floorNum));
        }

        /**
         * Removes and returns the first available spot of {@code size}, or null if none.
         */
        ParkingSpot takeSpot(SpotSize size) {
            Queue<ParkingSpot> q = available.get(size);
            return q.isEmpty() ? null : q.poll();
        }

        /** Returns a freed spot back to this floor's available queue. */
        void returnSpot(ParkingSpot spot) {
            available.get(spot.size).add(spot);
        }

        int availableCount(SpotSize size) { return available.get(size).size(); }
    }

    // ── ParkingLot fields ─────────────────────────────────────────────────

    /** All floors in ascending order (search starts from floor 1). */
    private final List<ParkingFloor> floors;

    /**
     * Active tickets keyed by ticketId for O(1) unpark lookup.
     * Removed from this map when the vehicle exits.
     */
    private final Map<String, ParkingTicket> activeTickets = new HashMap<>();

    private int ticketCounter = 0;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Constructs a parking lot with {@code numFloors} identical floors.
     *
     * @param numFloors number of floors (1-based)
     * @param small     small spots per floor (for motorcycles)
     * @param medium    medium spots per floor (for cars)
     * @param large     large spots per floor (for buses)
     */
    public ParkingLot(int numFloors, int small, int medium, int large) {
        floors = new ArrayList<>();
        for (int f = 1; f <= numFloors; f++)
            floors.add(new ParkingFloor(f, small, medium, large));
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Parks the vehicle in the smallest fitting spot on the lowest available floor.
     *
     * <p>Iterates fitting spot sizes in preference order (smallest first), and for
     * each size searches all floors before trying a larger size. Returns {@code null}
     * if the lot is full for this vehicle type.
     *
     * @param vehicle the vehicle to park
     * @return a {@link ParkingTicket} to use when leaving, or {@code null} if lot is full
     */
    public ParkingTicket park(Vehicle vehicle) {
        for (SpotSize size : fittingSizes(vehicle.type)) {
            for (ParkingFloor floor : floors) {
                ParkingSpot spot = floor.takeSpot(size);
                if (spot != null) {
                    spot.parkedVehicle = vehicle;
                    String tid = "T" + (++ticketCounter);
                    ParkingTicket ticket = new ParkingTicket(tid, vehicle, spot);
                    activeTickets.put(tid, ticket);
                    return ticket;
                }
            }
        }
        return null;  // lot is full for this vehicle type
    }

    /**
     * Removes the vehicle from its spot and frees it for future use.
     *
     * @param ticket the ticket issued at park time
     * @throws IllegalArgumentException if the ticket is unknown or already used
     */
    public void unpark(ParkingTicket ticket) {
        if (!activeTickets.containsKey(ticket.ticketId))
            throw new IllegalArgumentException("Unknown or already-used ticket: " + ticket.ticketId);
        activeTickets.remove(ticket.ticketId);
        ticket.spot.parkedVehicle = null;
        floors.get(ticket.spot.floorNum - 1).returnSpot(ticket.spot);
    }

    /**
     * Returns the total number of free spots of the given size across all floors.
     *
     * @param size the spot size to query
     * @return count of available spots of that size
     */
    public int getAvailableCount(SpotSize size) {
        int total = 0;
        for (ParkingFloor f : floors) total += f.availableCount(size);
        return total;
    }

    /** Prints a per-floor availability summary. */
    public void printStatus() {
        System.out.println("=== Parking Lot Status ===");
        for (ParkingFloor f : floors) {
            System.out.printf("  Floor %d - Small: %d, Medium: %d, Large: %d%n",
                    f.floorNum,
                    f.availableCount(SpotSize.SMALL),
                    f.availableCount(SpotSize.MEDIUM),
                    f.availableCount(SpotSize.LARGE));
        }
        System.out.println("  Active tickets: " + activeTickets.size());
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /**
     * Returns the spot sizes that can fit a given vehicle type, in preference
     * order (smallest first to maximise utilisation).
     */
    private static List<SpotSize> fittingSizes(VehicleType type) {
        switch (type) {
            case MOTORCYCLE: return Arrays.asList(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE);
            case CAR:        return Arrays.asList(SpotSize.MEDIUM, SpotSize.LARGE);
            case BUS:        return Arrays.asList(SpotSize.LARGE);
            default:         return Collections.emptyList();
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // 2 floors, each with 3 small + 2 medium + 1 large spot
        ParkingLot lot = new ParkingLot(2, 3, 2, 1);

        Vehicle moto = new Vehicle(VehicleType.MOTORCYCLE, "MH01-AB-1234");
        Vehicle car1 = new Vehicle(VehicleType.CAR,        "MH02-CD-5678");
        Vehicle car2 = new Vehicle(VehicleType.CAR,        "MH02-EF-9012");
        Vehicle bus1 = new Vehicle(VehicleType.BUS,        "MH03-GH-3456");
        Vehicle bus2 = new Vehicle(VehicleType.BUS,        "MH03-IJ-7890");
        Vehicle bus3 = new Vehicle(VehicleType.BUS,        "MH03-KL-0001");

        // Park motorcycle → uses small spot on floor 1
        ParkingTicket t1 = lot.park(moto);
        System.out.println("Parked moto: " + t1.ticketId + " at " + t1.spot.spotId);
        // Expected: T1 at F1-S1

        // Park car → uses medium spot on floor 1
        ParkingTicket t2 = lot.park(car1);
        System.out.println("Parked car1: " + t2.ticketId + " at " + t2.spot.spotId);
        // Expected: T2 at F1-M1

        // Park bus → uses the single large spot on floor 1
        ParkingTicket t3 = lot.park(bus1);
        System.out.println("Parked bus1: " + t3.ticketId + " at " + t3.spot.spotId);
        // Expected: T3 at F1-L1

        System.out.println("Available small:  " + lot.getAvailableCount(SpotSize.SMALL));
        // Expected: 5  (3 floor-1 + 3 floor-2 - 1 used = 5)
        System.out.println("Available large:  " + lot.getAvailableCount(SpotSize.LARGE));
        // Expected: 1  (floor 1 large is taken; floor 2 large is free)

        // Park second bus → goes to floor 2 (floor 1 large is full)
        ParkingTicket t4 = lot.park(bus2);
        System.out.println("Parked bus2: " + t4.ticketId + " at " + t4.spot.spotId);
        // Expected: T4 at F2-L1

        // All large spots taken — parking a third bus returns null
        ParkingTicket t5 = lot.park(bus3);
        System.out.println("Park bus3 (no large spots): " + (t5 == null ? "null (full)" : t5.ticketId));
        // Expected: null (full)

        // Unpark the motorcycle; spot should return to floor 1 small queue
        lot.unpark(t1);
        System.out.println("After unpark moto, available small: " + lot.getAvailableCount(SpotSize.SMALL));
        // Expected: 6

        // Car can overflow to a large spot when all medium spots are taken
        // Fill remaining medium spots first
        ParkingTicket t6 = lot.park(car2); // floor 1 has M2 free; floor 2 has M1, M2
        System.out.println("Parked car2: " + t6.ticketId + " at " + t6.spot.spotId);
        // Expected: T5 at F1-M2  (bus3 failed silently, no ticket issued)

        System.out.println();
        lot.printStatus();
    }
}
