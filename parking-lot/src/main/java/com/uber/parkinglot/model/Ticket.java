package com.uber.parkinglot.model;

public class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private long entryTime;
    private long exitTime;
    private double fee;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot, long entryTime) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = 0;
        this.fee = 0.0;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    @Override
    public String toString() {
        return "Ticket{ticketId='" + ticketId + "', vehicle=" + vehicle +
               ", spot=" + spot.getSpotId() + ", entryTime=" + entryTime + ", fee=" + fee + "}";
    }
}
