package com.uber.meetingroom;

import java.util.*;

public class RoomBooking {
    private final List<String> rooms;
    private final Map<String, List<int[]>> roomBookings = new HashMap<>();
    private final Map<String, Object[]> meetings = new HashMap<>();

    public RoomBooking(List<String> roomIds) {
        this.rooms = new ArrayList<>(roomIds);
        Collections.sort(this.rooms);
        for (String r : rooms) roomBookings.put(r, new ArrayList<>());
    }

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

    public boolean cancelMeeting(String meetingId) {
        Object[] info = meetings.remove(meetingId);
        if (info == null) return false;
        String room = (String) info[0];
        int s = (int) info[1], e = (int) info[2];
        roomBookings.get(room).removeIf(b -> b[0] == s && b[1] == e);
        return true;
    }

    private boolean isAvailable(String room, int start, int end) {
        for (int[] b : roomBookings.get(room)) {
            if (start <= b[1] && b[0] <= end) return false;
        }
        return true;
    }
}
