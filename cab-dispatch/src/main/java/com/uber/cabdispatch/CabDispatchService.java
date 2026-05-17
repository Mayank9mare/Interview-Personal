package com.uber.cabdispatch;

import java.util.*;

/**
 * Simplified cab-dispatch service that matches riders with the nearest available driver.
 *
 * <p>Dispatch uses Euclidean squared distance (no square root needed for comparison)
 * to pick the closest available driver, breaking ties by driver ID lexicographically.
 * On trip completion the driver's location is updated to the drop-off point.
 *
 * <p>Data structures: two {@code HashMap}s for O(1) driver and trip lookups.
 *
 * <p>Not thread-safe.
 */
public class CabDispatchService {
    /** Availability state of a driver. */
    public enum DriverStatus { AVAILABLE, ON_TRIP }
    /** Lifecycle state of a trip. */
    public enum TripStatus { REQUESTED, ASSIGNED, COMPLETED, CANCELLED }

    /**
     * Geographic coordinate expressed as a latitude/longitude pair.
     */
    public static class Location {
        /** Latitude in decimal degrees. */
        public final double lat;
        /** Longitude in decimal degrees. */
        public final double lon;

        /**
         * @param lat latitude
         * @param lon longitude
         */
        public Location(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }

    /**
     * Mutable state of a registered driver.
     */
    public static class Driver {
        /** Unique driver identifier. */
        public final String driverId;
        /** Last known location; updated on trip completion. */
        public Location location;
        /** Current availability; starts as {@link DriverStatus#AVAILABLE}. */
        public DriverStatus status = DriverStatus.AVAILABLE;

        private Driver(String driverId, Location location) {
            this.driverId = driverId;
            this.location = location;
        }
    }

    /**
     * Mutable state of an active or completed trip.
     */
    public static class Trip {
        /** UUID-based unique trip identifier. */
        public final String tripId;
        /** Rider who requested the trip. */
        public final String riderId;
        /** Driver assigned to the trip. */
        public final String driverId;
        /** Pickup location. */
        public final Location pickup;
        /** Drop-off location. */
        public final Location drop;
        /** Current lifecycle status. */
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

    /** driverId → Driver. */
    private final Map<String, Driver> drivers = new HashMap<>();
    /** tripId → Trip. */
    private final Map<String, Trip> trips = new HashMap<>();

    /**
     * Registers a new driver at the given location.
     *
     * @param driverId unique driver identifier
     * @param location initial position
     */
    public void addDriver(String driverId, Location location) {
        drivers.put(driverId, new Driver(driverId, location));
    }

    /**
     * Updates a driver's current position (e.g. periodic GPS ping).
     *
     * @param driverId driver to update
     * @param location new position
     * @throws IllegalArgumentException if the driver does not exist
     */
    public void updateDriverLocation(String driverId, Location location) {
        requireDriver(driverId).location = location;
    }

    /**
     * Matches a rider with the nearest available driver and creates a trip.
     *
     * @param riderId unique rider identifier
     * @param pickup  rider's current location
     * @param drop    desired destination
     * @return the newly created {@link Trip} with status ASSIGNED
     * @throws IllegalStateException if no drivers are available
     */
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

    /**
     * Marks a trip as completed and moves the driver to the drop-off location.
     *
     * @param tripId the trip to complete
     * @throws IllegalArgumentException if the trip does not exist
     */
    public void completeTrip(String tripId) {
        Trip trip = requireTrip(tripId);
        trip.status = TripStatus.COMPLETED;
        Driver driver = requireDriver(trip.driverId);
        driver.location = trip.drop;
        driver.status = DriverStatus.AVAILABLE;
    }

    /**
     * Cancels a trip and makes the driver available again.
     *
     * @param tripId the trip to cancel
     * @throws IllegalArgumentException if the trip does not exist
     */
    public void cancelTrip(String tripId) {
        Trip trip = requireTrip(tripId);
        trip.status = TripStatus.CANCELLED;
        requireDriver(trip.driverId).status = DriverStatus.AVAILABLE;
    }

    /**
     * @param driverId the driver to retrieve
     * @return the {@link Driver} object for inspection
     * @throws IllegalArgumentException if the driver does not exist
     */
    public Driver getDriver(String driverId) {
        return requireDriver(driverId);
    }

    /** @throws IllegalArgumentException if the driver is not registered */
    private Driver requireDriver(String driverId) {
        Driver driver = drivers.get(driverId);
        if (driver == null) throw new IllegalArgumentException("unknown driver");
        return driver;
    }

    /** @throws IllegalArgumentException if the trip is not found */
    private Trip requireTrip(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) throw new IllegalArgumentException("unknown trip");
        return trip;
    }

    /** Returns the squared Euclidean distance between two locations (avoids a costly sqrt). */
    private double distanceSquared(Location a, Location b) {
        double dx = a.lat - b.lat;
        double dy = a.lon - b.lon;
        return dx * dx + dy * dy;
    }
}
