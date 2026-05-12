package com.uber.parkinglot;

import com.uber.parkinglot.model.*;
import com.uber.parkinglot.strategy.HourlyPricingStrategy;
import com.uber.parkinglot.strategy.PricingStrategy;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParkingLotTest {

    /**
     * Helper: builds a 2-floor test lot.
     * Floor 0: 1 motorcycle spot, 2 compact spots, 1 large spot
     * Floor 1: 1 motorcycle spot, 2 compact spots, 1 large spot
     */
    private ParkingLot createTestLot() {
        List<ParkingSpot> floor0Spots = Arrays.asList(
                new MotorcycleSpot("F0-M1", 0),
                new CompactSpot("F0-C1", 0),
                new CompactSpot("F0-C2", 0),
                new LargeSpot("F0-L1", 0)
        );
        List<ParkingSpot> floor1Spots = Arrays.asList(
                new MotorcycleSpot("F1-M1", 1),
                new CompactSpot("F1-C1", 1),
                new CompactSpot("F1-C2", 1),
                new LargeSpot("F1-L1", 1)
        );
        Floor floor0 = new Floor(0, floor0Spots);
        Floor floor1 = new Floor(1, floor1Spots);

        PricingStrategy pricing = new HourlyPricingStrategy(
                Map.of(
                        VehicleType.MOTORCYCLE, 20.0,
                        VehicleType.CAR, 40.0,
                        VehicleType.TRUCK, 60.0
                )
        );
        return new ParkingLot("Test Lot", Arrays.asList(floor0, floor1), pricing);
    }

    // ------------------------------------------------------------------
    // 1. park_motorcycle_findsMotorcycleSpot
    // ------------------------------------------------------------------
    @Test
    void park_motorcycle_findsMotorcycleSpot() {
        ParkingLot lot = createTestLot();
        Vehicle moto = new Motorcycle("M1", "MOTO-001");

        Ticket ticket = lot.park(moto);

        assertNotNull(ticket, "Ticket should not be null");
        assertNotNull(ticket.getTicketId());
        assertEquals(SpotType.MOTORCYCLE, ticket.getSpot().getSpotType(),
                "Motorcycle should be assigned a MOTORCYCLE spot first");
        assertEquals(0, ticket.getSpot().getFloor(), "Should use floor 0 first");
    }

    // ------------------------------------------------------------------
    // 2. park_car_findsCompactSpot
    // ------------------------------------------------------------------
    @Test
    void park_car_findsCompactSpot() {
        ParkingLot lot = createTestLot();
        Vehicle car = new Car("C1", "CAR-001");

        Ticket ticket = lot.park(car);

        assertNotNull(ticket);
        assertEquals(SpotType.COMPACT, ticket.getSpot().getSpotType(),
                "Car should be assigned a COMPACT spot first");
        assertEquals(0, ticket.getSpot().getFloor());
    }

    // ------------------------------------------------------------------
    // 3. park_truck_findsLargeSpot
    // ------------------------------------------------------------------
    @Test
    void park_truck_findsLargeSpot() {
        ParkingLot lot = createTestLot();
        Vehicle truck = new Truck("T1", "TRUCK-001");

        Ticket ticket = lot.park(truck);

        assertNotNull(ticket);
        assertEquals(SpotType.LARGE, ticket.getSpot().getSpotType(),
                "Truck should be assigned a LARGE spot");
        assertEquals(0, ticket.getSpot().getFloor());
    }

    // ------------------------------------------------------------------
    // 4. park_whenFull_returnsNull
    // ------------------------------------------------------------------
    @Test
    void park_whenFull_returnsNull() {
        ParkingLot lot = createTestLot();
        // Park 4 trucks to fill all 2 large spots (F0-L1, F1-L1)
        // Then park cars in compact spots (4 compact total) and motorcycles in motorcycle spots
        // Easiest: fill every truck spot, then try a truck
        lot.park(new Truck("T1", "TRUCK-A"));  // F0-L1
        lot.park(new Truck("T2", "TRUCK-B"));  // F1-L1
        // No large spots remain for trucks
        Ticket t = lot.park(new Truck("T3", "TRUCK-C"));
        assertNull(t, "Should return null when no spot is available for truck");
    }

    // ------------------------------------------------------------------
    // 5. unpark_calculatesFeeAndFreesSpot
    // ------------------------------------------------------------------
    @Test
    void unpark_calculatesFeeAndFreesSpot() {
        ParkingLot lot = createTestLot();
        Vehicle car = new Car("C1", "CAR-001");
        Ticket ticket = lot.park(car);
        assertNotNull(ticket);

        // Simulate exactly 1 hour parked
        ticket.setEntryTime(System.currentTimeMillis() - 3_600_000L);

        double fee = lot.unpark(ticket.getTicketId());

        assertEquals(40.0, fee, 0.01, "1 hour car fee should be $40");
        assertTrue(ticket.getSpot().isAvailable(), "Spot should be free after unpark");
        assertFalse(lot.getActiveTickets().contains(ticket), "Ticket should not be active after unpark");
    }

    // ------------------------------------------------------------------
    // 6. getAvailableSpotCount_decreasesOnPark
    // ------------------------------------------------------------------
    @Test
    void getAvailableSpotCount_decreasesOnPark() {
        ParkingLot lot = createTestLot();
        // Initially 4 compact spots total across 2 floors (also motorcycle can use compact)
        int beforeCar = lot.getAvailableSpotCount(VehicleType.CAR);

        lot.park(new Car("C1", "CAR-001"));

        int afterCar = lot.getAvailableSpotCount(VehicleType.CAR);
        assertEquals(beforeCar - 1, afterCar,
                "Available count should decrease by 1 after parking a car");
    }

    // ------------------------------------------------------------------
    // 7. getAvailableSpotCount_increasesOnUnpark
    // ------------------------------------------------------------------
    @Test
    void getAvailableSpotCount_increasesOnUnpark() {
        ParkingLot lot = createTestLot();
        Ticket ticket = lot.park(new Car("C1", "CAR-001"));
        assertNotNull(ticket);

        int afterPark = lot.getAvailableSpotCount(VehicleType.CAR);
        lot.unpark(ticket.getTicketId());
        int afterUnpark = lot.getAvailableSpotCount(VehicleType.CAR);

        assertEquals(afterPark + 1, afterUnpark,
                "Available count should increase by 1 after unparking a car");
    }

    // ------------------------------------------------------------------
    // 8. park_preferLowerFloor
    // ------------------------------------------------------------------
    @Test
    void park_preferLowerFloor() {
        ParkingLot lot = createTestLot();

        // Floor 0 has: 1 motorcycle spot, 2 compact spots, 1 large spot
        // Cars fit in compact and large spots → 3 cars fit on floor 0 before floor 1 is needed
        Ticket t1 = lot.park(new Car("C1", "CAR-001")); // F0-C1
        Ticket t2 = lot.park(new Car("C2", "CAR-002")); // F0-C2
        Ticket t3 = lot.park(new Car("C3", "CAR-003")); // F0-L1 (large fits car)

        assertNotNull(t1);
        assertNotNull(t2);
        assertNotNull(t3);
        assertEquals(0, t1.getSpot().getFloor(), "First car should park on floor 0");
        assertEquals(0, t2.getSpot().getFloor(), "Second car should park on floor 0");
        assertEquals(0, t3.getSpot().getFloor(), "Third car should park on floor 0 (large spot)");

        // Fourth car must go to floor 1 (all car-compatible spots on floor 0 are full)
        Ticket t4 = lot.park(new Car("C4", "CAR-004"));
        assertNotNull(t4);
        assertEquals(1, t4.getSpot().getFloor(), "Fourth car should park on floor 1");
    }

    // ------------------------------------------------------------------
    // 9. motorcycle_canParkInCompactSpot_whenMotorcycleSpotFull
    // ------------------------------------------------------------------
    @Test
    void motorcycle_canParkInCompactSpot_whenMotorcycleSpotFull() {
        ParkingLot lot = createTestLot();

        // The first motorcycle takes F0-M1 (motorcycle spot on floor 0).
        // Subsequent motorcycles can also fit compact spots (floor 0 iteration order:
        // motorcycle spot → compact spot → compact spot → large spot).
        // So motorcycle #2 will land on F0-C1 (compact), motorcycle #3 on F0-C2, etc.
        // The key behavior to test: a motorcycle CAN use compact spots when motorcycle spots are full.

        // Fill the only motorcycle spot on floor 0 first.
        Ticket t1 = lot.park(new Motorcycle("M1", "MOTO-001")); // F0-M1 (motorcycle spot)
        assertNotNull(t1);
        assertEquals(SpotType.MOTORCYCLE, t1.getSpot().getSpotType(),
                "First motorcycle should use the motorcycle spot");

        // Second motorcycle — motorcycle spot on floor 0 is taken, so it goes to compact
        Ticket t2 = lot.park(new Motorcycle("M2", "MOTO-002"));
        assertNotNull(t2);
        assertEquals(SpotType.COMPACT, t2.getSpot().getSpotType(),
                "Second motorcycle should use a compact spot when motorcycle spot is full");

        // Confirm it still parked on floor 0 (prefers lower floor)
        assertEquals(0, t2.getSpot().getFloor(),
                "Motorcycle should prefer the lower floor");
    }

    // ------------------------------------------------------------------
    // Extra: fee calculation for 2.5 hours (ceiled to 3 hours)
    // ------------------------------------------------------------------
    @Test
    void unpark_fee_ceilsToNextHour() {
        ParkingLot lot = createTestLot();
        Vehicle car = new Car("C1", "CAR-001");
        Ticket ticket = lot.park(car);
        assertNotNull(ticket);

        // 2.5 hours parked → ceil to 3 hours → 3 * 40 = 120
        ticket.setEntryTime(System.currentTimeMillis() - (long)(2.5 * 3_600_000));

        double fee = lot.unpark(ticket.getTicketId());
        assertEquals(120.0, fee, 0.01, "2.5 hour parking should be billed as 3 hours = $120");
    }

    // ------------------------------------------------------------------
    // Extra: unpark with invalid ticket throws exception
    // ------------------------------------------------------------------
    @Test
    void unpark_invalidTicket_throwsException() {
        ParkingLot lot = createTestLot();
        assertThrows(IllegalArgumentException.class,
                () -> lot.unpark("non-existent-id"),
                "Unparking with invalid ticket ID should throw IllegalArgumentException");
    }
}
