package com.uber.elevator;

import java.util.*;

public class ElevatorSystem {
    public enum Direction { UP, DOWN, IDLE }

    public static class Elevator {
        public final int id;
        private int floor;
        private Direction direction = Direction.IDLE;
        private final TreeSet<Integer> stops = new TreeSet<>();

        private Elevator(int id, int floor) {
            this.id = id;
            this.floor = floor;
        }

        public int getFloor() { return floor; }
        public Direction getDirection() { return direction; }
        public Set<Integer> getStops() { return Collections.unmodifiableSet(stops); }
    }

    private final List<Elevator> elevators = new ArrayList<>();
    private final int minFloor;
    private final int maxFloor;

    public ElevatorSystem(int elevatorCount, int minFloor, int maxFloor) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        for (int i = 0; i < elevatorCount; i++) elevators.add(new Elevator(i, minFloor));
    }

    public int requestPickup(int floor, Direction direction) {
        validateFloor(floor);
        Elevator elevator = chooseElevator(floor, direction);
        elevator.stops.add(floor);
        updateDirection(elevator);
        return elevator.id;
    }

    public void requestDrop(int elevatorId, int floor) {
        validateFloor(floor);
        Elevator elevator = elevators.get(elevatorId);
        elevator.stops.add(floor);
        updateDirection(elevator);
    }

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

    public Elevator getElevator(int id) {
        return elevators.get(id);
    }

    private Elevator chooseElevator(int floor, Direction requestDirection) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt((Elevator e) -> score(e, floor, requestDirection))
                        .thenComparingInt(e -> e.id))
                .orElseThrow();
    }

    private int score(Elevator e, int floor, Direction requestDirection) {
        if (e.direction == Direction.IDLE) return Math.abs(e.floor - floor);
        boolean canServeOnWay = e.direction == requestDirection
                && ((e.direction == Direction.UP && e.floor <= floor)
                || (e.direction == Direction.DOWN && e.floor >= floor));
        return canServeOnWay ? Math.abs(e.floor - floor) : 10_000 + Math.abs(e.floor - floor);
    }

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

    private void validateFloor(int floor) {
        if (floor < minFloor || floor > maxFloor) throw new IllegalArgumentException("invalid floor");
    }
}
