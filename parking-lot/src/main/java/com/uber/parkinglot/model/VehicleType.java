package com.uber.parkinglot.model;

/**
 * Classifies a vehicle by its physical size, used to determine which spot types it may occupy.
 */
public enum VehicleType {
    /** Two-wheeled vehicle; fits only motorcycle spots (and larger by policy). */
    MOTORCYCLE,
    /** Standard passenger car; fits compact and large spots. */
    CAR,
    /** Large commercial vehicle; fits only large spots. */
    TRUCK
}
