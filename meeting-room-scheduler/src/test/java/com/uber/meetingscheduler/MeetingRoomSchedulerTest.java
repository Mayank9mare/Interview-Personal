package com.uber.meetingscheduler;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MeetingRoomSchedulerTest {
    @Test
    void example1() {
        MeetingRoomScheduler s = new MeetingRoomScheduler(2, 2);
        assertEquals(List.of(0, 1), s.getAvailableRooms(10, 20));
        assertTrue(s.bookRoom("b1", 0, 0, 10, 20));
        assertFalse(s.bookRoom("b2", 1, 0, 15, 25)); // overlap
        assertFalse(s.bookRoom("b3", 1, 0, 20, 30)); // boundary overlap
        assertTrue(s.bookRoom("b4", 1, 0, 21, 30));
        assertTrue(s.bookRoom("b5", 0, 1, 15, 25));
        assertEquals(List.of(), s.getAvailableRooms(20, 20));
        assertEquals(List.of("b1", "b4"), s.listBookingsForRoom(0));
        assertEquals(List.of("b5"), s.listBookingsForRoom(1));
        assertEquals(List.of("b1", "b5"), s.listBookingsForEmployee(0));
        assertEquals(List.of("b4"), s.listBookingsForEmployee(1));
        assertFalse(s.cancelBooking("b2"));
        assertTrue(s.cancelBooking("b1"));
        assertFalse(s.cancelBooking("b1"));
        assertEquals(List.of(0), s.getAvailableRooms(10, 20));
    }

    @Test
    void example2_edgeCases() {
        MeetingRoomScheduler s = new MeetingRoomScheduler(1, 1);
        assertEquals(List.of(), s.listBookingsForRoom(0));
        assertEquals(List.of(), s.listBookingsForEmployee(0));
        assertFalse(s.bookRoom("x1", 0, 0, 50, 49)); // invalid
        assertEquals(List.of(), s.getAvailableRooms(70, 10)); // invalid query
        assertTrue(s.bookRoom("x2", 0, 0, 60, 60));
        assertEquals(List.of(), s.getAvailableRooms(60, 60));
        assertTrue(s.bookRoom("x3", 0, 0, 61, 61));
        assertFalse(s.bookRoom("x4", 0, 0, 60, 61));
        assertFalse(s.cancelBooking("does-not-exist"));
        assertTrue(s.cancelBooking("x2"));
        assertEquals(List.of(0), s.getAvailableRooms(60, 60));
    }

    @Test
    void startTimeNegative_returnsFalse() {
        MeetingRoomScheduler s = new MeetingRoomScheduler(1, 1);
        assertFalse(s.bookRoom("b1", 0, 0, -1, 10));
    }
}
