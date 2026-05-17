package com.uber.parkinglot.model;

/**
 * Base class for a physical parking spot in the lot.
 *
 * <p>A spot has a fixed identity ({@code spotId}, {@code floor}, {@code spotType}) and mutable
 * occupancy state ({@code parkedVehicle}). The invariant is: {@code parkedVehicle == null} iff the
 * spot is available. Subclasses implement {@link #canFit} to enforce size compatibility rules.
 *
 * <p>Not thread-safe; external synchronisation is required for concurrent access.
 */
public abstract class ParkingSpot {
    /** Unique identifier for this spot within the lot. */
    private final String spotId;
    /** Floor number on which this spot resides. */
    private final int floor;
    /** Size category of this spot. */
    private final SpotType spotType;
    /** Currently parked vehicle, or {@code null} when the spot is free. */
    private Vehicle parkedVehicle;

    /**
     * Constructs a parking spot with the given identity; initially unoccupied.
     *
     * @param spotId   unique spot identifier
     * @param floor    floor number
     * @param spotType size category of this spot
     */
    protected ParkingSpot(String spotId, int floor, SpotType spotType) {
        this.spotId = spotId;
        this.floor = floor;
        this.spotType = spotType;
        this.parkedVehicle = null;
    }

    /**
     * Returns the unique identifier of this spot.
     *
     * @return spot id
     */
    public String getSpotId() {
        return spotId;
    }

    /**
     * Returns the floor number on which this spot resides.
     *
     * @return floor number
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Returns the size category of this spot.
     *
     * @return spot type
     */
    public SpotType getSpotType() {
        return spotType;
    }

    /**
     * Returns the vehicle currently parked in this spot, or {@code null} if unoccupied.
     *
     * @return parked vehicle, or {@code null}
     */
    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    /**
     * Returns {@code true} if no vehicle is currently parked here.
     *
     * @return {@code true} if available
     */
    public boolean isAvailable() {
        return parkedVehicle == null;
    }

    /**
     * Returns whether a vehicle of the given type is physically compatible with this spot.
     *
     * @param vehicleType the vehicle type to check
     * @return {@code true} if the vehicle can fit
     */
    public abstract boolean canFit(VehicleType vehicleType);

    /**
     * Parks the given vehicle in this spot.
     *
     * @param vehicle the vehicle to park
     * @throws IllegalStateException     if the spot is already occupied
     * @throws IllegalArgumentException  if the vehicle type is incompatible with this spot
     */
    public void park(Vehicle vehicle) {
        if (!isAvailable()) {
            throw new IllegalStateException("Spot " + spotId + " is already occupied.");
        }
        if (!canFit(vehicle.getType())) {
            throw new IllegalArgumentException(
                "Vehicle type " + vehicle.getType() + " cannot fit in spot type " + spotType);
        }
        this.parkedVehicle = vehicle;
    }

    /**
     * Removes the currently parked vehicle, freeing the spot.
     *
     * @throws IllegalStateException if the spot is already empty
     */
    public void unpark() {
        if (isAvailable()) {
            throw new IllegalStateException("Spot " + spotId + " is already empty.");
        }
        this.parkedVehicle = null;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{spotId='" + spotId + "', floor=" + floor +
               ", available=" + isAvailable() + "}";
    }
}
