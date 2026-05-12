package com.uber.parkinglot;

import com.uber.parkinglot.model.*;
import com.uber.parkinglot.strategy.HourlyPricingStrategy;
import com.uber.parkinglot.strategy.PricingStrategy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Demo runner for the Parking Lot system.
 */
public class App {
    public static void main(String[] args) {
        System.out.println("=== Parking Lot LLD Demo ===\n");

        // -------------------------------------------------------
        // 1. Create a 2-floor parking lot
        // -------------------------------------------------------
        // Floor 0: 1 motorcycle spot, 2 compact spots, 1 large spot
        List<ParkingSpot> floor0Spots = Arrays.asList(
                new MotorcycleSpot("F0-M1", 0),
                new CompactSpot("F0-C1", 0),
                new CompactSpot("F0-C2", 0),
                new LargeSpot("F0-L1", 0)
        );
        Floor floor0 = new Floor(0, floor0Spots);

        // Floor 1: 1 motorcycle spot, 2 compact spots, 1 large spot
        List<ParkingSpot> floor1Spots = Arrays.asList(
                new MotorcycleSpot("F1-M1", 1),
                new CompactSpot("F1-C1", 1),
                new CompactSpot("F1-C2", 1),
                new LargeSpot("F1-L1", 1)
        );
        Floor floor1 = new Floor(1, floor1Spots);

        PricingStrategy pricing = new HourlyPricingStrategy(
                Map.of(
                        VehicleType.MOTORCYCLE, 20.0,
                        VehicleType.CAR, 40.0,
                        VehicleType.TRUCK, 60.0
                )
        );

        ParkingLot lot = new ParkingLot("Downtown Parking", Arrays.asList(floor0, floor1), pricing);
        System.out.println("Created: " + lot);

        // -------------------------------------------------------
        // 2. Park a motorcycle, car, and truck
        // -------------------------------------------------------
        Vehicle moto  = new Motorcycle("V001", "MOTO-123");
        Vehicle car   = new Car("V002", "CAR-456");
        Vehicle truck = new Truck("V003", "TRUCK-789");

        Ticket motoTicket  = lot.park(moto);
        Ticket carTicket   = lot.park(car);
        Ticket truckTicket = lot.park(truck);

        System.out.println("\n--- Parked vehicles ---");
        System.out.println("Motorcycle ticket : " + motoTicket.getTicketId() +
                           " | Spot: " + motoTicket.getSpot().getSpotId());
        System.out.println("Car ticket        : " + carTicket.getTicketId() +
                           " | Spot: " + carTicket.getSpot().getSpotId());
        System.out.println("Truck ticket      : " + truckTicket.getTicketId() +
                           " | Spot: " + truckTicket.getSpot().getSpotId());

        // -------------------------------------------------------
        // 3. Show available spots
        // -------------------------------------------------------
        System.out.println("\n--- Available spots ---");
        System.out.println("Motorcycle spots available : " + lot.getAvailableSpotCount(VehicleType.MOTORCYCLE));
        System.out.println("Car spots available        : " + lot.getAvailableSpotCount(VehicleType.CAR));
        System.out.println("Truck spots available      : " + lot.getAvailableSpotCount(VehicleType.TRUCK));

        // -------------------------------------------------------
        // 4. Unpark the car (simulate 2 hours parked)
        // -------------------------------------------------------
        // Manually back-date entry time to simulate 2 hours
        carTicket.setEntryTime(System.currentTimeMillis() - 2 * 3_600_000L);
        double carFee = lot.unpark(carTicket.getTicketId());
        System.out.println("\n--- Unparked car ---");
        System.out.printf("Car fee (2 hours @ $40/hr): $%.2f%n", carFee);

        // -------------------------------------------------------
        // 5. Park another car
        // -------------------------------------------------------
        Vehicle car2 = new Car("V004", "CAR-999");
        Ticket car2Ticket = lot.park(car2);
        System.out.println("\n--- Parked second car ---");
        System.out.println("Car2 ticket : " + car2Ticket.getTicketId() +
                           " | Spot: " + car2Ticket.getSpot().getSpotId());

        // -------------------------------------------------------
        // 6. Show active tickets
        // -------------------------------------------------------
        System.out.println("\n--- Active tickets (" + lot.getActiveTickets().size() + ") ---");
        lot.getActiveTickets().forEach(t ->
                System.out.println("  " + t.getTicketId() + " | " +
                        t.getVehicle().getType() + " | Spot: " + t.getSpot().getSpotId()));

        // -------------------------------------------------------
        // 7. Fill the lot and try to park when full
        // -------------------------------------------------------
        // Current state: moto on F0-M1, truck on F0-L1, car2 on F0-C1
        // Free compact spots: F0-C2, F1-C1, F1-C2; free large: F1-L1; free moto: F1-M1
        // Park cars until compact and large spots are full
        lot.park(new Car("V005", "CAR-A01"));  // F0-C2
        lot.park(new Car("V006", "CAR-A02"));  // F1-C1
        lot.park(new Car("V007", "CAR-A03"));  // F1-C2
        lot.park(new Truck("V008", "TRUCK-A04")); // F1-L1 (only large left)
        // Now attempt to park another car — should return null
        Ticket overflow = lot.park(new Car("V009", "CAR-OVERFLOW"));
        System.out.println("\n--- Park when full ---");
        System.out.println("Attempt to park car when full: " + (overflow == null ? "null (no spot)" : overflow.getTicketId()));

        System.out.println("\n=== Demo complete ===");
    }
}
