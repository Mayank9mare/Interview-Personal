package com.uber.parkinglot.model;

/**
 * A two-wheeled motorcycle; always carries {@link VehicleType#MOTORCYCLE}.
 */
public class Motorcycle extends Vehicle {

    /**
     * Constructs a motorcycle with the given identity.
     *
     * @param id           unique identifier
     * @param licensePlate license plate string
     */
    public Motorcycle(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.MOTORCYCLE);
    }
}
