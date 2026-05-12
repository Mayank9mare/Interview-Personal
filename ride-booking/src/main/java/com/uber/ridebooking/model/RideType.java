package com.uber.ridebooking.model;

public enum RideType {
    SEDAN(50.0, 12.0),
    SUV(80.0, 18.0),
    AUTO(30.0, 8.0);

    private final double baseFare;
    private final double perKmRate;

    RideType(double baseFare, double perKmRate) {
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerKmRate() {
        return perKmRate;
    }
}
