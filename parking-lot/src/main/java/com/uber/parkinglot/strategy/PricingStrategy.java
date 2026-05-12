package com.uber.parkinglot.strategy;

import com.uber.parkinglot.model.Ticket;

public interface PricingStrategy {
    double calculate(Ticket ticket);
}
