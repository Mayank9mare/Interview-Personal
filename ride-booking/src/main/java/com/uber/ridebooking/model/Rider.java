package com.uber.ridebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a platform user who requests rides.
 *
 * Maintains an append-only trip history; the list is exposed read-only to
 * prevent external mutation. Not thread-safe on its own — callers (e.g.
 * {@code RideService}) must synchronize externally when mutating state.
 */
public class Rider {
    /** Unique rider identifier (UUID). */
    private final String id;
    /** Display name. */
    private final String name;
    /** Ordered history of all trips this rider has taken. */
    private final List<Trip> tripHistory;

    /**
     * @param id   unique rider identifier
     * @param name display name
     */
    public Rider(String id, String name) {
        this.id = id;
        this.name = name;
        this.tripHistory = new ArrayList<>();
    }

    /** @return unique rider identifier */
    public String getId() {
        return id;
    }

    /** @return display name */
    public String getName() {
        return name;
    }

    /**
     * @return unmodifiable view of the rider's trip history in insertion order
     */
    public List<Trip> getTripHistory() {
        return Collections.unmodifiableList(tripHistory);
    }

    /**
     * Appends a completed trip to history.
     *
     * @param trip the trip to record
     */
    public void addTrip(Trip trip) {
        tripHistory.add(trip);
    }

    @Override
    public String toString() {
        return String.format("Rider{id='%s', name='%s'}", id, name);
    }
}
