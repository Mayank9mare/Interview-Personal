package com.uber.parkinglot.strategy;

import com.uber.parkinglot.model.Ticket;

/**
 * Strategy interface for computing a parking fee from a completed {@link Ticket}.
 *
 * <p>Implementations may vary the fee by vehicle type, duration, time-of-day, or any other
 * factor derivable from the ticket. The caller is responsible for setting {@code exitTime} on
 * the ticket before invoking {@link #calculate}.
 */
public interface PricingStrategy {

    /**
     * Computes the parking fee for the given ticket.
     *
     * @param ticket a completed ticket with {@code exitTime} already set
     * @return fee in currency units; must be &ge; 0
     */
    double calculate(Ticket ticket);
}
