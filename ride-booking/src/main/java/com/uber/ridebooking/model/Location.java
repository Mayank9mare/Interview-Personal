package com.uber.ridebooking.model;

public class Location {
    private final double lat;
    private final double lon;

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    /**
     * Returns Euclidean distance between two locations.
     * (Simplified; no Haversine needed for interview purposes.)
     */
    public double distanceTo(Location other) {
        double dLat = this.lat - other.lat;
        double dLon = this.lon - other.lon;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", lat, lon);
    }
}
