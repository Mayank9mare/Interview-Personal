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
 * Responsibilities: rider/driver registration, ride request creation, trip
 * state transitions (accept → start → complete/cancel), fare calculation,
 * and proximity-based driver queries.
 *
 * Data structures:
 * <ul>
 *   <li>{@code ConcurrentHashMap} for all entity stores — O(1) average get/put.</li>
 *   <li>{@code requestToTrip} maps a request ID to the trip ID created on acceptance,
 *       enabling lookup in either direction.</li>
 * </ul>
 *
 * Thread-safety: {@code ConcurrentHashMap} provides safe concurrent reads and
 * single-key writes; compound state transitions (acceptRide, completeTrip, etc.)
 * use nested {@code synchronized} blocks on the affected entity objects to prevent
 * races, which is sufficient for an interview context.
 */
public class RideService {

    /** All registered riders keyed by rider ID. */
    private final Map<String, Rider> riders = new ConcurrentHashMap<>();
    /** All registered drivers keyed by driver ID. */
    private final Map<String, Driver> drivers = new ConcurrentHashMap<>();
    /** All outstanding and historical ride requests keyed by request ID. */
    private final Map<String, RideRequest> requests = new ConcurrentHashMap<>();
    /** All trips keyed by trip ID. */
    private final Map<String, Trip> trips = new ConcurrentHashMap<>();
    /** Maps request ID → trip ID; populated when a driver accepts a request. */
    private final Map<String, String> requestToTrip = new ConcurrentHashMap<>();

    /** Strategy used to compute the fare when a trip completes. */
    private final FareCalculator fareCalculator;

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    /** Creates a RideService using the standard {@link BaseFareCalculator}. */
    public RideService() {
        this(new BaseFareCalculator());
    }

    /**
     * Creates a RideService with a custom fare calculation strategy.
     *
     * @param fareCalculator the fare strategy to apply on trip completion
     */
    public RideService(FareCalculator fareCalculator) {
        this.fareCalculator = fareCalculator;
    }

    // ------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------

    /**
     * Registers a new rider and returns their generated ID.
     *
     * @param name display name
     * @return the newly assigned rider ID (UUID)
     */
    public String registerRider(String name) {
        String id = UUID.randomUUID().toString();
        riders.put(id, new Rider(id, name));
        return id;
    }

    /**
     * Registers a new driver at the given location and returns their generated ID.
     *
     * @param name display name
     * @param lat  initial latitude
     * @param lon  initial longitude
     * @return the newly assigned driver ID (UUID)
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
     * Creates a new ride request on behalf of a rider.
     *
     * @param riderId  the requesting rider's ID
     * @param pickup   pickup location
     * @param dropoff  dropoff location
     * @param type     vehicle category requested
     * @return the created {@link RideRequest} in {@code REQUESTED} status
     * @throws IllegalArgumentException if {@code riderId} is unknown
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
     * Creates a {@link Trip} in {@code ACCEPTED} status and marks the driver unavailable.
     *
     * @param driverId  the accepting driver's ID
     * @param requestId the ride request ID to accept
     * @return {@code true} on success; {@code false} if request not found, already accepted, or driver unavailable
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
     * Driver starts the trip, advancing status to {@code IN_PROGRESS}.
     *
     * @param driverId the driver's ID (must match the trip's assigned driver)
     * @param tripId   the trip to start
     * @return {@code true} on success; {@code false} if not found, wrong driver, or wrong status
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
     * Calculates fare via the configured {@link FareCalculator}, marks the driver
     * available again, and appends the trip to both the driver's and rider's histories.
     *
     * @param driverId the driver's ID (must match the trip's assigned driver)
     * @param tripId   the trip to complete
     * @return {@code true} on success; {@code false} if not found, wrong driver, or wrong status
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
     * Cancels a ride. Accepts either a trip ID or a request ID as {@code tripId}.
     * Works on a {@link RideRequest} in {@code REQUESTED} status or a {@link Trip}
     * in {@code ACCEPTED} or {@code IN_PROGRESS} status.
     *
     * @param actorId the rider's or assigned driver's ID
     * @param tripId  trip ID or request ID to cancel
     * @return {@code true} on success; {@code false} if not found, already terminal, or unauthorized actor
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

    /** Cancels an already-created Trip; authorises by rider or driver ID. */
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

    /** Cancels a REQUESTED ride that has no Trip yet; only the rider may cancel. */
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
     * Returns available drivers within {@code radiusKm} of {@code location},
     * sorted by ascending distance.
     *
     * @param location  the reference point (e.g. rider's pickup)
     * @param radiusKm  search radius in the same unit as {@link Location#distanceTo(Location)}
     * @return available drivers ordered nearest-first; empty list if none found
     */
    public List<Driver> getNearbyDrivers(Location location, double radiusKm) {
        return drivers.values().stream()
                .filter(Driver::isAvailable)
                .filter(d -> d.getCurrentLocation().distanceTo(location) <= radiusKm)
                .sorted(Comparator.comparingDouble(d -> d.getCurrentLocation().distanceTo(location)))
                .collect(Collectors.toList());
    }

    /**
     * Returns all trips recorded in a rider's history.
     *
     * @param riderId the rider's ID
     * @return a mutable snapshot of the rider's trip history
     * @throws IllegalArgumentException if {@code riderId} is unknown
     */
    public List<Trip> getRiderHistory(String riderId) {
        Rider rider = getOrThrow(riders, riderId, "Rider");
        return new ArrayList<>(rider.getTripHistory());
    }

    /**
     * Returns all trips recorded in a driver's history.
     *
     * @param driverId the driver's ID
     * @return a mutable snapshot of the driver's trip history
     * @throws IllegalArgumentException if {@code driverId} is unknown
     */
    public List<Trip> getDriverHistory(String driverId) {
        Driver driver = getOrThrow(drivers, driverId, "Driver");
        return new ArrayList<>(driver.getTripHistory());
    }

    // ------------------------------------------------------------------
    // Package-private helpers (used by tests)
    // ------------------------------------------------------------------

    /** @return the {@link Rider} for {@code riderId}, or {@code null} if not registered */
    Rider getRider(String riderId) {
        return riders.get(riderId);
    }

    /** @return the {@link Driver} for {@code driverId}, or {@code null} if not registered */
    Driver getDriver(String driverId) {
        return drivers.get(driverId);
    }

    /** @return the {@link RideRequest} for {@code requestId}, or {@code null} if not found */
    RideRequest getRequest(String requestId) {
        return requests.get(requestId);
    }

    /** @return the {@link Trip} for {@code tripId}, or {@code null} if not found */
    Trip getTrip(String tripId) {
        return trips.get(tripId);
    }

    /** @return the {@link Trip} created for {@code requestId}, or {@code null} if not yet accepted */
    Trip getTripByRequestId(String requestId) {
        String tripId = requestToTrip.get(requestId);
        return tripId != null ? trips.get(tripId) : null;
    }

    // ------------------------------------------------------------------
    // Internal utils
    // ------------------------------------------------------------------

    /** Looks up {@code id} in {@code map}; throws {@link IllegalArgumentException} if absent. */
    private <T> T getOrThrow(Map<String, T> map, String id, String label) {
        T value = map.get(id);
        if (value == null) {
            throw new IllegalArgumentException(label + " not found: " + id);
        }
        return value;
    }
}
