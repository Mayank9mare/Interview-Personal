package com.uber.parkinglot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single floor in the parking lot, owning a fixed set of {@link ParkingSpot}s.
 *
 * <p>The spot list is defensively copied at construction time so external mutations do not affect
 * the floor's state. Queries iterate the list linearly; spot counts are expected to be small enough
 * that this is acceptable.
 *
 * <p>Not thread-safe.
 */
public class Floor {
    /** Identifies this floor (1-based by convention). */
    private final int floorNumber;
    /** All spots on this floor, in the order they were provided at construction. */
    private final List<ParkingSpot> spots;

    /**
     * Constructs a floor with the given number and set of spots.
     *
     * @param floorNumber floor identifier (e.g. 1 for ground floor)
     * @param spots       spots located on this floor; defensive copy is made
     */
    public Floor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>(spots);
    }

    /**
     * Returns the floor number.
     *
     * @return floor number
     */
    public int getFloorNumber() {
        return floorNumber;
    }

    /**
     * Returns all spots on this floor (mutable list backed by this floor's state).
     *
     * @return list of all parking spots
     */
    public List<ParkingSpot> getSpots() {
        return spots;
    }

    /**
     * Returns the subset of spots that are currently unoccupied and can fit the given vehicle type.
     *
     * @param type vehicle type to match against
     * @return list of available, compatible spots; empty if none exist
     */
    public List<ParkingSpot> getAvailableSpots(VehicleType type) {
        return spots.stream()
                .filter(spot -> spot.isAvailable() && spot.canFit(type))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Floor{floorNumber=" + floorNumber + ", totalSpots=" + spots.size() + "}";
    }
}
