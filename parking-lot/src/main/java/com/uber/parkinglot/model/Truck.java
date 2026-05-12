package com.uber.parkinglot.model;

public class Truck extends Vehicle {
    public Truck(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.TRUCK);
    }
}
