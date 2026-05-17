package com.uber.ridebooking.strategy;

import com.uber.ridebooking.model.Trip;

/**
 * Surge pricing decorator — multiplies the fare produced by a wrapped
 * {@link FareCalculator} by a surge multiplier.
 *
 * Follows the Decorator pattern: any {@code FareCalculator} (including another
 * {@code SurgeFareCalculator}) can be wrapped, enabling composable pricing layers.
 * The multiplier must be &ge; 1.0; values below 1.0 represent a discount and are
 * rejected to keep the semantics unambiguous.
 * Thread-safe: all fields are final after construction.
 */
public class SurgeFareCalculator implements FareCalculator {

    /** The underlying fare calculator whose result is multiplied. */
    private final FareCalculator base;
    /** Surge multiplier applied to the base fare (e.g. 1.5 = 50 % surge). */
    private final double surgeMultiplier;

    /**
     * @param base            the fare calculator to wrap
     * @param surgeMultiplier factor applied to the wrapped fare; must be &ge; 1.0
     * @throws IllegalArgumentException if {@code surgeMultiplier} is less than 1.0
     */
    public SurgeFareCalculator(FareCalculator base, double surgeMultiplier) {
        if (surgeMultiplier < 1.0) {
            throw new IllegalArgumentException("Surge multiplier must be >= 1.0, got: " + surgeMultiplier);
        }
        this.base = base;
        this.surgeMultiplier = surgeMultiplier;
    }

    /** @return the surge multiplier applied on top of the base fare */
    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    /**
     * @param trip the trip to price
     * @return base fare multiplied by the surge factor, rounded to 2 decimal places
     */
    @Override
    public double calculate(Trip trip) {
        double baseFare = base.calculate(trip);
        double surgedFare = baseFare * surgeMultiplier;
        return Math.round(surgedFare * 100.0) / 100.0;
    }
}
