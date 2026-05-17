package com.uber.parkinglot.model;

/**
 * Base class for all vehicle types in the parking lot system.
 *
 * <p>Each vehicle carries an immutable identity ({@code id}, {@code licensePlate}) and a
 * {@link VehicleType} that drives spot-assignment rules. Subclasses fix the type at construction
 * time, so the type field is effectively a constant after the object is created.
 *
 * <p>Not thread-safe; instances are not expected to be shared across threads.
 */
public abstract class Vehicle {
    /** Unique application-level identifier for this vehicle. */
    private final String id;
    /** License plate string, used for display and lookup. */
    private final String licensePlate;
    /** Size category that determines which spot types this vehicle may use. */
    private final VehicleType type;

    /**
     * Constructs a vehicle with the given identity and type.
     *
     * @param id           unique identifier for this vehicle
     * @param licensePlate license plate string
     * @param type         size category of the vehicle
     */
    protected Vehicle(String id, String licensePlate, VehicleType type) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.type = type;
    }

    /**
     * Returns the unique application-level identifier.
     *
     * @return vehicle id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the license plate string.
     *
     * @return license plate
     */
    public String getLicensePlate() {
        return licensePlate;
    }

    /**
     * Returns the size category of this vehicle.
     *
     * @return vehicle type
     */
    public VehicleType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', licensePlate='" + licensePlate + "'}";
    }
}
