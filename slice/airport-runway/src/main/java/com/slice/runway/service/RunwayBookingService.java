package com.slice.runway.service;

import com.slice.runway.exception.RunwayException;
import com.slice.runway.model.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class RunwayBookingService {

    /** Minimum gap in minutes between any two bookings on the same runway. */
    private static final int MIN_GAP_MINUTES = 5;

    private final Map<String, Runway>  runways  = new ConcurrentHashMap<>();
    private final Map<String, Flight>  flights  = new ConcurrentHashMap<>();
    private final List<RunwayBooking>  bookings = new CopyOnWriteArrayList<>();

    // Per-runway lock: concurrent requests for different runways do not block each other
    private final ConcurrentHashMap<String, ReentrantLock> runwayLocks = new ConcurrentHashMap<>();

    // ── Setup ─────────────────────────────────────────────────────────────────

    public void addRunway(String runwayId, String name) {
        runways.put(runwayId, new Runway(runwayId, name));
        runwayLocks.putIfAbsent(runwayId, new ReentrantLock());
    }

    public void addFlight(String flightId, String airline) {
        flights.put(flightId, new Flight(flightId, airline));
    }

    // ── Book ──────────────────────────────────────────────────────────────────

    /**
     * Books a runway for a flight in a given window.
     *
     * A booking is rejected if any CONFIRMED booking on the same runway overlaps
     * the requested window extended by MIN_GAP_MINUTES on each side — this models
     * the safety separation gap required between operations.
     *
     * Thread safety: per-runway lock ensures no two threads can double-book the
     * same runway simultaneously. Different runways are booked independently.
     */
    public RunwayBooking bookRunway(String runwayId, String flightId,
                                    BookingType type,
                                    LocalDateTime start, LocalDateTime end) {
        validateRunway(runwayId);
        validateFlight(flightId);
        if (!end.isAfter(start))
            throw new RunwayException("end time must be after start time", runwayId);

        ReentrantLock lock = runwayLocks.computeIfAbsent(runwayId, k -> new ReentrantLock());
        lock.lock();
        try {
            // Effective window: expand by MIN_GAP_MINUTES to enforce safety gap
            LocalDateTime effectiveStart = start.minusMinutes(MIN_GAP_MINUTES);
            LocalDateTime effectiveEnd   = end.plusMinutes(MIN_GAP_MINUTES);

            boolean conflict = bookings.stream()
                    .filter(b -> b.getRunwayId().equals(runwayId)
                            && b.getStatus() == BookingStatus.CONFIRMED)
                    .anyMatch(b -> overlaps(b.getStartTime(), b.getEndTime(),
                            effectiveStart, effectiveEnd));

            if (conflict)
                throw new RunwayException(
                        "runway is occupied (including " + MIN_GAP_MINUTES + "-min gap) during " + start + "–" + end,
                        runwayId);

            RunwayBooking booking = new RunwayBooking(runwayId, flightId, type, start, end);
            bookings.add(booking);
            return booking;
        } finally {
            lock.unlock();
        }
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public void cancelBooking(String bookingId) {
        RunwayBooking booking = bookings.stream()
                .filter(b -> b.getBookingId().equals(bookingId))
                .findFirst()
                .orElseThrow(() -> new RunwayException("booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new RunwayException("booking is already cancelled: " + bookingId);

        ReentrantLock lock = runwayLocks.computeIfAbsent(booking.getRunwayId(), k -> new ReentrantLock());
        lock.lock();
        try {
            booking.cancel();
        } finally {
            lock.unlock();
        }
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** All CONFIRMED bookings for a runway on a given day. */
    public List<RunwayBooking> getRunwaySchedule(String runwayId, LocalDateTime dayStart, LocalDateTime dayEnd) {
        validateRunway(runwayId);
        return bookings.stream()
                .filter(b -> b.getRunwayId().equals(runwayId)
                        && b.getStatus() == BookingStatus.CONFIRMED
                        && !b.getEndTime().isBefore(dayStart)
                        && !b.getStartTime().isAfter(dayEnd))
                .sorted(Comparator.comparing(RunwayBooking::getStartTime))
                .collect(Collectors.toList());
    }

    /** All bookings (any status) for a given flight. */
    public List<RunwayBooking> getFlightBookings(String flightId) {
        validateFlight(flightId);
        return bookings.stream()
                .filter(b -> b.getFlightId().equals(flightId))
                .collect(Collectors.toList());
    }

    /**
     * Returns available (free) time slots of minimum length {@code minDurationMinutes}
     * on a given runway within the search window.
     *
     * Algorithm: treat confirmed bookings (+ gap) as busy intervals, then collect
     * gaps between them.
     */
    public List<LocalDateTime[]> getAvailableSlots(String runwayId,
                                                    LocalDateTime windowStart,
                                                    LocalDateTime windowEnd,
                                                    int minDurationMinutes) {
        validateRunway(runwayId);

        // Collect busy intervals (booking + safety gap on each side)
        List<LocalDateTime[]> busy = bookings.stream()
                .filter(b -> b.getRunwayId().equals(runwayId)
                        && b.getStatus() == BookingStatus.CONFIRMED
                        && !b.getEndTime().isBefore(windowStart)
                        && !b.getStartTime().isAfter(windowEnd))
                .map(b -> new LocalDateTime[]{
                        b.getStartTime().minusMinutes(MIN_GAP_MINUTES),
                        b.getEndTime().plusMinutes(MIN_GAP_MINUTES)})
                .sorted(Comparator.comparing(iv -> iv[0]))
                .collect(Collectors.toList());

        // Merge overlapping busy intervals, then collect gaps
        List<LocalDateTime[]> merged = new ArrayList<>();
        for (LocalDateTime[] iv : busy) {
            if (merged.isEmpty() || iv[0].isAfter(merged.get(merged.size() - 1)[1])) {
                merged.add(new LocalDateTime[]{iv[0], iv[1]});
            } else {
                LocalDateTime[] last = merged.get(merged.size() - 1);
                if (iv[1].isAfter(last[1])) last[1] = iv[1];
            }
        }

        List<LocalDateTime[]> slots = new ArrayList<>();
        LocalDateTime cursor = windowStart;
        for (LocalDateTime[] busy2 : merged) {
            LocalDateTime freeEnd = busy2[0];
            if (freeEnd.isAfter(cursor)
                    && java.time.Duration.between(cursor, freeEnd).toMinutes() >= minDurationMinutes) {
                slots.add(new LocalDateTime[]{cursor, freeEnd});
            }
            if (busy2[1].isAfter(cursor)) cursor = busy2[1];
        }
        if (cursor.isBefore(windowEnd)
                && java.time.Duration.between(cursor, windowEnd).toMinutes() >= minDurationMinutes) {
            slots.add(new LocalDateTime[]{cursor, windowEnd});
        }
        return slots;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Two intervals [s1,e1) and [s2,e2) overlap iff s1 < e2 && s2 < e1. */
    private boolean overlaps(LocalDateTime s1, LocalDateTime e1,
                              LocalDateTime s2, LocalDateTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    private void validateRunway(String runwayId) {
        if (!runways.containsKey(runwayId))
            throw new RunwayException("runway not found", runwayId);
    }

    private void validateFlight(String flightId) {
        if (!flights.containsKey(flightId))
            throw new RunwayException("flight not found: " + flightId);
    }
}
