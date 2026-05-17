package com.uber.parkinglot.model;

/**
 * A narrow spot sized for motorcycles only; rejects all other vehicle types.
 */
public class MotorcycleSpot extends ParkingSpot {

    /**
     * Constructs a motorcycle spot with the given identity.
     *
     * @param spotId unique spot identifier
     * @param floor  floor number
     */
    public MotorcycleSpot(String spotId, int floor) {
        super(spotId, floor, SpotType.MOTORCYCLE);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} only for {@link VehicleType#MOTORCYCLE}
     */
    @Override
    public boolean canFit(VehicleType vehicleType) {
        return vehicleType == VehicleType.MOTORCYCLE;
    }
}
