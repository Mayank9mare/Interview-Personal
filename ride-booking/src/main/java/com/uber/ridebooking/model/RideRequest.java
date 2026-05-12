package com.uber.ridebooking.model;

public class RideRequest {
    private final String id;
    private final Rider rider;
    private final Location pickup;
    private final Location dropoff;
    private final RideType type;
    private TripStatus status;

    public RideRequest(String id, Rider rider, Location pickup, Location dropoff, RideType type) {
        this.id = id;
        this.rider = rider;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.type = type;
        this.status = TripStatus.REQUESTED;
    }

    public String getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Location getPickup() {
        return pickup;
    }

    public Location getDropoff() {
        return dropoff;
    }

    public RideType getType() {
        return type;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("RideRequest{id='%s', rider=%s, pickup=%s, dropoff=%s, type=%s, status=%s}",
                id, rider.getName(), pickup, dropoff, type, status);
    }
}
