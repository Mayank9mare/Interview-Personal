package com.uber.ridebooking.model;

/**
 * Immutable geographic coordinate expressed as a (latitude, longitude) pair.
 *
 * Distances are computed with Euclidean approximation — sufficient for an
 * interview context where the coordinate deltas are small.
 * Thread-safe: all fields are final.
 */
public class Location {
    /** Latitude in decimal degrees. */
    private final double lat;
    /** Longitude in decimal degrees. */
    private final double lon;

    /**
     * @param lat latitude in decimal degrees
     * @param lon longitude in decimal degrees
     */
    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    /** @return latitude in decimal degrees */
    public double getLat() {
        return lat;
    }

    /** @return longitude in decimal degrees */
    public double getLon() {
        return lon;
    }

    /**
     * Returns the Euclidean distance between this location and {@code other}.
     * (Simplified; no Haversine needed for interview purposes.)
     *
     * @param other the target location
     * @return Euclidean distance in the same unit as the coordinate values
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
