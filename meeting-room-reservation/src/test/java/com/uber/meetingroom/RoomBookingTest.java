package com.uber.meetingroom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RoomBookingTest {
    private RoomBooking rb;

    @BeforeEach
    void setUp() {
        rb = new RoomBooking(List.of("room-c", "room-a", "room-b"));
    }

    @Test
    void bookMeeting_returnsLexSmallestRoom() {
        assertEquals("room-a", rb.bookMeeting("m1", 10, 20));
    }

    @Test
    void bookMeeting_avoidsConflict() {
        rb.bookMeeting("m1", 10, 20);
        assertEquals("room-b", rb.bookMeeting("m2", 10, 20));
    }

    @Test
    void bookMeeting_returnsEmptyWhenAllBusy() {
        rb.bookMeeting("m1", 10, 20);
        rb.bookMeeting("m2", 10, 20);
        rb.bookMeeting("m3", 10, 20);
        assertEquals("", rb.bookMeeting("m4", 10, 20));
    }

    @Test
    void bookMeeting_adjacentTimeSlotsDoNotConflict() {
        rb.bookMeeting("m1", 10, 20);
        // 21 onwards should be fine in the same room
        assertEquals("room-a", rb.bookMeeting("m2", 21, 30));
    }

    @Test
    void bookMeeting_inclusiveBoundaryConflicts() {
        rb.bookMeeting("m1", 10, 20);
        // starts exactly at end -> overlaps (inclusive end)
        assertEquals("room-b", rb.bookMeeting("m2", 20, 30));
    }

    @Test
    void cancelMeeting_freesRoom() {
        rb.bookMeeting("m1", 10, 20);
        rb.bookMeeting("m2", 10, 20);
        rb.bookMeeting("m3", 10, 20);
        assertTrue(rb.cancelMeeting("m1"));
        assertEquals("room-a", rb.bookMeeting("m4", 10, 20));
    }

    @Test
    void cancelMeeting_unknownMeetingReturnsFalse() {
        assertFalse(rb.cancelMeeting("nonexistent"));
    }

    @Test
    void bookMeeting_noRoomsInitialized() {
        RoomBooking empty = new RoomBooking(List.of());
        assertEquals("", empty.bookMeeting("m1", 10, 20));
    }

    @Test
    void bookMeeting_nonOverlappingTimesFitSameRoom() {
        rb.bookMeeting("m1", 1, 5);
        rb.bookMeeting("m2", 6, 10);
        // both in room-a since no conflict
        assertEquals("room-a", rb.bookMeeting("m3", 11, 15));
    }
}
