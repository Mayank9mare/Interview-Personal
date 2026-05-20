package com.slice.runway.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class RunwayBooking {

    private final String        bookingId;
    private final String        runwayId;
    private final String        flightId;
    private final BookingType   type;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private volatile BookingStatus status;

    public RunwayBooking(String runwayId, String flightId,
                         BookingType type,
                         LocalDateTime startTime, LocalDateTime endTime) {
        this.bookingId = UUID.randomUUID().toString().substring(0, 8);
        this.runwayId  = runwayId;
        this.flightId  = flightId;
        this.type      = type;
        this.startTime = startTime;
        this.endTime   = endTime;
        this.status    = BookingStatus.CONFIRMED;
    }

    public String        getBookingId() { return bookingId; }
    public String        getRunwayId()  { return runwayId; }
    public String        getFlightId()  { return flightId; }
    public BookingType   getType()      { return type; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime()   { return endTime; }
    public BookingStatus getStatus()    { return status; }

    public void cancel() { this.status = BookingStatus.CANCELLED; }

    @Override
    public String toString() {
        return String.format("Booking{id=%s, runway=%s, flight=%s, type=%s, %s→%s, status=%s}",
                bookingId, runwayId, flightId, type, startTime, endTime, status);
    }
}
