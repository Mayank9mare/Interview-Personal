package com.uber.parkinglot.model;

/**
 * A medium-sized spot that accommodates motorcycles and cars, but not trucks.
 */
public class CompactSpot extends ParkingSpot {

    /**
     * Constructs a compact spot with the given identity.
     *
     * @param spotId unique spot identifier
     * @param floor  floor number
     */
    public CompactSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.COMPACT);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} for {@link VehicleType#MOTORCYCLE} and {@link VehicleType#CAR}
     */
    @Override
    public boolean canFit(VehicleType vehicleType) {
        return vehicleType == VehicleType.MOTORCYCLE || vehicleType == VehicleType.CAR;
    }
}
