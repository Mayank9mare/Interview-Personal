package com.uber.cabdispatch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CabDispatchServiceTest {
    @Test
    void requestRideAssignsNearestAvailableDriver() {
        CabDispatchService service = new CabDispatchService();
        service.addDriver("far", new CabDispatchService.Location(10, 10));
        service.addDriver("near", new CabDispatchService.Location(1, 1));

        CabDispatchService.Trip trip = service.requestRide("r1",
                new CabDispatchService.Location(0, 0),
                new CabDispatchService.Location(5, 5));

        assertEquals("near", trip.driverId);
        assertEquals(CabDispatchService.DriverStatus.ON_TRIP, service.getDriver("near").status);
    }

    @Test
    void completeTripReleasesDriverAtDropLocation() {
        CabDispatchService service = new CabDispatchService();
        service.addDriver("d1", new CabDispatchService.Location(0, 0));
        CabDispatchService.Trip trip = service.requestRide("r1",
                new CabDispatchService.Location(0, 0),
                new CabDispatchService.Location(5, 5));

        service.completeTrip(trip.tripId);

        assertEquals(CabDispatchService.TripStatus.COMPLETED, trip.status);
        assertEquals(CabDispatchService.DriverStatus.AVAILABLE, service.getDriver("d1").status);
        assertEquals(5, service.getDriver("d1").location.lat);
    }
}
