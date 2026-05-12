package com.uber.ridebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Driver {
    private final String id;
    private final String name;
    private Location currentLocation;
    private boolean available;
    private final List<Trip> tripHistory;

    public Driver(String id, String name, Location currentLocation) {
        this.id = id;
        this.name = name;
        this.currentLocation = currentLocation;
        this.available = true;
        this.tripHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<Trip> getTripHistory() {
        return Collections.unmodifiableList(tripHistory);
    }

    public void addTrip(Trip trip) {
        tripHistory.add(trip);
    }

    @Override
    public String toString() {
        return String.format("Driver{id='%s', name='%s', location=%s, available=%b}",
                id, name, currentLocation, available);
    }
}
