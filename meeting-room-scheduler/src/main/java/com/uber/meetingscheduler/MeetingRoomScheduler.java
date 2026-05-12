// Companies: Salesforce, Uber
package com.uber.meetingscheduler;

import java.util.*;

public class MeetingRoomScheduler {
    private static class Booking {
        final String id;
        final int employee, room, start, end;
        Booking(String id, int emp, int room, int s, int e) {
            this.id = id; this.employee = emp; this.room = room; this.start = s; this.end = e;
        }
    }

    private final int roomsCount;
    private final Map<String, Booking> bookings = new HashMap<>();
    private final Map<Integer, List<Booking>> byRoom = new HashMap<>();
    private final Map<Integer, List<Booking>> byEmployee = new HashMap<>();

    public MeetingRoomScheduler(int roomsCount, int employeesCount) {
        this.roomsCount = roomsCount;
        for (int i = 0; i < roomsCount; i++) byRoom.put(i, new ArrayList<>());
        for (int i = 0; i < employeesCount; i++) byEmployee.put(i, new ArrayList<>());
    }

    public boolean bookRoom(String bookingId, int employeeId, int roomId, int startTime, int endTime) {
        if (startTime > endTime || startTime < 0) return false;
        for (Booking b : byRoom.get(roomId)) {
            if (startTime <= b.end && b.start <= endTime) return false;
        }
        Booking b = new Booking(bookingId, employeeId, roomId, startTime, endTime);
        bookings.put(bookingId, b);
        byRoom.get(roomId).add(b);
        byEmployee.get(employeeId).add(b);
        return true;
    }

    public List<Integer> getAvailableRooms(int startTime, int endTime) {
        if (startTime > endTime) return Collections.emptyList();
        List<Integer> result = new ArrayList<>();
        for (int r = 0; r < roomsCount; r++) {
            boolean free = true;
            for (Booking b : byRoom.get(r)) {
                if (startTime <= b.end && b.start <= endTime) { free = false; break; }
            }
            if (free) result.add(r);
        }
        return result;
    }

    public boolean cancelBooking(String bookingId) {
        Booking b = bookings.remove(bookingId);
        if (b == null) return false;
        byRoom.get(b.room).remove(b);
        byEmployee.get(b.employee).remove(b);
        return true;
    }

    public List<String> listBookingsForRoom(int roomId) {
        List<Booking> list = new ArrayList<>(byRoom.get(roomId));
        list.sort((a, b) -> a.start != b.start ? Integer.compare(a.start, b.start) : a.id.compareTo(b.id));
        List<String> result = new ArrayList<>();
        for (Booking b : list) result.add(b.id);
        return result;
    }

    public List<String> listBookingsForEmployee(int employeeId) {
        List<Booking> list = new ArrayList<>(byEmployee.get(employeeId));
        list.sort((a, b) -> a.start != b.start ? Integer.compare(a.start, b.start) : a.id.compareTo(b.id));
        List<String> result = new ArrayList<>();
        for (Booking b : list) result.add(b.id);
        return result;
    }
}
