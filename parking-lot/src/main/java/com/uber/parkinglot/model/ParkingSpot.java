package com.uber.parkinglot.model;

public abstract class ParkingSpot {
    private final String spotId;
    private final int floor;
    private final SpotType spotType;
    private Vehicle parkedVehicle;

    protected ParkingSpot(String spotId, int floor, SpotType spotType) {
        this.spotId = spotId;
        this.floor = floor;
        this.spotType = spotType;
        this.parkedVehicle = null;
    }

    public String getSpotId() {
        return spotId;
    }

    public int getFloor() {
        return floor;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    public boolean isAvailable() {
        return parkedVehicle == null;
    }

    public abstract boolean canFit(VehicleType vehicleType);

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
