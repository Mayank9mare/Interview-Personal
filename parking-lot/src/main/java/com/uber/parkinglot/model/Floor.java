package com.uber.parkinglot.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Floor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public Floor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = new ArrayList<>(spots);
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public List<ParkingSpot> getSpots() {
        return spots;
    }

    /**
     * Returns list of available spots that can fit the given vehicle type.
     */
    public List<ParkingSpot> getAvailableSpots(VehicleType type) {
        return spots.stream()
                .filter(spot -> spot.isAvailable() && spot.canFit(type))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Floor{floorNumber=" + floorNumber + ", totalSpots=" + spots.size() + "}";
    }
}
