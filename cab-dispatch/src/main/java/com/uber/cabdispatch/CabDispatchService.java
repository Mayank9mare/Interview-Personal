package com.uber.cabdispatch;

import java.util.*;

public class CabDispatchService {
    public enum DriverStatus { AVAILABLE, ON_TRIP }
    public enum TripStatus { REQUESTED, ASSIGNED, COMPLETED, CANCELLED }

    public static class Location {
        public final double lat;
        public final double lon;

        public Location(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    public static class Driver {
        public final String driverId;
        public Location location;
        public DriverStatus status = DriverStatus.AVAILABLE;

        private Driver(String driverId, Location location) {
            this.driverId = driverId;
            this.location = location;
        }
    }

    public static class Trip {
        public final String tripId;
        public final String riderId;
        public final String driverId;
        public final Location pickup;
        public final Location drop;
        public TripStatus status;

        private Trip(String tripId, String riderId, String driverId, Location pickup, Location drop) {
            this.tripId = tripId;
            this.riderId = riderId;
            this.driverId = driverId;
            this.pickup = pickup;
            this.drop = drop;
            this.status = TripStatus.ASSIGNED;
        }
    }

    private final Map<String, Driver> drivers = new HashMap<>();
    private final Map<String, Trip> trips = new HashMap<>();

    public void addDriver(String driverId, Location location) {
        drivers.put(driverId, new Driver(driverId, location));
    }

    public void updateDriverLocation(String driverId, Location location) {
        requireDriver(driverId).location = location;
    }

    public Trip requestRide(String riderId, Location pickup, Location drop) {
        Driver driver = drivers.values().stream()
                .filter(d -> d.status == DriverStatus.AVAILABLE)
                .min(Comparator
                        .comparingDouble((Driver d) -> distanceSquared(d.location, pickup))
                        .thenComparing(d -> d.driverId))
                .orElseThrow(() -> new IllegalStateException("no drivers available"));

        driver.status = DriverStatus.ON_TRIP;
        Trip trip = new Trip(UUID.randomUUID().toString(), riderId, driver.driverId, pickup, drop);
        trips.put(trip.tripId, trip);
        return trip;
    }

    public void completeTrip(String tripId) {
        Trip trip = requireTrip(tripId);
        trip.status = TripStatus.COMPLETED;
        Driver driver = requireDriver(trip.driverId);
        driver.location = trip.drop;
        driver.status = DriverStatus.AVAILABLE;
    }

    public void cancelTrip(String tripId) {
        Trip trip = requireTrip(tripId);
        trip.status = TripStatus.CANCELLED;
        requireDriver(trip.driverId).status = DriverStatus.AVAILABLE;
    }

    public Driver getDriver(String driverId) {
        return requireDriver(driverId);
    }

    private Driver requireDriver(String driverId) {
        Driver driver = drivers.get(driverId);
        if (driver == null) throw new IllegalArgumentException("unknown driver");
        return driver;
    }

    private Trip requireTrip(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) throw new IllegalArgumentException("unknown trip");
        return trip;
    }

    private double distanceSquared(Location a, Location b) {
        double dx = a.lat - b.lat;
        double dy = a.lon - b.lon;
        return dx * dx + dy * dy;
    }
}
