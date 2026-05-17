package com.uber.ridebooking.model;

/**
 * An active or historical trip pairing a {@link RideRequest} with a {@link Driver}.
 *
 * Created in {@link TripStatus#ACCEPTED} state; progresses through
 * {@code IN_PROGRESS} to {@code COMPLETED} (or {@code CANCELLED}).
 * {@code fare} is populated only on completion.  {@code startTime} and
 * {@code endTime} are epoch-milliseconds; both are 0 until the respective
 * transition occurs.
 *
 * Not thread-safe; callers must synchronize on this instance when mutating state.
 */
public class Trip {
    /** Unique trip identifier (UUID). */
    private final String id;
    /** The originating ride request (pickup, dropoff, type, rider). */
    private final RideRequest request;
    /** The driver assigned to this trip. */
    private final Driver driver;
    /** Calculated fare in currency units; 0.0 until the trip is completed. */
    private double fare;
    /** Epoch-millisecond timestamp when the driver started the trip; 0 until then. */
    private long startTime;
    /** Epoch-millisecond timestamp when the trip was completed; 0 until then. */
    private long endTime;
    /** Current lifecycle status. */
    private TripStatus status;

    /**
     * @param id      unique trip identifier
     * @param request the originating ride request
     * @param driver  the assigned driver
     */
    public Trip(String id, RideRequest request, Driver driver) {
        this.id = id;
        this.request = request;
        this.driver = driver;
        this.fare = 0.0;
        this.startTime = 0L;
        this.endTime = 0L;
        this.status = TripStatus.ACCEPTED;
    }

    /** @return unique trip identifier */
    public String getId() {
        return id;
    }

    /** @return the originating ride request */
    public RideRequest getRequest() {
        return request;
    }

    /** @return the assigned driver */
    public Driver getDriver() {
        return driver;
    }

    /** @return calculated fare; 0.0 if the trip has not yet completed */
    public double getFare() {
        return fare;
    }

    /**
     * Sets the fare after calculation on trip completion.
     *
     * @param fare fare in currency units
     */
    public void setFare(double fare) {
        this.fare = fare;
    }

    /** @return epoch-millisecond start time; 0 if the trip has not started */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Records the epoch-millisecond time the driver started driving.
     *
     * @param startTime epoch milliseconds
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /** @return epoch-millisecond end time; 0 if the trip has not completed */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Records the epoch-millisecond time the trip was completed.
     *
     * @param endTime epoch milliseconds
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    /** @return current lifecycle status */
    public TripStatus getStatus() {
        return status;
    }

    /**
     * Updates the lifecycle status.
     *
     * @param status new status
     */
    public void setStatus(TripStatus status) {
        this.status = status;
    }

    /**
     * Convenience: Euclidean distance between pickup and dropoff.
     *
     * @return distance in the same units as {@link Location#distanceTo(Location)}
     */
    public double getDistanceKm() {
        return request.getPickup().distanceTo(request.getDropoff());
    }

    @Override
    public String toString() {
        return String.format(
                "Trip{id='%s', rider='%s', driver='%s', type=%s, fare=%.2f, status=%s}",
                id, request.getRider().getName(), driver.getName(),
                request.getType(), fare, status);
    }
}
