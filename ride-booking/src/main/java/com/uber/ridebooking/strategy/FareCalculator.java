package com.uber.ridebooking.strategy;

import com.uber.ridebooking.model.Trip;

/**
 * Strategy interface for fare calculation.
 * Implementations can vary the pricing algorithm (base, surge, promotional, etc.)
 */
public interface FareCalculator {
    /**
     * Calculate the fare for the given trip.
     *
     * @param trip the completed (or completing) trip
     * @return fare in rupees / currency units
     */
    double calculate(Trip trip);
}
