package com.uber.ridebooking.model;

/**
 * Lifecycle states of a ride request or trip.
 *
 * Valid transitions:
 * <pre>
 *   REQUESTED → ACCEPTED → IN_PROGRESS → COMPLETED
 *   REQUESTED → CANCELLED
 *   ACCEPTED  → CANCELLED
 *   IN_PROGRESS → CANCELLED
 * </pre>
 */
public enum TripStatus {
    /** Rider has submitted a request; no driver assigned yet. */
    REQUESTED,
    /** Driver has accepted the request; trip object created. */
    ACCEPTED,
    /** Driver has started driving; trip clock is running. */
    IN_PROGRESS,
    /** Trip finished successfully; fare has been calculated. */
    COMPLETED,
    /** Request or trip was cancelled by rider or driver. */
    CANCELLED
}
