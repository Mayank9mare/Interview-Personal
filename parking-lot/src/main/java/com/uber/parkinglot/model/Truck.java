package com.uber.parkinglot.model;

/**
 * A large commercial truck; always carries {@link VehicleType#TRUCK}.
 */
public class Truck extends Vehicle {

    /**
     * Constructs a truck with the given identity.
     *
     * @param id           unique identifier
     * @param licensePlate license plate string
     */
    public Truck(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.TRUCK);
    }
}
