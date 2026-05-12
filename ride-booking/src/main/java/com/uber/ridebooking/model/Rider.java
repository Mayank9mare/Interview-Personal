package com.uber.ridebooking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rider {
    private final String id;
    private final String name;
    private final List<Trip> tripHistory;

    public Rider(String id, String name) {
        this.id = id;
        this.name = name;
        this.tripHistory = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Trip> getTripHistory() {
        return Collections.unmodifiableList(tripHistory);
    }

    public void addTrip(Trip trip) {
        tripHistory.add(trip);
    }

    @Override
    public String toString() {
        return String.format("Rider{id='%s', name='%s'}", id, name);
    }
}
