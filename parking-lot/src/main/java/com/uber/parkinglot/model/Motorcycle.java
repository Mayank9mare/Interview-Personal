package com.uber.parkinglot.model;

public class Motorcycle extends Vehicle {
    public Motorcycle(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.MOTORCYCLE);
    }
}
