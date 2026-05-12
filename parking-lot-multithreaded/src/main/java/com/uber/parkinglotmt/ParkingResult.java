package com.uber.parkinglotmt;

public class ParkingResult {
    public final int status;
    public final String spotId, vehicleNumber, ticketId;

    public ParkingResult(int status, String spotId, String vehicleNumber, String ticketId) {
        this.status = status;
        this.spotId = spotId;
        this.vehicleNumber = vehicleNumber;
        this.ticketId = ticketId;
    }
}
