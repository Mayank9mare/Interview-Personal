package com.uber.elevator;

import java.util.*;

/**
 * Simulates a building's elevator system with a greedy dispatch algorithm.
 *
 * <p>Dispatch heuristic: an IDLE elevator is scored by distance to the request floor.
 * A moving elevator that is already travelling in the request direction and has not
 * yet passed the floor is scored by distance; all other moving elevators incur a
 * large penalty (10 000) plus distance to discourage preemption.
 *
 * <p>Simulation is stepped manually via {@link #step()}. Each call moves every elevator
 * one floor toward its next stop (or serves the current floor if it is already a stop).
 *
 * <p>Not thread-safe.
 */
public class ElevatorSystem {
    /** Direction of travel for an elevator. */
    public enum Direction { UP, DOWN, IDLE }

    /**
     * State of a single elevator car.
     */
    public static class Elevator {
        /** Zero-based elevator identifier. */
        public final int id;
        /** Current floor. */
        private int floor;
        /** Current direction of travel. */
        private Direction direction = Direction.IDLE;
        /** Pending stops sorted ascending; used to determine next move. */
        private final TreeSet<Integer> stops = new TreeSet<>();

        private Elevator(int id, int floor) {
            this.id = id;
            this.floor = floor;
        }

        /** @return current floor the elevator is at */
        public int getFloor() { return floor; }
        /** @return current direction of travel */
        public Direction getDirection() { return direction; }
        /** @return unmodifiable view of the pending stop floors */
        public Set<Integer> getStops() { return Collections.unmodifiableSet(stops); }
    }

    /** All elevator cars; index equals the elevator's ID. */
    private final List<Elevator> elevators = new ArrayList<>();
    /** Lowest valid floor in the building. */
    private final int minFloor;
    /** Highest valid floor in the building. */
    private final int maxFloor;

    /**
     * @param elevatorCount number of elevator cars (IDs 0 to elevatorCount-1); all start at minFloor
     * @param minFloor      lowest valid floor
     * @param maxFloor      highest valid floor
     */
    public ElevatorSystem(int elevatorCount, int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        for (int i = 0; i < elevatorCount; i++) elevators.add(new Elevator(i, minFloor));
    }

    /**
     * Handles a hall-button press: assigns the best elevator to pick up a rider.
     *
     * @param floor     floor where the rider is waiting
     * @param direction the direction the rider intends to travel
     * @return ID of the elevator assigned to service the request
     * @throws IllegalArgumentException if {@code floor} is outside [minFloor, maxFloor]
     */
    public int requestPickup(int floor, Direction direction) {
        validateFloor(floor);
        Elevator elevator = chooseElevator(floor, direction);
        elevator.stops.add(floor);
        updateDirection(elevator);
        return elevator.id;
    }

    /**
     * Handles a cabin-button press: adds a destination floor to an elevator.
     *
     * @param elevatorId ID of the elevator (as returned by {@link #requestPickup})
     * @param floor      destination floor the rider selected inside the cabin
     * @throws IllegalArgumentException if {@code floor} is outside [minFloor, maxFloor]
     */
    public void requestDrop(int elevatorId, int floor) {
        validateFloor(floor);
        Elevator elevator = elevators.get(elevatorId);
        elevator.stops.add(floor);
        updateDirection(elevator);
    }

    /**
     * Advances the simulation by one time unit.
     *
     * <p>Each elevator either serves its current floor (if it is a scheduled stop)
     * or moves one floor in its current direction.
     */
    public void step() {
        for (Elevator elevator : elevators) {
            if (elevator.stops.remove(elevator.floor)) {
                updateDirection(elevator);
                continue;
            }
            if (elevator.direction == Direction.UP) elevator.floor++;
            else if (elevator.direction == Direction.DOWN) elevator.floor--;

            if (elevator.stops.remove(elevator.floor)) updateDirection(elevator);
        }
    }

    /**
     * @param id elevator ID
     * @return the {@link Elevator} object for inspection
     */
    public Elevator getElevator(int id) {
        return elevators.get(id);
    }

    /** Picks the lowest-scored available elevator for a pickup request. */
    private Elevator chooseElevator(int floor, Direction requestDirection) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt((Elevator e) -> score(e, floor, requestDirection))
                        .thenComparingInt(e -> e.id))
                .orElseThrow();
    }

    /** Returns a lower score for elevators that can serve {@code floor} with minimal detour. */
    private int score(Elevator e, int floor, Direction requestDirection) {
        if (e.direction == Direction.IDLE) return Math.abs(e.floor - floor);
        boolean canServeOnWay = e.direction == requestDirection
                && ((e.direction == Direction.UP && e.floor <= floor)
                || (e.direction == Direction.DOWN && e.floor >= floor));
        return canServeOnWay ? Math.abs(e.floor - floor) : 10_000 + Math.abs(e.floor - floor);
    }

    /** Recalculates {@code e.direction} based on its current floor and remaining stops. */
    private void updateDirection(Elevator e) {
        if (e.stops.isEmpty()) {
            e.direction = Direction.IDLE;
        } else if (e.stops.ceiling(e.floor) != null && e.stops.ceiling(e.floor) != e.floor) {
            e.direction = Direction.UP;
        } else if (e.stops.floor(e.floor) != null && e.stops.floor(e.floor) != e.floor) {
            e.direction = Direction.DOWN;
        } else {
            e.direction = Direction.IDLE;
        }
    }

    /** @throws IllegalArgumentException if {@code floor} is outside [minFloor, maxFloor] */
    private void validateFloor(int floor) {
        if (floor < minFloor || floor > maxFloor) throw new IllegalArgumentException("invalid floor");
    }
}
