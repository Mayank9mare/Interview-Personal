package com.uber.ridebooking.model;

public class Trip {
    private final String id;
    private final RideRequest request;
    private final Driver driver;
    private double fare;
    private long startTime;
    private long endTime;
    private TripStatus status;

    public Trip(String id, RideRequest request, Driver driver) {
        this.id = id;
        this.request = request;
        this.driver = driver;
        this.fare = 0.0;
        this.startTime = 0L;
        this.endTime = 0L;
        this.status = TripStatus.ACCEPTED;
    }

    public String getId() {
        return id;
    }

    public RideRequest getRequest() {
        return request;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    /**
     * Convenience: distance between pickup and dropoff in the ride request.
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
