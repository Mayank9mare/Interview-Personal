package com.uber.parkinglot.model;

public abstract class Vehicle {
    private final String id;
    private final String licensePlate;
    private final VehicleType type;

    protected Vehicle(String id, String licensePlate, VehicleType type) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', licensePlate='" + licensePlate + "'}";
    }
}
