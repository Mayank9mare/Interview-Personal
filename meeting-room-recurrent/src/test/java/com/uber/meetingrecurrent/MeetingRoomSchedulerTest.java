package com.uber.meetingrecurrent;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MeetingRoomSchedulerTest {
    @Test
    void example1() {
        MeetingRoomScheduler s = new MeetingRoomScheduler(2, 2);
        assertEquals(List.of(0, 1), s.getAvailableRooms(10, 12));
        assertTrue(s.bookRoom("b1", 0, 0, 10, 3, 5)); // occ: [10,12],[15,17],...
        assertEquals(List.of(1), s.getAvailableRooms(12, 12));
        assertFalse(s.bookRoom("b2", 1, 0, 12, 1, 10)); // boundary overlap with [10,12]
        assertFalse(s.bookRoom("b3", 1, 0, 16, 1, 10)); // overlaps [15,17]
        assertTrue(s.bookRoom("b4", 1, 1, 12, 2, 6));
        assertEquals(List.of(), s.getAvailableRooms(12, 12));
        List<String> room0 = s.listBookingsForRoom(0, 5);
        assertEquals(List.of("b1-10-12","b1-15-17","b1-20-22","b1-25-27","b1-30-32"), room0);
        List<String> room1 = s.listBookingsForRoom(1, 4);
        assertEquals(List.of("b4-12-13","b4-18-19","b4-24-25","b4-30-31"), room1);
        assertEquals(List.of("b1-10-12","b1-15-17","b1-20-22"), s.listBookingsForEmployee(0, 3));
        assertEquals(List.of("b4-12-13","b4-18-19","b4-24-25"), s.listBookingsForEmployee(1, 3));
        assertTrue(s.cancelBooking("b1"));
        assertEquals(List.of(0), s.getAvailableRooms(10, 12));
        assertFalse(s.cancelBooking("b1"));
    }

    @Test
    void example2_invalidInputs() {
        MeetingRoomScheduler s = new MeetingRoomScheduler(1, 1);
        assertEquals(List.of(), s.listBookingsForRoom(0, 5));
        assertFalse(s.bookRoom("x1", 0, 0, -1, 1, 5)); // negative start
        assertFalse(s.bookRoom("x2", 0, 0, 10, 0, 5)); // duration=0
        assertFalse(s.bookRoom("x3", 0, 0, 10, 5, 5)); // duration==repeatDuration
        assertTrue(s.bookRoom("x4", 0, 0, 20, 1, 7));
        assertEquals(List.of(), s.getAvailableRooms(30, 10));
        assertEquals(List.of(), s.getAvailableRooms(20, 20));
        // x5 would overlap x4 at occurrence 69: x4 at 20+7*7=69, x5 at 21+8*6=69
        assertFalse(s.bookRoom("x5", 0, 0, 21, 1, 8));
        List<String> room = s.listBookingsForRoom(0, 6);
        assertEquals(List.of("x4-20-20","x4-27-27","x4-34-34","x4-41-41","x4-48-48","x4-55-55"), room);
        assertFalse(s.cancelBooking("does-not-exist"));
        assertTrue(s.cancelBooking("x4"));
        assertEquals(List.of(0), s.getAvailableRooms(20, 20));
    }
}
