package com.uber.parkinglot.model;

public class LargeSpot extends ParkingSpot {
    public LargeSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.LARGE);
    }

    @Override
    public boolean canFit(VehicleType vehicleType) {
        // Large spots fit all vehicle types
        return true;
    }
}
