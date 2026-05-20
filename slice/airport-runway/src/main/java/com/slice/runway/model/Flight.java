package com.slice.runway.model;

public class Flight {

    private final String flightId;
    private final String airline;

    public Flight(String flightId, String airline) {
        this.flightId = flightId;
        this.airline  = airline;
    }

    public String getFlightId() { return flightId; }
    public String getAirline()  { return airline; }

    @Override
    public String toString() {
        return String.format("Flight{id='%s', airline='%s'}", flightId, airline);
    }
}
