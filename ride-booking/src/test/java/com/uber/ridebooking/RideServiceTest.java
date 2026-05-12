package com.uber.ridebooking;

import com.uber.ridebooking.model.*;
import com.uber.ridebooking.strategy.BaseFareCalculator;
import com.uber.ridebooking.strategy.SurgeFareCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RideServiceTest {

    private RideService service;

    // Common test fixtures
    private String riderId;
    private String driverId;

    @BeforeEach
    void setUp() {
        service = new RideService(new BaseFareCalculator());
        riderId  = service.registerRider("Alice");
        driverId = service.registerDriver("Bob", 0.0, 0.0);
    }

    // ---------------------------------------------------------------
    // 1. registerRider_returnsId
    // ---------------------------------------------------------------
    @Test
    void registerRider_returnsId() {
        String id = service.registerRider("Charlie");
        assertNotNull(id, "registerRider should return a non-null ID");
        assertFalse(id.isBlank(), "ID should not be blank");

        Rider rider = service.getRider(id);
        assertNotNull(rider);
        assertEquals("Charlie", rider.getName());
    }

    // ---------------------------------------------------------------
    // 2. registerDriver_returnsId
    // ---------------------------------------------------------------
    @Test
    void registerDriver_returnsId() {
        String id = service.registerDriver("Diana", 12.9, 77.6);
        assertNotNull(id, "registerDriver should return a non-null ID");
        assertFalse(id.isBlank());

        Driver driver = service.getDriver(id);
        assertNotNull(driver);
        assertEquals("Diana", driver.getName());
        assertEquals(12.9, driver.getCurrentLocation().getLat(), 1e-9);
        assertEquals(77.6, driver.getCurrentLocation().getLon(), 1e-9);
        assertTrue(driver.isAvailable(), "New driver should be available");
    }

    // ---------------------------------------------------------------
    // 3. requestRide_createsRequest
    // ---------------------------------------------------------------
    @Test
    void requestRide_createsRequest() {
        Location pickup  = new Location(1.0, 1.0);
        Location dropoff = new Location(4.0, 5.0);

        RideRequest req = service.requestRide(riderId, pickup, dropoff, RideType.SEDAN);

        assertNotNull(req);
        assertNotNull(req.getId());
        assertEquals(TripStatus.REQUESTED, req.getStatus());
        assertEquals(RideType.SEDAN, req.getType());
        assertEquals(riderId, req.getRider().getId());
    }

    // ---------------------------------------------------------------
    // 4. acceptRide_assignsDriverAndCreatesTrip
    // ---------------------------------------------------------------
    @Test
    void acceptRide_assignsDriverAndCreatesTrip() {
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.AUTO);

        boolean accepted = service.acceptRide(driverId, req.getId());
        assertTrue(accepted, "acceptRide should return true");

        // Request status updated
        assertEquals(TripStatus.ACCEPTED, req.getStatus());

        // Driver is now unavailable
        Driver driver = service.getDriver(driverId);
        assertFalse(driver.isAvailable(), "Driver should be marked unavailable after accepting");

        // A Trip was created
        Trip trip = service.getTripByRequestId(req.getId());
        assertNotNull(trip, "A trip must be created on acceptRide");
        assertEquals(TripStatus.ACCEPTED, trip.getStatus());
        assertEquals(driverId, trip.getDriver().getId());
    }

    // ---------------------------------------------------------------
    // 5. startTrip_changesStatusToInProgress
    // ---------------------------------------------------------------
    @Test
    void startTrip_changesStatusToInProgress() {
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.SEDAN);
        service.acceptRide(driverId, req.getId());
        Trip trip = service.getTripByRequestId(req.getId());

        boolean started = service.startTrip(driverId, trip.getId());
        assertTrue(started);
        assertEquals(TripStatus.IN_PROGRESS, trip.getStatus());
        assertTrue(trip.getStartTime() > 0, "startTime should be recorded");
    }

    // ---------------------------------------------------------------
    // 6. completeTrip_calculatesfare_andUpdatesHistories
    // ---------------------------------------------------------------
    @Test
    void completeTrip_calculatesfare_andUpdatesHistories() {
        // Pickup=(0,0) -> Dropoff=(3,4)  distance = sqrt(9+16) = 5.0 km
        // SEDAN: baseFare=50, perKmRate=12  => fare = 50 + 12*5 = 110.0
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.SEDAN);
        service.acceptRide(driverId, req.getId());
        Trip trip = service.getTripByRequestId(req.getId());
        service.startTrip(driverId, trip.getId());

        boolean completed = service.completeTrip(driverId, trip.getId());
        assertTrue(completed);

        // Status
        assertEquals(TripStatus.COMPLETED, trip.getStatus());

        // Fare calculation
        assertEquals(110.0, trip.getFare(), 0.01,
                "Fare should be baseFare(50) + perKmRate(12)*distance(5) = 110");

        // Histories
        List<Trip> riderHistory = service.getRiderHistory(riderId);
        assertEquals(1, riderHistory.size());
        assertEquals(trip.getId(), riderHistory.get(0).getId());

        List<Trip> driverHistory = service.getDriverHistory(driverId);
        assertEquals(1, driverHistory.size());
        assertEquals(trip.getId(), driverHistory.get(0).getId());
    }

    // ---------------------------------------------------------------
    // 7. cancelRide_byRider_cancelsRequest
    // ---------------------------------------------------------------
    @Test
    void cancelRide_byRider_cancelsRequest() {
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(1.0, 1.0), RideType.AUTO);

        boolean cancelled = service.cancelRide(riderId, req.getId());
        assertTrue(cancelled);
        assertEquals(TripStatus.CANCELLED, req.getStatus());

        // Another cancel attempt should fail
        boolean again = service.cancelRide(riderId, req.getId());
        assertFalse(again, "Cancelling an already-cancelled request should return false");
    }

    // ---------------------------------------------------------------
    // 8. getNearbyDrivers_returnsWithinRadius
    // ---------------------------------------------------------------
    @Test
    void getNearbyDrivers_returnsWithinRadius() {
        // driverId (Bob) is at (0,0)
        String d2 = service.registerDriver("Carol", 3.0, 4.0);   // distance = 5.0
        String d3 = service.registerDriver("Dave",  6.0, 8.0);   // distance = 10.0

        Location query = new Location(0.0, 0.0);

        List<Driver> within5  = service.getNearbyDrivers(query, 5.0);
        List<Driver> within11 = service.getNearbyDrivers(query, 11.0);

        // Bob (dist=0) and Carol (dist=5) are within 5
        assertEquals(2, within5.size());
        // Bob is closest
        assertEquals("Bob", within5.get(0).getName());

        // All three within 11
        assertEquals(3, within11.size());
    }

    // ---------------------------------------------------------------
    // 9. completeTrip_makesDriverAvailableAgain
    // ---------------------------------------------------------------
    @Test
    void completeTrip_makesDriverAvailableAgain() {
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.SUV);
        service.acceptRide(driverId, req.getId());
        Trip trip = service.getTripByRequestId(req.getId());
        service.startTrip(driverId, trip.getId());

        Driver driver = service.getDriver(driverId);
        assertFalse(driver.isAvailable(), "Should be unavailable mid-trip");

        service.completeTrip(driverId, trip.getId());
        assertTrue(driver.isAvailable(), "Driver should be available after completing trip");
    }

    // ---------------------------------------------------------------
    // 10. surgeFareCalculator_appliesMultiplier
    // ---------------------------------------------------------------
    @Test
    void surgeFareCalculator_appliesMultiplier() {
        RideService surgeService = new RideService(
                new SurgeFareCalculator(new BaseFareCalculator(), 2.0));

        String sRider  = surgeService.registerRider("Sam");
        String sDriver = surgeService.registerDriver("Tom", 0.0, 0.0);

        // distance = 5.0 km; SEDAN base fare = 110; surge 2x = 220
        RideRequest req = surgeService.requestRide(sRider,
                new Location(0.0, 0.0), new Location(3.0, 4.0), RideType.SEDAN);
        surgeService.acceptRide(sDriver, req.getId());
        Trip trip = surgeService.getTripByRequestId(req.getId());
        surgeService.startTrip(sDriver, trip.getId());
        surgeService.completeTrip(sDriver, trip.getId());

        assertEquals(220.0, trip.getFare(), 0.01,
                "Surge 2x on 110 base should be 220");
    }

    // ---------------------------------------------------------------
    // 11. cancelRide_byDriver_onAcceptedTrip_releasesDriver
    // ---------------------------------------------------------------
    @Test
    void cancelRide_byDriver_onAcceptedTrip_releasesDriver() {
        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(1.0, 1.0), RideType.SEDAN);
        service.acceptRide(driverId, req.getId());
        Trip trip = service.getTripByRequestId(req.getId());

        Driver driver = service.getDriver(driverId);
        assertFalse(driver.isAvailable());

        boolean cancelled = service.cancelRide(driverId, trip.getId());
        assertTrue(cancelled);
        assertEquals(TripStatus.CANCELLED, trip.getStatus());
        assertTrue(driver.isAvailable(), "Driver should be available again after cancelling accepted trip");
    }

    // ---------------------------------------------------------------
    // 12. acceptRide_sameRequestTwice_returnsFalse
    // ---------------------------------------------------------------
    @Test
    void acceptRide_sameRequestTwice_returnsFalse() {
        String driver2Id = service.registerDriver("Zara", 0.5, 0.5);

        RideRequest req = service.requestRide(riderId,
                new Location(0.0, 0.0), new Location(1.0, 1.0), RideType.AUTO);

        assertTrue(service.acceptRide(driverId, req.getId()));
        // Second driver tries to accept the same request
        assertFalse(service.acceptRide(driver2Id, req.getId()),
                "Should not accept an already-accepted request");
    }
}
