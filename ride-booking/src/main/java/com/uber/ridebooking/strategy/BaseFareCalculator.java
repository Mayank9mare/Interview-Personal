package com.uber.ridebooking.strategy;

import com.uber.ridebooking.model.RideType;
import com.uber.ridebooking.model.Trip;

/**
 * Standard (non-surge) fare calculator.
 *
 * Formula: {@code baseFare + perKmRate * distanceKm}, rounded to 2 decimal places.
 * Pricing constants are sourced from the trip's {@link com.uber.ridebooking.model.RideType}.
 * Thread-safe: stateless.
 */
public class BaseFareCalculator implements FareCalculator {

    /**
     * @param trip the trip to price
     * @return fare in currency units rounded to 2 decimal places
     */
    @Override
    public double calculate(Trip trip) {
        RideType type = trip.getRequest().getType();
        double distance = trip.getDistanceKm();
        double fare = type.getBaseFare() + (type.getPerKmRate() * distance);
        // Round to 2 decimal places
        return Math.round(fare * 100.0) / 100.0;
    }
}
