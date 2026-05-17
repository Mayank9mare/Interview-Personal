package com.uber.ridebooking.model;

/**
 * A rider's request for a ride from a pickup to a dropoff location.
 *
 * Created with status {@link TripStatus#REQUESTED}; its status mirrors the
 * associated {@link Trip} as the lifecycle progresses. The request is kept alive
 * even after a Trip is created so that fare estimations and cancellations can
 * reference the original intent without fetching the trip object.
 *
 * Not thread-safe; callers must synchronize on this instance when mutating status.
 */
public class RideRequest {
    /** Unique request identifier (UUID). */
    private final String id;
    /** The rider who submitted this request. */
    private final Rider rider;
    /** Pickup location. */
    private final Location pickup;
    /** Dropoff location. */
    private final Location dropoff;
    /** Vehicle category requested. */
    private final RideType type;
    /** Current lifecycle status; mirrors the paired Trip's status. */
    private TripStatus status;

    /**
     * @param id      unique request identifier
     * @param rider   the requesting rider
     * @param pickup  pickup location
     * @param dropoff dropoff location
     * @param type    vehicle category requested
     */
    public RideRequest(String id, Rider rider, Location pickup, Location dropoff, RideType type) {
        this.id = id;
        this.rider = rider;
        this.pickup = pickup;
        this.dropoff = dropoff;
        this.type = type;
        this.status = TripStatus.REQUESTED;
    }

    /** @return unique request identifier */
    public String getId() {
        return id;
    }

    /** @return the rider who submitted this request */
    public Rider getRider() {
        return rider;
    }

    /** @return pickup location */
    public Location getPickup() {
        return pickup;
    }

    /** @return dropoff location */
    public Location getDropoff() {
        return dropoff;
    }

    /** @return vehicle category requested */
    public RideType getType() {
        return type;
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

    @Override
    public String toString() {
        return String.format("RideRequest{id='%s', rider=%s, pickup=%s, dropoff=%s, type=%s, status=%s}",
                id, rider.getName(), pickup, dropoff, type, status);
    }
}
