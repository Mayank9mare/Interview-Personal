package com.uber.ridebooking;

import com.uber.ridebooking.model.*;
import com.uber.ridebooking.strategy.BaseFareCalculator;
import com.uber.ridebooking.strategy.FareCalculator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core service orchestrating the ride booking lifecycle.
 *
 * Thread-safety note: uses ConcurrentHashMap for storage; compound state
 * transitions (acceptRide, completeTrip, etc.) are synchronized on the
 * affected objects, which is sufficient for an interview context.
 */
public class RideService {

    // -- Storage --
    private final Map<String, Rider> riders = new ConcurrentHashMap<>();
    private final Map<String, Driver> drivers = new ConcurrentHashMap<>();
    private final Map<String, RideRequest> requests = new ConcurrentHashMap<>();
    private final Map<String, Trip> trips = new ConcurrentHashMap<>();

    // requestId -> tripId (set when a driver accepts a request)
    private final Map<String, String> requestToTrip = new ConcurrentHashMap<>();

    private final FareCalculator fareCalculator;

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    public RideService() {
        this(new BaseFareCalculator());
    }

    public RideService(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    /**
     * Register a new rider and return their generated ID.
     */
    public String registerRider(String name) {
        String id = UUID.randomUUID().toString();
        riders.put(id, new Rider(id, name));
        return id;
    }

    /**
     * Register a new driver at the given location and return their generated ID.
     */
    public String registerDriver(String name, double lat, double lon) {
        String id = UUID.randomUUID().toString();
        drivers.put(id, new Driver(id, name, new Location(lat, lon)));
        return id;
    }

    // ------------------------------------------------------------------
    // Ride lifecycle
    // ------------------------------------------------------------------

    /**
     * Rider requests a ride.
     *
     * @throws IllegalArgumentException if riderId is unknown
     */
    public RideRequest requestRide(String riderId, Location pickup, Location dropoff, RideType type) {
        Rider rider = getOrThrow(riders, riderId, "Rider");
        String id = UUID.randomUUID().toString();
        RideRequest request = new RideRequest(id, rider, pickup, dropoff, type);
        requests.put(id, request);
        return request;
    }

    /**
     * Driver accepts a pending ride request.
     * Creates a Trip in ACCEPTED status and marks the driver unavailable.
     *
     * @return true on success; false if request not found, already accepted, or driver unavailable
     */
    public boolean acceptRide(String driverId, String requestId) {
        Driver driver = drivers.get(driverId);
        RideRequest request = requests.get(requestId);

        if (driver == null || request == null) return false;

        synchronized (request) {
            if (request.getStatus() != TripStatus.REQUESTED) return false;

            synchronized (driver) {
                if (!driver.isAvailable()) return false;

                // Atomically accept
                request.setStatus(TripStatus.ACCEPTED);
                driver.setAvailable(false);

                String tripId = UUID.randomUUID().toString();
                Trip trip = new Trip(tripId, request, driver);
                trips.put(tripId, trip);
                requestToTrip.put(requestId, tripId);
                return true;
            }
        }
    }

    /**
     * Driver starts the trip (moves it to IN_PROGRESS).
     *
     * @return true on success
     */
    public boolean startTrip(String driverId, String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) return false;

        synchronized (trip) {
            if (!trip.getDriver().getId().equals(driverId)) return false;
            if (trip.getStatus() != TripStatus.ACCEPTED) return false;

            trip.setStatus(TripStatus.IN_PROGRESS);
            trip.getRequest().setStatus(TripStatus.IN_PROGRESS);
            trip.setStartTime(System.currentTimeMillis());
            return true;
        }
    }

    /**
     * Driver completes the trip.
     * Calculates fare, marks driver available, and adds trip to both histories.
     *
     * @return true on success
     */
    public boolean completeTrip(String driverId, String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) return false;

        synchronized (trip) {
            if (!trip.getDriver().getId().equals(driverId)) return false;
            if (trip.getStatus() != TripStatus.IN_PROGRESS) return false;

            trip.setEndTime(System.currentTimeMillis());
            double fare = fareCalculator.calculate(trip);
            trip.setFare(fare);
            trip.setStatus(TripStatus.COMPLETED);
            trip.getRequest().setStatus(TripStatus.COMPLETED);

            Driver driver = trip.getDriver();
            synchronized (driver) {
                driver.setAvailable(true);
                driver.addTrip(trip);
            }
            trip.getRequest().getRider().addTrip(trip);
            return true;
        }
    }

    /**
     * Cancel a ride. Works on either a RideRequest (REQUESTED status) or a Trip
     * (ACCEPTED / IN_PROGRESS). The actorId can be either the rider's ID or the
     * assigned driver's ID.
     *
     * @return true on success
     */
    public boolean cancelRide(String actorId, String tripId) {
        // First try to cancel by tripId (could be a Trip)
        Trip trip = trips.get(tripId);
        if (trip != null) {
            return cancelTrip(actorId, trip);
        }

        // Fall back: treat tripId as a requestId
        RideRequest request = requests.get(tripId);
        if (request != null) {
            return cancelRequest(actorId, request);
        }

        return false;
    }

    private boolean cancelTrip(String actorId, Trip trip) {
        synchronized (trip) {
            TripStatus status = trip.getStatus();
            if (status == TripStatus.COMPLETED || status == TripStatus.CANCELLED) return false;

            // Only rider or assigned driver may cancel
            String riderId = trip.getRequest().getRider().getId();
            String driverId = trip.getDriver().getId();
            if (!actorId.equals(riderId) && !actorId.equals(driverId)) return false;

            trip.setStatus(TripStatus.CANCELLED);
            trip.getRequest().setStatus(TripStatus.CANCELLED);

            Driver driver = trip.getDriver();
            synchronized (driver) {
                driver.setAvailable(true);
            }
            return true;
        }
    }

    private boolean cancelRequest(String actorId, RideRequest request) {
        synchronized (request) {
            if (request.getStatus() != TripStatus.REQUESTED) return false;

            String riderId = request.getRider().getId();
            if (!actorId.equals(riderId)) return false;

            request.setStatus(TripStatus.CANCELLED);
            return true;
        }
    }

    // ------------------------------------------------------------------
    // Queries
    // ------------------------------------------------------------------

    /**
     * Returns available drivers within radiusKm of location, sorted by distance ascending.
     */
    public List<Driver> getNearbyDrivers(Location location, double radiusKm) {
        return drivers.values().stream()
                .filter(Driver::isAvailable)
                .filter(d -> d.getCurrentLocation().distanceTo(location) <= radiusKm)
                .sorted(Comparator.comparingDouble(d -> d.getCurrentLocation().distanceTo(location)))
                .collect(Collectors.toList());
    }

    /**
     * Returns all trips (completed or otherwise) in a rider's history.
     */
    public List<Trip> getRiderHistory(String riderId) {
        Rider rider = getOrThrow(riders, riderId, "Rider");
        return new ArrayList<>(rider.getTripHistory());
    }

    /**
     * Returns all trips in a driver's history.
     */
    public List<Trip> getDriverHistory(String driverId) {
        Driver driver = getOrThrow(drivers, driverId, "Driver");
        return new ArrayList<>(driver.getTripHistory());
    }

    // ------------------------------------------------------------------
    // Package-private helpers (used by tests)
    // ------------------------------------------------------------------

    Rider getRider(String riderId) {
        return riders.get(riderId);
    }

    Driver getDriver(String driverId) {
        return drivers.get(driverId);
    }

    RideRequest getRequest(String requestId) {
        return requests.get(requestId);
    }

    Trip getTrip(String tripId) {
        return trips.get(tripId);
    }

    Trip getTripByRequestId(String requestId) {
        String tripId = requestToTrip.get(requestId);
        return tripId != null ? trips.get(tripId) : null;
    }

    // ------------------------------------------------------------------
    // Internal utils
    // ------------------------------------------------------------------

    private <T> T getOrThrow(Map<String, T> map, String id, String label) {
        T value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException(label + " not found: " + id);
        }
        return value;
    }
}
