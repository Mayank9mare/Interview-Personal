package com.uber.trainplatform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrainPlatformManagerTest {
    private TrainPlatformManager mgr;

    @BeforeEach
    void setUp() { mgr = new TrainPlatformManager(3); }

    @Test
    void assignPlatform_firstTrainNoDelay() {
        assertEquals("0,0", mgr.assignPlatform("T1", 10, 5));
    }

    @Test
    void assignPlatform_picksLowestIndex_whenTied() {
        // platform 0,1,2 all free — should pick 0
        assertEquals("0,0", mgr.assignPlatform("T1", 10, 5));
    }

    @Test
    void assignPlatform_picksFreePlatformFirst() {
        mgr.assignPlatform("T1", 0, 10);  // p0 free at 10
        mgr.assignPlatform("T2", 0, 10);  // p1 free at 10 (p1 had delay 0, chosen over p0)
        // T2b arrives at 0: p0 delay=10, p1 delay=10, p2 delay=0 → p2 chosen
        assertEquals("2,0", mgr.assignPlatform("T2b", 0, 5));
    }

    @Test
    void assignPlatform_delayWhenAllBusy() {
        // All 3 platforms busy until time 10
        mgr.assignPlatform("T1", 0, 10);
        mgr.assignPlatform("T2", 0, 10);
        mgr.assignPlatform("T3", 0, 10);
        // New train arrives at 5 — all platforms free at 10, delay = 5 each → pick platform 0
        String result = mgr.assignPlatform("T4", 5, 3);
        assertEquals("0,5", result);
    }

    @Test
    void getTrainAtPlatform_returnsTrainDuringOccupancy() {
        mgr.assignPlatform("T1", 10, 5); // p0, 10-15
        assertEquals("T1", mgr.getTrainAtPlatform(0, 10));
        assertEquals("T1", mgr.getTrainAtPlatform(0, 14));
    }

    @Test
    void getTrainAtPlatform_returnsEmptyAfterDeparture() {
        mgr.assignPlatform("T1", 10, 5); // p0, 10-15
        assertEquals("", mgr.getTrainAtPlatform(0, 15));
        assertEquals("", mgr.getTrainAtPlatform(0, 9));
    }

    @Test
    void getPlatformOfTrain_returnsPlatformDuringOccupancy() {
        mgr.assignPlatform("T1", 10, 5); // p0
        assertEquals(0, mgr.getPlatformOfTrain("T1", 10));
        assertEquals(0, mgr.getPlatformOfTrain("T1", 14));
    }

    @Test
    void getPlatformOfTrain_returnsNegOneWhenNotPresent() {
        mgr.assignPlatform("T1", 10, 5);
        assertEquals(-1, mgr.getPlatformOfTrain("T1", 15));
        assertEquals(-1, mgr.getPlatformOfTrain("T2", 10));
    }

    @Test
    void assignPlatform_sequentialTrainsSamePlatform() {
        // T1 on p0 from 0-5, T2 on p0 from 5+ (no conflict if T2 arrives at 5)
        mgr.assignPlatform("T1", 0, 5); // p0 free at 5
        String r = mgr.assignPlatform("T2", 5, 3); // p0 is exactly free, delay=0
        assertEquals("0,0", r);
    }
}
