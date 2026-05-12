package com.uber.parkinglot.strategy;

import com.uber.parkinglot.model.Ticket;
import com.uber.parkinglot.model.VehicleType;

import java.util.Map;

public class HourlyPricingStrategy implements PricingStrategy {
    private final Map<VehicleType, Double> hourlyRates;

    public HourlyPricingStrategy(Map<VehicleType, Double> hourlyRates) {
        this.hourlyRates = hourlyRates;
    }

    @Override
    public double calculate(Ticket ticket) {
        VehicleType type = ticket.getVehicle().getType();
        double hourlyRate = hourlyRates.getOrDefault(type, 0.0);
        long durationMs = ticket.getExitTime() - ticket.getEntryTime();
        // Minimum 1 hour; ceil to next hour
        double hours = Math.max(1.0, Math.ceil(durationMs / 3_600_000.0));
        return hours * hourlyRate;
    }
}
