// Companies: Amazon, Microsoft, Goldman Sachs, Uber
package com.uber.parkinglotmt;

import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
    private static final int ACTIVE = 1;

    private final int[][][] spotType;   // [floor][row][col] = 2 or 4
    private final int[][][] spotActive; // [floor][row][col] = 1 if active
    private final boolean[][][] occupied;
    private final int floors, rows, cols;
    private final Object lock = new Object();

    // Track by vehicleNumber and ticketId -> spotId (persists after removal for search)
    private final ConcurrentHashMap<String, String> vehicleToSpot = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> ticketToSpot  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> spotToVehicle = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> spotToTicket  = new ConcurrentHashMap<>();

    public ParkingLot(String[][][] parking) {
        floors = parking.length;
        rows   = parking[0].length;
        cols   = parking[0][0].length;
        spotType   = new int[floors][rows][cols];
        spotActive = new int[floors][rows][cols];
        occupied   = new boolean[floors][rows][cols];
        for (int f = 0; f < floors; f++)
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++) {
                    String[] parts = parking[f][r][c].split("-");
                    spotType[f][r][c]   = Integer.parseInt(parts[0]);
                    spotActive[f][r][c] = Integer.parseInt(parts[1]);
                }
    }

    /**
     * Park a vehicle of the given type. Scans floor -> row -> col order.
     * Returns status=201 with spotId on success, status=404 with empty spotId on failure.
     */
    public ParkingResult park(int vehicleType, String vehicleNumber, String ticketId) {
        synchronized (lock) {
            for (int f = 0; f < floors; f++)
                for (int r = 0; r < rows; r++)
                    for (int c = 0; c < cols; c++) {
                        if (spotActive[f][r][c] == ACTIVE
                                && spotType[f][r][c] == vehicleType
                                && !occupied[f][r][c]) {
                            occupied[f][r][c] = true;
                            String spotId = f + "-" + r + "-" + c;
                            vehicleToSpot.put(vehicleNumber, spotId);
                            ticketToSpot.put(ticketId, spotId);
                            spotToVehicle.put(spotId, vehicleNumber);
                            spotToTicket.put(spotId, ticketId);
                            return new ParkingResult(201, spotId, vehicleNumber, ticketId);
                        }
                    }
            return new ParkingResult(404, "", vehicleNumber, ticketId);
        }
    }

    /**
     * Remove a vehicle identified by exactly one non-blank parameter.
     * Returns 201 on success, 404 if not found or spot already empty.
     */
    public int removeVehicle(String spotId, String vehicleNumber, String ticketId) {
        synchronized (lock) {
            String resolvedSpot = null;
            if (spotId != null && !spotId.isEmpty())
                resolvedSpot = spotId;
            else if (vehicleNumber != null && !vehicleNumber.isEmpty())
                resolvedSpot = vehicleToSpot.get(vehicleNumber);
            else if (ticketId != null && !ticketId.isEmpty())
                resolvedSpot = ticketToSpot.get(ticketId);

            if (resolvedSpot == null) return 404;
            String[] parts = resolvedSpot.split("-");
            int f = Integer.parseInt(parts[0]);
            int r = Integer.parseInt(parts[1]);
            int c = Integer.parseInt(parts[2]);
            if (!occupied[f][r][c]) return 404;
            occupied[f][r][c] = false;
            return 201;
        }
    }

    /**
     * Search for a vehicle by exactly one non-blank parameter.
     * Returns last known spotId even after removal.
     * Returns status=404 if the vehicle was never parked.
     */
    public ParkingResult searchVehicle(String spotId, String vehicleNumber, String ticketId) {
        String resolvedSpot;
        String resolvedVehicle = vehicleNumber != null ? vehicleNumber : "";
        String resolvedTicket  = ticketId != null ? ticketId : "";

        if (spotId != null && !spotId.isEmpty()) {
            resolvedSpot   = spotId;
            resolvedVehicle = spotToVehicle.getOrDefault(spotId, "");
            resolvedTicket  = spotToTicket.getOrDefault(spotId, "");
        } else if (vehicleNumber != null && !vehicleNumber.isEmpty()) {
            resolvedSpot = vehicleToSpot.get(vehicleNumber);
            if (resolvedSpot != null)
                resolvedTicket = spotToTicket.getOrDefault(resolvedSpot, "");
        } else if (ticketId != null && !ticketId.isEmpty()) {
            resolvedSpot = ticketToSpot.get(ticketId);
            if (resolvedSpot != null)
                resolvedVehicle = spotToVehicle.getOrDefault(resolvedSpot, "");
        } else {
            resolvedSpot = null;
        }

        if (resolvedSpot == null)
            return new ParkingResult(404, "", resolvedVehicle, resolvedTicket);
        return new ParkingResult(201, resolvedSpot, resolvedVehicle, resolvedTicket);
    }

    /**
     * Count free active spots of the given vehicleType on the specified floor.
     */
    public int getFreeSpotsCount(int floor, int vehicleType) {
        synchronized (lock) {
            int count = 0;
            for (int r = 0; r < rows; r++)
                for (int c = 0; c < cols; c++)
                    if (spotActive[floor][r][c] == ACTIVE
                            && spotType[floor][r][c] == vehicleType
                            && !occupied[floor][r][c])
                        count++;
            return count;
        }
    }
}
