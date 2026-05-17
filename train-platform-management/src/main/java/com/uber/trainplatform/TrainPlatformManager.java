package com.uber.trainplatform;

import java.util.*;

/**
 * Assigns arriving trains to platforms with the goal of minimising waiting delay.
 * <p>
 * Each platform is modelled as a sequential queue: a train can only enter after the previous
 * occupant has left. The {@link #assignPlatform} method greedily picks the platform that
 * becomes free soonest (minimum delay from the requested arrival time). Platform indices are
 * zero-based. Not thread-safe.
 */
public class TrainPlatformManager {

    /**
     * A single occupancy record: which train occupies a platform during {@code [start, end)}.
     */
    private static class Slot {
        /** The train occupying this slot. */
        final String trainId;
        /** Inclusive start time of occupancy. */
        final int start;
        /** Exclusive end time of occupancy. */
        final int end;

        /**
         * @param trainId identifier of the train
         * @param start   inclusive start time
         * @param end     exclusive end time
         */
        Slot(String trainId, int start, int end) {
            this.trainId = trainId;
            this.start = start;
            this.end = end;
        }
    }

    /** Total number of platforms managed. */
    private final int platformCount;

    /** {@code freeAt[i]} is the earliest time platform {@code i} becomes available. */
    private final int[] freeAt;

    /** Per-platform history of all assigned slots. */
    private final List<Slot>[] slots;

    /** Maps each trainId to its most recently assigned slot for fast reverse lookup. */
    private final Map<String, Slot> trainSlot = new HashMap<>();

    /**
     * @param platformCount number of platforms to manage (indexed 0 to platformCount-1)
     */
    @SuppressWarnings("unchecked")
    public TrainPlatformManager(int platformCount) {
        this.platformCount = platformCount;
        this.freeAt = new int[platformCount];
        this.slots = new ArrayList[platformCount];
        for (int i = 0; i < platformCount; i++) slots[i] = new ArrayList<>();
    }

    /**
     * Assigns the train to the platform with the minimum waiting delay and records the occupancy.
     * If two platforms tie on delay, the one with the lower index wins.
     *
     * @param trainId     unique identifier for the train
     * @param arrivalTime the time at which the train arrives
     * @param waitTime    how long the train will occupy the platform
     * @return a comma-separated string {@code "platformIndex,delay"} where {@code delay} is the
     *         number of time units the train must wait before entering the platform
     */
    public String assignPlatform(String trainId, int arrivalTime, int waitTime) {
        int best = 0;
        int bestDelay = Math.max(0, freeAt[0] - arrivalTime);
        for (int i = 1; i < platformCount; i++) {
            int delay = Math.max(0, freeAt[i] - arrivalTime);
            if (delay < bestDelay) {
                bestDelay = delay;
                best = i;
            }
        }
        int start = arrivalTime + bestDelay;
        int end = start + waitTime;
        freeAt[best] = end;
        Slot slot = new Slot(trainId, start, end);
        slots[best].add(slot);
        trainSlot.put(trainId, slot);
        return best + "," + bestDelay;
    }

    /**
     * Returns the ID of the train occupying a given platform at the specified time.
     *
     * @param platformNumber zero-based platform index
     * @param timestamp      point in time to query
     * @return the train ID whose slot contains {@code timestamp}, or empty string if none
     */
    public String getTrainAtPlatform(int platformNumber, int timestamp) {
        for (Slot s : slots[platformNumber]) {
            if (timestamp >= s.start && timestamp < s.end) return s.trainId;
        }
        return "";
    }

    /**
     * Returns the platform number where the given train is present at the specified time.
     *
     * @param trainId   the train to look up
     * @param timestamp point in time to query
     * @return zero-based platform index, or {@code -1} if the train is not on any platform
     */
    public int getPlatformOfTrain(String trainId, int timestamp) {
        for (int i = 0; i < platformCount; i++) {
            for (Slot s : slots[i]) {
                if (s.trainId.equals(trainId) && timestamp >= s.start && timestamp < s.end) return i;
            }
        }
        return -1;
    }
}
