package com.uber.parkinglot.model;

public class Car extends Vehicle {
    public Car(String id, String licensePlate) {
        super(id, licensePlate, VehicleType.CAR);
    }
}
