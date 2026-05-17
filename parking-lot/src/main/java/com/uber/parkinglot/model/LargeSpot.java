package com.uber.parkinglot.model;

/**
 * A full-size spot that accommodates all vehicle types, including trucks.
 */
public class LargeSpot extends ParkingSpot {

    /**
     * Constructs a large spot with the given identity.
     *
     * @param spotId unique spot identifier
     * @param floor  floor number
     */
    public LargeSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.LARGE);
    }

    /**
     * {@inheritDoc}
     *
     * @return always {@code true}; large spots accept every vehicle type
     */
    @Override
    public boolean canFit(VehicleType vehicleType) {
        return true;
    }
}
