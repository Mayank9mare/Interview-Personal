// Companies: Salesforce, Uber
package com.uber.meetingrecurrent;

import java.util.*;

/**
 * Meeting-room scheduler that supports recurring reservations.
 *
 * <p>Each booking repeats indefinitely, but conflict checking is performed across
 * the first {@value #OCCURRENCES} occurrences. A booking is valid only when its
 * {@code duration < repeatDuration} (no overlap within its own series).
 *
 * <p>Conflict check complexity: O(b × OCCURRENCES²) per {@code bookRoom} call where
 * b is the number of existing bookings in the target room.
 *
 * <p>Not thread-safe.
 */
public class MeetingRoomScheduler {
    /** Number of future occurrences used for conflict detection. */
    private static final int OCCURRENCES = 20;

    /** Immutable description of a recurring reservation. */
    private static class RecurringBooking {
        final String id;
        final int employee, room, startTime, duration, repeatDuration;
        RecurringBooking(String id, int emp, int room, int s, int dur, int rep) {
            this.id = id; this.employee = emp; this.room = room;
            this.startTime = s; this.duration = dur; this.repeatDuration = rep;
        }
        int occurrenceStart(int k) { return startTime + k * repeatDuration; }
        int occurrenceEnd(int k) { return occurrenceStart(k) + duration - 1; }
    }

    /** Total number of rooms, indexed [0, roomsCount). */
    private final int roomsCount;
    /** Booking ID → RecurringBooking for O(1) lookup. */
    private final Map<String, RecurringBooking> bookings = new HashMap<>();
    /** Room ID → list of recurring bookings in that room. */
    private final Map<Integer, List<RecurringBooking>> byRoom = new HashMap<>();
    /** Employee ID → list of recurring bookings for that employee. */
    private final Map<Integer, List<RecurringBooking>> byEmployee = new HashMap<>();

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
     * Attempts to create a recurring room reservation.
     *
     * @param bookingId      unique identifier for this booking
     * @param employeeId     employee making the reservation
     * @param roomId         room to reserve
     * @param startTime      start time of the first occurrence
     * @param duration       length of each occurrence (must be &lt; repeatDuration)
     * @param repeatDuration period between occurrence starts (must be &gt; duration)
     * @return {@code true} if the booking was created; {@code false} if it would
     *         conflict with an existing booking within the first {@value #OCCURRENCES}
     *         occurrences, or if the arguments are invalid
     */
    public boolean bookRoom(String bookingId, int employeeId, int roomId,
                            int startTime, int duration, int repeatDuration) {
        if (startTime < 0 || duration <= 0 || duration >= repeatDuration) return false;
        RecurringBooking newB = new RecurringBooking(bookingId, employeeId, roomId, startTime, duration, repeatDuration);
        // Check all 20 occurrences of new booking against all existing bookings in this room
        for (RecurringBooking existing : byRoom.get(roomId)) {
            for (int ki = 0; ki < OCCURRENCES; ki++) {
                int s1 = newB.occurrenceStart(ki), e1 = newB.occurrenceEnd(ki);
                for (int kj = 0; kj < OCCURRENCES; kj++) {
                    int s2 = existing.occurrenceStart(kj), e2 = existing.occurrenceEnd(kj);
                    if (s1 <= e2 && s2 <= e1) return false;
                }
            }
        }
        bookings.put(bookingId, newB);
        byRoom.get(roomId).add(newB);
        byEmployee.get(employeeId).add(newB);
        return true;
    }

    /**
     * Returns IDs of all rooms that have no recurring occurrence overlapping the given window.
     *
     * @param startTime start of the query interval (inclusive)
     * @param endTime   end of the query interval (inclusive)
     * @return sorted list of free room IDs; empty if the interval is invalid
     */
    public List<Integer> getAvailableRooms(int startTime, int endTime) {
        if (startTime > endTime) return Collections.emptyList();
        List<Integer> result = new ArrayList<>();
        outer:
        for (int r = 0; r < roomsCount; r++) {
            for (RecurringBooking b : byRoom.get(r)) {
                for (int k = 0; k < OCCURRENCES; k++) {
                    if (startTime <= b.occurrenceEnd(k) && b.occurrenceStart(k) <= endTime) continue outer;
                }
            }
            result.add(r);
        }
        return result;
    }

    /**
     * Cancels an existing recurring booking (all future occurrences).
     *
     * @param bookingId the booking to cancel
     * @return {@code true} if found and removed; {@code false} if not found
     */
    public boolean cancelBooking(String bookingId) {
        RecurringBooking b = bookings.remove(bookingId);
        if (b == null) return false;
        byRoom.get(b.room).remove(b);
        byEmployee.get(b.employee).remove(b);
        return true;
    }

    /**
     * Lists the next {@code n} occurrences (across all recurring bookings) for a room,
     * sorted by occurrence start time then booking ID.
     *
     * @param roomId room to query
     * @param n      maximum number of occurrences to return
     * @return list of strings formatted as {@code "bookingId-startTime-endTime"}
     */
    public List<String> listBookingsForRoom(int roomId, int n) {
        return listN(byRoom.get(roomId), n);
    }

    /**
     * Lists the next {@code n} occurrences for an employee across all their recurring
     * bookings, sorted by occurrence start time then booking ID.
     *
     * @param employeeId employee to query
     * @param n          maximum number of occurrences to return
     * @return list of strings formatted as {@code "bookingId-startTime-endTime"}
     */
    public List<String> listBookingsForEmployee(int employeeId, int n) {
        return listN(byEmployee.get(employeeId), n);
    }

    /** Expands all occurrences of each booking in {@code bList}, sorts, and returns the first {@code n}. */
    private List<String> listN(List<RecurringBooking> bList, int n) {
        List<int[]> all = new ArrayList<>(); // [start, end, bookingIndex]
        for (RecurringBooking b : bList) {
            for (int k = 0; k < OCCURRENCES; k++) {
                all.add(new int[]{b.occurrenceStart(k), b.occurrenceEnd(k), System.identityHashCode(b)});
            }
        }
        // Need the booking id too — use a different structure
        List<String[]> entries = new ArrayList<>();
        for (RecurringBooking b : bList) {
            for (int k = 0; k < OCCURRENCES; k++) {
                entries.add(new String[]{
                    b.id,
                    String.valueOf(b.occurrenceStart(k)),
                    String.valueOf(b.occurrenceEnd(k))
                });
            }
        }
        entries.sort((a, b2) -> Integer.parseInt(a[1]) != Integer.parseInt(b2[1])
            ? Integer.compare(Integer.parseInt(a[1]), Integer.parseInt(b2[1]))
            : a[0].compareTo(b2[0]));
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, entries.size()); i++) {
            String[] e = entries.get(i);
            result.add(e[0] + "-" + e[1] + "-" + e[2]);
        }
        return result;
    }
}
