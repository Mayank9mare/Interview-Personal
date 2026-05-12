package com.uber.ridebooking;

import com.uber.ridebooking.model.*;
import com.uber.ridebooking.strategy.BaseFareCalculator;
import com.uber.ridebooking.strategy.SurgeFareCalculator;

import java.util.List;

/**
 * Demo runner showcasing the complete ride booking lifecycle.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("=== Uber Ride Booking System Demo ===\n");

        RideService service = new RideService(new BaseFareCalculator());

        // ----------------------------------------------------------------
        // 1. Register riders
        // ----------------------------------------------------------------
        String rider1Id = service.registerRider("Alice");
        String rider2Id = service.registerRider("Bob");
        System.out.println("Registered riders:");
        System.out.println("  " + service.getRider(rider1Id));
        System.out.println("  " + service.getRider(rider2Id));

        // ----------------------------------------------------------------
        // 2. Register drivers at various locations
        // ----------------------------------------------------------------
        String driver1Id = service.registerDriver("Charlie",  0.0,  0.0);
        String driver2Id = service.registerDriver("Diana",    1.0,  1.0);
        String driver3Id = service.registerDriver("Eve",     10.0, 10.0);
        System.out.println("\nRegistered drivers:");
        System.out.println("  " + service.getDriver(driver1Id));
        System.out.println("  " + service.getDriver(driver2Id));
        System.out.println("  " + service.getDriver(driver3Id));

        // ----------------------------------------------------------------
        // 3. Alice requests a SEDAN ride
        // ----------------------------------------------------------------
        Location pickup  = new Location(0.5, 0.5);
        Location dropoff = new Location(4.5, 4.5);
        RideRequest request = service.requestRide(rider1Id, pickup, dropoff, RideType.SEDAN);
        System.out.println("\nRide requested: " + request);

        // ----------------------------------------------------------------
        // 4. Show nearby drivers within radius 5
        // ----------------------------------------------------------------
        List<Driver> nearby = service.getNearbyDrivers(pickup, 5.0);
        System.out.println("\nNearby drivers (radius=5.0):");
        nearby.forEach(d -> {
            double dist = d.getCurrentLocation().distanceTo(pickup);
            System.out.printf("  %s  (distance=%.3f)%n", d.getName(), dist);
        });

        // ----------------------------------------------------------------
        // 5. Charlie accepts the ride
        // ----------------------------------------------------------------
        boolean accepted = service.acceptRide(driver1Id, request.getId());
        System.out.println("\nCharlie accepted: " + accepted);

        Trip trip = service.getTripByRequestId(request.getId());
        System.out.println("Trip created:    " + trip);
        System.out.println("Charlie available: " + service.getDriver(driver1Id).isAvailable());

        // ----------------------------------------------------------------
        // 6. Start the trip
        // ----------------------------------------------------------------
        boolean started = service.startTrip(driver1Id, trip.getId());
        System.out.println("\nTrip started: " + started + " | Status: " + trip.getStatus());

        // ----------------------------------------------------------------
        // 7. Complete the trip
        // ----------------------------------------------------------------
        boolean completed = service.completeTrip(driver1Id, trip.getId());
        System.out.printf("%nTrip completed: %b | Fare: %.2f | Status: %s%n",
                completed, trip.getFare(), trip.getStatus());
        System.out.println("Charlie available again: " + service.getDriver(driver1Id).isAvailable());

        // ----------------------------------------------------------------
        // 8. Trip histories
        // ----------------------------------------------------------------
        System.out.println("\n--- Alice's Trip History ---");
        service.getRiderHistory(rider1Id).forEach(t ->
                System.out.printf("  %s%n", t));

        System.out.println("--- Charlie's Trip History ---");
        service.getDriverHistory(driver1Id).forEach(t ->
                System.out.printf("  %s%n", t));

        // ----------------------------------------------------------------
        // 9. Demonstrate cancellation by rider
        // ----------------------------------------------------------------
        System.out.println("\n--- Cancellation Demo ---");
        RideRequest request2 = service.requestRide(rider2Id,
                new Location(1.0, 1.0), new Location(3.0, 3.0), RideType.AUTO);
        System.out.println("Bob requested: " + request2.getStatus());

        boolean cancelled = service.cancelRide(rider2Id, request2.getId());
        System.out.println("Bob cancelled:  " + cancelled + " | Status: " + request2.getStatus());

        // ----------------------------------------------------------------
        // 10. Demonstrate cancellation of an accepted trip by driver
        // ----------------------------------------------------------------
        RideRequest request3 = service.requestRide(rider1Id,
                new Location(0.0, 0.0), new Location(2.0, 2.0), RideType.SUV);
        service.acceptRide(driver2Id, request3.getId());
        Trip trip3 = service.getTripByRequestId(request3.getId());
        System.out.println("\nDiana accepted Alice's SUV ride | Diana available: "
                + service.getDriver(driver2Id).isAvailable());

        boolean driverCancelled = service.cancelRide(driver2Id, trip3.getId());
        System.out.println("Diana cancelled: " + driverCancelled
                + " | Trip status: " + trip3.getStatus()
                + " | Diana available again: " + service.getDriver(driver2Id).isAvailable());

        // ----------------------------------------------------------------
        // 11. Surge pricing demo
        // ----------------------------------------------------------------
        System.out.println("\n--- Surge Pricing Demo (1.5x) ---");
        RideService surgeService = new RideService(
                new SurgeFareCalculator(new BaseFareCalculator(), 1.5));
        String sRider  = surgeService.registerRider("Sam");
        String sDriver = surgeService.registerDriver("Tom", 0.0, 0.0);
        RideRequest sReq = surgeService.requestRide(sRider,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.SEDAN);
        surgeService.acceptRide(sDriver, sReq.getId());
        Trip sTrip = surgeService.getTripByRequestId(sReq.getId());
        surgeService.startTrip(sDriver, sTrip.getId());
        surgeService.completeTrip(sDriver, sTrip.getId());
        System.out.printf("Distance=5.0 km, SEDAN base=(50 + 12*5)=110  => surge fare=%.2f%n",
                sTrip.getFare());

        System.out.println("\n=== Demo complete ===");
    }
}
