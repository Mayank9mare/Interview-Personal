package com.uber.parkinglot.model;

public class MotorcycleSpot extends ParkingSpot {
    public MotorcycleSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.MOTORCYCLE);
    }

    @Override
    public boolean canFit(VehicleType vehicleType) {
        return vehicleType == VehicleType.MOTORCYCLE;
    }
}
