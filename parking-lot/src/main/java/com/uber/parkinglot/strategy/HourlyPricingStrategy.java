package com.uber.parkinglot.strategy;

import com.uber.parkinglot.model.Ticket;
import com.uber.parkinglot.model.VehicleType;

import java.util.Map;

/**
 * A {@link PricingStrategy} that charges a per-vehicle-type hourly rate.
 *
 * <p>Duration is derived from {@code ticket.exitTime - ticket.entryTime}, converted to hours and
 * ceiled to the next whole hour. A minimum of one billable hour is always applied regardless of
 * actual duration. Vehicle types absent from the rate map are charged {@code 0.0}.
 *
 * <p>Not thread-safe; the rate map is held by reference and must not be mutated after construction.
 */
public class HourlyPricingStrategy implements PricingStrategy {
    /** Per-vehicle-type hourly rate in currency units. */
    private final Map<VehicleType, Double> hourlyRates;

    /**
     * Constructs a strategy with the given per-type hourly rates.
     *
     * @param hourlyRates map from vehicle type to hourly rate; types not present default to 0.0
     */
    public HourlyPricingStrategy(Map<VehicleType, Double> hourlyRates) {
        this.hourlyRates = hourlyRates;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Fee = {@code ceil(max(1, durationHours)) * hourlyRate} for the ticket's vehicle type.
     *
     * @param ticket completed ticket with {@code exitTime} set
     * @return computed fee; {@code 0.0} if the vehicle type has no configured rate
     */
    @Override
    public double calculate(Ticket ticket) {
        VehicleType type = ticket.getVehicle().getType();
        double hourlyRate = hourlyRates.getOrDefault(type, 0.0);
        long durationMs = ticket.getExitTime() - ticket.getEntryTime();
        double hours = Math.max(1.0, Math.ceil(durationMs / 3_600_000.0));
        return hours * hourlyRate;
    }
}
