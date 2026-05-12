package com.uber.parkinglot.model;

public class CompactSpot extends ParkingSpot {
    public CompactSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.COMPACT);
    }

    @Override
    public boolean canFit(VehicleType vehicleType) {
        return vehicleType == VehicleType.MOTORCYCLE || vehicleType == VehicleType.CAR;
    }
}
