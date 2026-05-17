// Companies: Salesforce, Uber
package com.uber.meetingscheduler;

import java.util.*;

/**
 * Manages one-time meeting-room reservations for a fixed set of rooms and employees.
 *
 * <p>Two bookings conflict if their time intervals overlap (inclusive on both ends).
 * Conflict check is O(b) per room where b = number of existing bookings in that room.
 *
 * <p>Data structures: {@code HashMap} for O(1) booking lookup; per-room and per-employee
 * {@code ArrayList}s for iteration. Not thread-safe.
 */
public class MeetingRoomScheduler {
    /** Immutable record of a single booking. */
    private static class Booking {
        final String id;
        final int employee, room, start, end;
        Booking(String id, int emp, int room, int s, int e) {
            this.id = id; this.employee = emp; this.room = room; this.start = s; this.end = e;
        }
    }

    /** Total number of rooms, indexed [0, roomsCount). */
    private final int roomsCount;
    /** Booking ID → Booking for O(1) lookup and cancellation. */
    private final Map<String, Booking> bookings = new HashMap<>();
    /** Room ID → list of bookings in that room. */
    private final Map<Integer, List<Booking>> byRoom = new HashMap<>();
    /** Employee ID → list of bookings for that employee. */
    private final Map<Integer, List<Booking>> byEmployee = new HashMap<>();

    /**
     * @param roomsCount     total number of rooms (IDs 0 to roomsCount-1)
     * @param employeesCount total number of employees (IDs 0 to employeesCount-1)
     */
    public MeetingRoomScheduler(int roomsCount, int employeesCount) {
        this.roomsCount = roomsCount;
        for (int i = 0; i < roomsCount; i++) byRoom.put(i, new ArrayList<>());
        for (int i = 0; i < employeesCount; i++) byEmployee.put(i, new ArrayList<>());
    }

    /**
     * Attempts to reserve a room for an employee.
     *
     * @param bookingId  unique identifier for this booking
     * @param employeeId employee making the reservation
     * @param roomId     room to reserve
     * @param startTime  start of the interval (inclusive)
     * @param endTime    end of the interval (inclusive)
     * @return {@code true} if the booking was created; {@code false} if the room is busy
     *         or the time range is invalid
     */
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

    /**
     * Returns IDs of all rooms that are free throughout the given interval.
     *
     * @param startTime start of the query interval (inclusive)
     * @param endTime   end of the query interval (inclusive)
     * @return sorted list of available room IDs; empty if the interval is invalid
     */
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

    /**
     * Cancels an existing booking.
     *
     * @param bookingId the booking to cancel
     * @return {@code true} if found and removed; {@code false} if not found
     */
    public boolean cancelBooking(String bookingId) {
        Booking b = bookings.remove(bookingId);
        if (b == null) return false;
        byRoom.get(b.room).remove(b);
        byEmployee.get(b.employee).remove(b);
        return true;
    }

    /**
     * Lists booking IDs for a room, sorted by start time then booking ID.
     *
     * @param roomId the room to query
     * @return ordered list of booking IDs
     */
    public List<String> listBookingsForRoom(int roomId) {
        List<Booking> list = new ArrayList<>(byRoom.get(roomId));
        list.sort((a, b) -> a.start != b.start ? Integer.compare(a.start, b.start) : a.id.compareTo(b.id));
        List<String> result = new ArrayList<>();
        for (Booking b : list) result.add(b.id);
        return result;
    }

    /**
     * Lists booking IDs for an employee, sorted by start time then booking ID.
     *
     * @param employeeId the employee to query
     * @return ordered list of booking IDs
     */
    public List<String> listBookingsForEmployee(int employeeId) {
        List<Booking> list = new ArrayList<>(byEmployee.get(employeeId));
        list.sort((a, b) -> a.start != b.start ? Integer.compare(a.start, b.start) : a.id.compareTo(b.id));
        List<String> result = new ArrayList<>();
        for (Booking b : list) result.add(b.id);
        return result;
    }
}
