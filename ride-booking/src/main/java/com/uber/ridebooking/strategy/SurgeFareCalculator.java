package com.uber.ridebooking.strategy;

import com.uber.ridebooking.model.Trip;

/**
 * Surge pricing: applies a multiplier on top of the base fare.
 * Decorator pattern — wraps a base FareCalculator.
 */
public class SurgeFareCalculator implements FareCalculator {

    private final FareCalculator base;
    private final double surgeMultiplier;

    public SurgeFareCalculator(FareCalculator base, double surgeMultiplier) {
        if (surgeMultiplier < 1.0) {
            throw new IllegalArgumentException("Surge multiplier must be >= 1.0, got: " + surgeMultiplier);
        }
        this.base = base;
        this.surgeMultiplier = surgeMultiplier;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    @Override
    public double calculate(Trip trip) {
        double baseFare = base.calculate(trip);
        double surgedFare = baseFare * surgeMultiplier;
        return Math.round(surgedFare * 100.0) / 100.0;
    }
}
