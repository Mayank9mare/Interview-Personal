package com.uber.parkinglot.model;

/**
 * Physical size category of a parking spot, determining which vehicle types may park in it.
 */
public enum SpotType {
    /** Narrow spot; accepts motorcycles only. */
    MOTORCYCLE,
    /** Medium spot; accepts motorcycles and cars. */
    COMPACT,
    /** Full-size spot; accepts all vehicle types including trucks. */
    LARGE
}
