package com.uber.ridebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a driver on the platform.
 *
 * Mutable state ({@code currentLocation}, {@code available}) is updated by
 * {@code RideService} during the trip lifecycle. Callers must synchronize on
 * the Driver instance when reading and writing these fields concurrently.
 */
public class Driver {
    /** Unique driver identifier (UUID). */
    private final String id;
    /** Display name. */
    private final String name;
    /** Last known GPS position; updated when a trip completes or the driver moves. */
    private Location currentLocation;
    /** True when the driver has no active trip and can accept new requests. */
    private boolean available;
    /** Ordered history of all trips this driver has completed or cancelled. */
    private final List<Trip> tripHistory;

    /**
     * @param id              unique driver identifier
     * @param name            display name
     * @param currentLocation initial GPS position
     */
    public Driver(String id, String name, Location currentLocation) {
        this.id = id;
        this.name = name;
        this.currentLocation = currentLocation;
        this.available = true;
        this.tripHistory = new ArrayList<>();
    }

    /** @return unique driver identifier */
    public String getId() {
        return id;
    }

    /** @return display name */
    public String getName() {
        return name;
    }

    /** @return last known GPS position */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Updates the driver's known position.
     *
     * @param currentLocation new GPS position
     */
    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    /** @return {@code true} if the driver can accept a new ride request */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Marks the driver as available or unavailable.
     *
     * @param available {@code false} when a trip is in progress; {@code true} after completion or cancellation
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * @return unmodifiable view of the driver's trip history in insertion order
     */
    public List<Trip> getTripHistory() {
        return Collections.unmodifiableList(tripHistory);
    }

    /**
     * Appends a completed or cancelled trip to history.
     *
     * @param trip the trip to record
     */
    public void addTrip(Trip trip) {
        tripHistory.add(trip);
    }

    @Override
    public String toString() {
        return String.format("Driver{id='%s', name='%s', location=%s, available=%b}",
                id, name, currentLocation, available);
    }
}
