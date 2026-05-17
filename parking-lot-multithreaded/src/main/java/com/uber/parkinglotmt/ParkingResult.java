package com.uber.parkinglotmt;

/**
 * Immutable return value for {@link ParkingLot} operations.
 *
 * <p>Convention: {@code status == 201} indicates success; {@code status == 404} indicates
 * failure (spot not found, lot full, etc.).
 */
public class ParkingResult {
    /** HTTP-style status code: 201 = success, 404 = not found / lot full. */
    public final int status;
    /** Spot identifier formatted as {@code "floor-row-col"}; empty string on failure. */
    public final String spotId;
    /** Vehicle number associated with the operation. */
    public final String vehicleNumber;
    /** Ticket ID associated with the operation. */
    public final String ticketId;

    /**
     * @param status        201 for success, 404 for failure
     * @param spotId        spot identifier (empty on failure)
     * @param vehicleNumber vehicle registration number
     * @param ticketId      parking ticket identifier
     */
    public ParkingResult(int status, String spotId, String vehicleNumber, String ticketId) {
        this.status = status;
        this.spotId = spotId;
        this.vehicleNumber = vehicleNumber;
        this.ticketId = ticketId;
    }
}
