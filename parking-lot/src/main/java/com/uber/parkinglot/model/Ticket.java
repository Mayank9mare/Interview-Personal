package com.uber.parkinglot.model;

/**
 * Records a single parking session: the vehicle, the assigned spot, entry/exit timestamps,
 * and the computed fee.
 *
 * <p>A ticket is created at entry ({@code exitTime = 0}, {@code fee = 0}). On checkout,
 * {@link #setExitTime} and {@link #setFee} are populated by the lot before the spot is freed.
 * The invariant {@code exitTime > 0 && fee >= 0} holds for a completed ticket.
 *
 * <p>Not thread-safe.
 */
public class Ticket {
    /** Unique identifier for this parking session. */
    private final String ticketId;
    /** Vehicle that initiated this session. */
    private final Vehicle vehicle;
    /** Spot assigned for this session; remains fixed for the session's lifetime. */
    private final ParkingSpot spot;
    /** Wall-clock entry time in milliseconds since epoch. */
    private long entryTime;
    /** Wall-clock exit time in milliseconds since epoch; {@code 0} until checkout. */
    private long exitTime;
    /** Parking fee in currency units; {@code 0.0} until checkout. */
    private double fee;

    /**
     * Constructs a new ticket for a parking session that is just starting.
     *
     * @param ticketId  unique session identifier
     * @param vehicle   vehicle being parked
     * @param spot      spot assigned to this vehicle
     * @param entryTime entry timestamp in milliseconds since epoch
     */
    public Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = 0;
        this.fee = 0.0;
    }

    /**
     * Returns the unique session identifier.
     *
     * @return ticket id
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * Returns the vehicle associated with this session.
     *
     * @return vehicle
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     * Returns the spot assigned for this session.
     *
     * @return parking spot
     */
    public ParkingSpot getSpot() {
        return spot;
    }

    /**
     * Returns the entry timestamp in milliseconds since epoch.
     *
     * @return entry time
     */
    public long getEntryTime() {
        return entryTime;
    }

    /**
     * Overrides the entry timestamp (useful for testing with a fixed clock).
     *
     * @param entryTime entry time in milliseconds since epoch
     */
    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Returns the exit timestamp in milliseconds since epoch, or {@code 0} if not yet checked out.
     *
     * @return exit time, or {@code 0}
     */
    public long getExitTime() {
        return exitTime;
    }

    /**
     * Sets the exit timestamp at checkout time.
     *
     * @param exitTime exit time in milliseconds since epoch
     */
    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    /**
     * Returns the computed parking fee, or {@code 0.0} if checkout has not occurred yet.
     *
     * @return fee in currency units
     */
    public double getFee() {
        return fee;
    }

    /**
     * Sets the fee after it has been calculated by the pricing strategy.
     *
     * @param fee computed fee in currency units
     */
    public void setFee(double fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "Ticket{ticketId='" + ticketId + "', vehicle=" + vehicle +
               ", spot=" + spot.getSpotId() + ", entryTime=" + entryTime + ", fee=" + fee + "}";
    }
}
