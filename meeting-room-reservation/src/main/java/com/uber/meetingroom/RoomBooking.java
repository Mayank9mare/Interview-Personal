package com.uber.meetingroom;

import java.util.*;

/**
 * Manages meeting-room reservations across a fixed set of rooms.
 * <p>
 * Rooms are stored in sorted order so that {@link #bookMeeting} assigns the lexicographically
 * first available room, providing a deterministic tie-breaking rule.
 * Each room's bookings are kept as a list of {@code [start, end]} integer intervals; overlap
 * is checked with the standard interval-intersection predicate {@code start <= b[1] && b[0] <= end}.
 * Not thread-safe.
 */
public class RoomBooking {
    /** Sorted list of all room IDs available for booking. */
    private final List<String> rooms;

    /** Per-room list of booked intervals stored as {@code [start, end]} pairs. */
    private final Map<String, List<int[]>> roomBookings = new HashMap<>();

    /** Maps meeting ID to {@code [roomId, start, end]} for O(1) cancellation lookup. */
    private final Map<String, Object[]> meetings = new HashMap<>();

    /**
     * @param roomIds collection of room identifiers to manage (order does not matter; sorted internally)
     */
    public RoomBooking(List<String> roomIds) {
        this.rooms = new ArrayList<>(roomIds);
        Collections.sort(this.rooms);
        for (String r : rooms) roomBookings.put(r, new ArrayList<>());
    }

    /**
     * Books the lexicographically first available room for the given time interval.
     *
     * @param meetingId unique identifier for this meeting
     * @param start     start time (inclusive)
     * @param end       end time (inclusive)
     * @return the ID of the assigned room, or empty string if no room is available
     */
    public String bookMeeting(String meetingId, int start, int end) {
        for (String room : rooms) {
            if (isAvailable(room, start, end)) {
                roomBookings.get(room).add(new int[]{start, end});
                meetings.put(meetingId, new Object[]{room, start, end});
                return room;
            }
        }
        return "";
    }

    /**
     * Cancels a previously booked meeting and frees the room slot.
     *
     * @param meetingId the meeting to cancel
     * @return {@code true} if the meeting existed and was cancelled; {@code false} if not found
     */
    public boolean cancelMeeting(String meetingId) {
        Object[] info = meetings.remove(meetingId);
        if (info == null) return false;
        String room = (String) info[0];
        int s = (int) info[1], e = (int) info[2];
        roomBookings.get(room).removeIf(b -> b[0] == s && b[1] == e);
        return true;
    }

    /** Returns {@code true} if {@code room} has no booking that overlaps {@code [start, end]}. */
    private boolean isAvailable(String room, int start, int end) {
        for (int[] b : roomBookings.get(room)) {
            if (start <= b[1] && b[0] <= end) return false;
        }
        return true;
    }
}
