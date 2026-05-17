package com.uber.parkinglot;

import com.uber.parkinglot.model.*;
import com.uber.parkinglot.strategy.PricingStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Top-level facade for the parking lot system.
 *
 * <p>Manages a fixed collection of {@link Floor}s and a map of in-flight {@link Ticket}s keyed by
 * ticket ID. Core operations are {@link #park} (assign a spot, issue a ticket) and
 * {@link #unpark} (calculate fee, free the spot, return the fee). Spot assignment follows a
 * first-fit policy: floors are tried in iteration order, and within a floor the first available
 * compatible spot is chosen.
 *
 * <p>Data structures: {@code List<Floor>} for ordered floor iteration;
 * {@code HashMap<String, Ticket>} for O(1) ticket lookup by ID.
 *
 * <p>Not thread-safe; callers must synchronise externally if shared across threads.
 */
public class ParkingLot {
    /** Human-readable name of this lot. */
    private final String name;
    /** Ordered list of floors; park() iterates from index 0 upward. */
    private final List<Floor> floors;
    /** All tickets for vehicles currently parked; keyed by ticket ID. */
    private final Map<String, Ticket> activeTickets;
    /** Strategy used to compute fees at checkout. */
    private final PricingStrategy pricingStrategy;

    /**
     * Constructs a parking lot with the given floors and pricing strategy.
     *
     * @param name            display name of this lot
     * @param floors          floors in preferred assignment order; defensive copy is made
     * @param pricingStrategy strategy used to calculate fees at checkout
     */
    public ParkingLot(String name, List<Floor> floors, PricingStrategy pricingStrategy) {
        this.name = name;
        this.floors = new ArrayList<>(floors);
        this.activeTickets = new HashMap<>();
        this.pricingStrategy = pricingStrategy;
    }

    /**
     * Parks a vehicle.
     * Iterates floors in order (lowest first), finds the first available spot
     * that can fit the vehicle type, creates a ticket, and returns it.
     *
     * @param vehicle the vehicle to park
     * @return the generated Ticket, or null if no spot is available
     */
    public Ticket park(Vehicle vehicle) {
        for (Floor floor : floors) {
            List<ParkingSpot> availableSpots = floor.getAvailableSpots(vehicle.getType());
            if (!availableSpots.isEmpty()) {
                ParkingSpot spot = availableSpots.get(0);
                spot.park(vehicle);
                String ticketId = UUID.randomUUID().toString();
                Ticket ticket = new Ticket(ticketId, vehicle, spot, System.currentTimeMillis());
                activeTickets.put(ticketId, ticket);
                return ticket;
            }
        }
        // No available spot found
        return null;
    }

    /**
     * Unparks a vehicle by ticket ID.
     * Sets exit time, calculates fee, frees the spot, removes from active tickets.
     *
     * @param ticketId the ticket ID
     * @return the calculated fee
     * @throws IllegalArgumentException if ticket is not found
     */
    public double unpark(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("No active ticket found with ID: " + ticketId);
        }
        ticket.setExitTime(System.currentTimeMillis());
        double fee = pricingStrategy.calculate(ticket);
        ticket.setFee(fee);
        ticket.getSpot().unpark();
        activeTickets.remove(ticketId);
        return fee;
    }

    /**
     * Returns the total count of available spots for the given vehicle type
     * across all floors.
     */
    public int getAvailableSpotCount(VehicleType type) {
        return floors.stream()
                .mapToInt(floor -> floor.getAvailableSpots(type).size())
                .sum();
    }

    /**
     * Returns all currently active (parked) tickets.
     */
    public List<Ticket> getActiveTickets() {
        return new ArrayList<>(activeTickets.values());
    }

    /**
     * Returns the display name of this lot.
     *
     * @return lot name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the list of floors in this lot (backed by the internal list; do not mutate).
     *
     * @return list of floors
     */
    public List<Floor> getFloors() {
        return floors;
    }

    @Override
    public String toString() {
        return "ParkingLot{name='" + name + "', floors=" + floors.size() +
               ", activeTickets=" + activeTickets.size() + "}";
    }
}
