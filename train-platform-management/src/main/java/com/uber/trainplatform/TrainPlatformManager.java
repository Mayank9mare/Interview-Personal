package com.uber.trainplatform;

import java.util.*;

public class TrainPlatformManager {

    private static class Slot {
        final String trainId;
        final int start;
        final int end;

        Slot(String trainId, int start, int end) {
            this.trainId = trainId;
            this.start = start;
            this.end = end;
        }
    }

    private final int platformCount;
    private final int[] freeAt;
    private final List<Slot>[] slots;
    private final Map<String, Slot> trainSlot = new HashMap<>();

    @SuppressWarnings("unchecked")
    public TrainPlatformManager(int platformCount) {
        this.platformCount = platformCount;
        this.freeAt = new int[platformCount];
        this.slots = new ArrayList[platformCount];
        for (int i = 0; i < platformCount; i++) slots[i] = new ArrayList<>();
    }

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

    public String getTrainAtPlatform(int platformNumber, int timestamp) {
        for (Slot s : slots[platformNumber]) {
            if (timestamp >= s.start && timestamp < s.end) return s.trainId;
        }
        return "";
    }

    public int getPlatformOfTrain(String trainId, int timestamp) {
        for (int i = 0; i < platformCount; i++) {
            for (Slot s : slots[i]) {
                if (s.trainId.equals(trainId) && timestamp >= s.start && timestamp < s.end) return i;
            }
        }
        return -1;
    }
}
