package com.uber.ridebooking.strategy;

import com.uber.ridebooking.model.RideType;
import com.uber.ridebooking.model.Trip;

/**
 * Standard fare: baseFare + (perKmRate * distance).
 */
public class BaseFareCalculator implements FareCalculator {

    @Override
    public double calculate(Trip trip) {
        RideType type = trip.getRequest().getType();
        double distance = trip.getDistanceKm();
        double fare = type.getBaseFare() + (type.getPerKmRate() * distance);
        // Round to 2 decimal places
        return Math.round(fare * 100.0) / 100.0;
    }
}
