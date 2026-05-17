package com.uber.ridebooking.model;

/**
 * Vehicle category offered on the platform, each carrying its own pricing constants.
 *
 * Fare formula: {@code baseFare + perKmRate * distanceKm}.
 */
public enum RideType {
    /** Standard 4-door sedan. */
    SEDAN(50.0, 12.0),
    /** Larger SUV / 6-seater. */
    SUV(80.0, 18.0),
    /** Three-wheeler auto-rickshaw. */
    AUTO(30.0, 8.0);

    /** Fixed boarding charge in currency units. */
    private final double baseFare;
    /** Per-kilometre rate in currency units. */
    private final double perKmRate;

    RideType(double baseFare, double perKmRate) {
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    /** @return fixed boarding charge in currency units */
    public double getBaseFare() {
        return baseFare;
    }

    /** @return per-kilometre rate in currency units */
    public double getPerKmRate() {
        return perKmRate;
    }
}
