package com.uber.parkinglotmt;

import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ParkingLotTest {

    /**
     * Single floor, 4x4 grid:
     * Row 0: 4-1, 4-1, 2-1, 2-0
     * Row 1: 2-1, 4-1, 2-1, 2-1
     * Row 2: 4-0, 2-1, 4-0, 2-1
     * Row 3: 4-1, 4-1, 4-1, 2-1
     */
    private static String[][][] singleFloor() {
        return new String[][][]{
            {
                {"4-1", "4-1", "2-1", "2-0"},
                {"2-1", "4-1", "2-1", "2-1"},
                {"4-0", "2-1", "4-0", "2-1"},
                {"4-1", "4-1", "4-1", "2-1"}
            }
        };
    }

    @Test
    void park_assignsFirstAvailableSpot() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult r = lot.park(4, "bh234", "tkt4534");
        assertEquals(201, r.status);
        assertEquals("bh234", r.vehicleNumber);
        assertFalse(r.spotId.isEmpty());
        // First active 4-wheeler spot is floor=0, row=0, col=0
        assertEquals("0-0-0", r.spotId);
    }

    @Test
    void getFreeSpotsCount_decreasesAfterPark() {
        ParkingLot lot = new ParkingLot(singleFloor());
        int before = lot.getFreeSpotsCount(0, 4);
        lot.park(4, "v1", "t1");
        assertEquals(before - 1, lot.getFreeSpotsCount(0, 4));
    }

    @Test
    void removeVehicle_bySpotId() {
        ParkingLot lot = new ParkingLot(singleFloor());
        int freeBefore = lot.getFreeSpotsCount(0, 4);
        ParkingResult r = lot.park(4, "v1", "t1");
        assertEquals(201, lot.removeVehicle(r.spotId, "", ""));
        // Spot should be freed
        assertEquals(freeBefore, lot.getFreeSpotsCount(0, 4));
    }

    @Test
    void removeVehicle_byVehicleNumber() {
        ParkingLot lot = new ParkingLot(singleFloor());
        lot.park(4, "v1", "t1");
        assertEquals(201, lot.removeVehicle("", "v1", ""));
    }

    @Test
    void removeVehicle_byTicketId() {
        ParkingLot lot = new ParkingLot(singleFloor());
        lot.park(4, "v1", "t1");
        assertEquals(201, lot.removeVehicle("", "", "t1"));
    }

    @Test
    void removeVehicle_spotNotOccupied_returns404() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult r = lot.park(4, "v1", "t1");
        lot.removeVehicle(r.spotId, "", "");
        // Second remove should fail
        assertEquals(404, lot.removeVehicle(r.spotId, "", ""));
    }

    @Test
    void searchVehicle_byVehicleNumber_afterRemoval() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult parked = lot.park(4, "v1", "t1");
        lot.removeVehicle("", "v1", "");
        // Search should still return last known spotId
        ParkingResult found = lot.searchVehicle("", "v1", "");
        assertEquals(201, found.status);
        assertEquals(parked.spotId, found.spotId);
    }

    @Test
    void searchVehicle_byTicketId() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult parked = lot.park(2, "v1", "t1");
        ParkingResult found = lot.searchVehicle("", "", "t1");
        assertEquals(201, found.status);
        assertEquals(parked.spotId, found.spotId);
    }

    @Test
    void searchVehicle_bySpotId() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult parked = lot.park(4, "v1", "t1");
        ParkingResult found = lot.searchVehicle(parked.spotId, "", "");
        assertEquals(201, found.status);
        assertEquals("v1", found.vehicleNumber);
        assertEquals("t1", found.ticketId);
    }

    @Test
    void searchVehicle_neverParked_returns404() {
        ParkingLot lot = new ParkingLot(singleFloor());
        ParkingResult r = lot.searchVehicle("", "unknown", "");
        assertEquals(404, r.status);
    }

    @Test
    void park_noSpotAvailable_returns404() {
        // 1-spot parking lot
        ParkingLot lot = new ParkingLot(new String[][][]{{{"4-1"}}});
        lot.park(4, "v1", "t1");
        ParkingResult r = lot.park(4, "v2", "t2");
        assertEquals(404, r.status);
        assertTrue(r.spotId.isEmpty());
    }

    @Test
    void park_wrongType_returns404() {
        // Only 4-wheeler spots
        ParkingLot lot = new ParkingLot(new String[][][]{{{"4-1", "4-1"}}});
        ParkingResult r = lot.park(2, "v1", "t1");
        assertEquals(404, r.status);
    }

    @Test
    void park_inactiveSpot_skipped() {
        // First spot inactive, second active
        ParkingLot lot = new ParkingLot(new String[][][]{{{"4-0", "4-1"}}});
        ParkingResult r = lot.park(4, "v1", "t1");
        assertEquals(201, r.status);
        assertEquals("0-0-1", r.spotId);
    }

    @Test
    void getFreeSpotsCount_multiFloor() {
        // Two floors, each with 2 active 4-wheeler spots
        ParkingLot lot = new ParkingLot(new String[][][]{
            {{"4-1", "4-1"}},
            {{"4-1", "4-1"}}
        });
        assertEquals(2, lot.getFreeSpotsCount(0, 4));
        assertEquals(2, lot.getFreeSpotsCount(1, 4));
        lot.park(4, "v1", "t1"); // parks on floor 0
        assertEquals(1, lot.getFreeSpotsCount(0, 4));
        assertEquals(2, lot.getFreeSpotsCount(1, 4));
    }

    @Test
    void concurrentPark_noDoubleAssignment() throws InterruptedException {
        ParkingLot lot = new ParkingLot(new String[][][]{{{"4-1", "4-1", "4-1", "4-1", "4-1"}}});
        int threads = 5;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        String[] assignedSpots = new String[threads];
        Thread[] ts = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            ts[i] = new Thread(() -> {
                try { barrier.await(); } catch (Exception e) { Thread.currentThread().interrupt(); }
                ParkingResult r = lot.park(4, "v" + idx, "t" + idx);
                assignedSpots[idx] = r.spotId;
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        // All 5 spots assigned, all unique (no double-assignment)
        java.util.Set<String> unique = new java.util.HashSet<>(java.util.Arrays.asList(assignedSpots));
        assertEquals(5, unique.size());
        assertEquals(0, lot.getFreeSpotsCount(0, 4));
    }

    @Test
    void concurrentRemoveAndPark_noRaceCondition() throws InterruptedException {
        // 3 spots; park 3 vehicles, then concurrently remove + park
        ParkingLot lot = new ParkingLot(new String[][][]{{{"4-1", "4-1", "4-1"}}});
        lot.park(4, "v1", "t1");
        lot.park(4, "v2", "t2");
        lot.park(4, "v3", "t3");

        int threads = 3;
        CyclicBarrier barrier = new CyclicBarrier(threads);
        Thread[] ts = new Thread[threads];
        // Each thread removes one vehicle and immediately parks a new one
        String[] vehicles = {"v1", "v2", "v3"};
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            ts[i] = new Thread(() -> {
                try { barrier.await(); } catch (Exception e) { Thread.currentThread().interrupt(); }
                lot.removeVehicle("", vehicles[idx], "");
                lot.park(4, "new_v" + idx, "new_t" + idx);
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        // After all operations lot should still be full (3 new vehicles parked in 3 freed spots)
        assertEquals(0, lot.getFreeSpotsCount(0, 4));
    }
}
