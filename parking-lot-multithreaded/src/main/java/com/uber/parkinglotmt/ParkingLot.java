// Companies: Amazon, Microsoft, Goldman Sachs, Uber
package com.uber.parkinglotmt;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe multi-floor parking lot.
 *
 * <p>Spots are addressed as {@code (floor, row, col)} and described by a type (2 = two-wheeler,
 * 4 = four-wheeler) and an active flag. The layout is parsed from the constructor argument.
 *
 * <p>Invariant: {@code occupied[f][r][c]} is only mutated while holding {@code lock};
 * the lookup maps ({@code vehicleToSpot} etc.) are {@link ConcurrentHashMap}s and may be
 * read without the lock. Spot IDs are formatted as {@code "floor-row-col"}.
 *
 * <p>Thread-safe: {@link #park} and {@link #removeVehicle} are fully synchronized;
 * {@link #searchVehicle} reads lock-free maps and is safe for concurrent readers.
 */
public class ParkingLot {
    /** Sentinel value meaning this spot is operational. */
    private static final int ACTIVE = 1;

    /** Vehicle type per spot: 2 = two-wheeler, 4 = four-wheeler. */
    private final int[][][] spotType;
    /** Whether each spot is operational (1) or decommissioned (0). */
    private final int[][][] spotActive;
    /** Whether each active spot is currently occupied. */
    private final boolean[][][] occupied;
    /** Grid dimensions. */
    private final int floors, rows, cols;
    /** Coarse lock protecting {@code occupied} mutations. */
    private final Object lock = new Object();

    /** vehicleNumber → spotId; persists after removal (last-known location). */
    private final ConcurrentHashMap<String, String> vehicleToSpot = new ConcurrentHashMap<>();
    /** ticketId → spotId; persists after removal. */
    private final ConcurrentHashMap<String, String> ticketToSpot  = new ConcurrentHashMap<>();
    /** spotId → vehicleNumber; persists after removal. */
    private final ConcurrentHashMap<String, String> spotToVehicle = new ConcurrentHashMap<>();
    /** spotId → ticketId; persists after removal. */
    private final ConcurrentHashMap<String, String> spotToTicket  = new ConcurrentHashMap<>();

    /**
     * @param parking 3-D array of spot descriptors formatted as {@code "type-active"}
     *                (e.g. {@code "4-1"} for an active four-wheeler spot),
     *                indexed {@code [floor][row][col]}
     */
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
     * Parks a vehicle in the first available spot of the matching type (floor → row → col order).
     *
     * @param vehicleType   2 for two-wheeler, 4 for four-wheeler
     * @param vehicleNumber vehicle registration number
     * @param ticketId      parking ticket identifier
     * @return result with status 201 and the assigned spotId on success;
     *         status 404 and empty spotId when no suitable spot is free
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
     * Frees the spot associated with one of the provided identifiers.
     * Exactly one of the three parameters should be non-blank.
     *
     * @param spotId        direct spot address (or {@code null}/empty to ignore)
     * @param vehicleNumber vehicle number to look up (or {@code null}/empty to ignore)
     * @param ticketId      ticket ID to look up (or {@code null}/empty to ignore)
     * @return 201 on success; 404 if the spot could not be resolved or was already free
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
     * Looks up a vehicle by one of the provided identifiers.
     *
     * <p>Returns the last known spot even after the vehicle has been removed,
     * because the lookup maps are never cleared.
     *
     * @param spotId        spot address to resolve (or {@code null}/empty)
     * @param vehicleNumber vehicle number to look up (or {@code null}/empty)
     * @param ticketId      ticket ID to look up (or {@code null}/empty)
     * @return result with status 201 and all known fields on success;
     *         status 404 if the vehicle was never parked
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
     * Counts free, active spots of the given vehicle type on a floor.
     *
     * @param floor       zero-based floor index
     * @param vehicleType 2 for two-wheeler, 4 for four-wheeler
     * @return number of unoccupied active spots matching the type
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
