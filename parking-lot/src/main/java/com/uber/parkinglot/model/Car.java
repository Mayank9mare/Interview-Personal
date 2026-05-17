package com.uber.parkinglot.model;

/**
 * A standard passenger car; always carries {@link VehicleType#CAR}.
 */
public class Car extends Vehicle {

    /**
     * Constructs a car with the given identity.
     *
     * @param id           unique identifier
     * @param licensePlate license plate string
     */
    public Car(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.CAR);
    }
}
