package com.slice.runway;

import com.slice.runway.exception.RunwayException;
import com.slice.runway.model.BookingType;
import com.slice.runway.model.RunwayBooking;
import com.slice.runway.service.RunwayBookingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class AirportApp {

    private static final RunwayBookingService svc = new RunwayBookingService();

    public static void main(String[] args) {
        setup();

        LocalDateTime base = LocalDateTime.of(2026, 5, 20, 6, 0);

        // ── Happy-path bookings ───────────────────────────────────────────────
        section("Book RW1: AI101 LANDING  06:00–06:20");
        RunwayBooking b1 = svc.bookRunway("RW1", "AI101", BookingType.LANDING,
                base, base.plusMinutes(20));
        System.out.println(b1);

        // Gap < MIN_GAP (5 min): 06:20 end + 5 min gap = 06:25 buffer → 06:22 start overlaps
        section("Book RW1: AI202 TAKEOFF  06:22–06:35 (within gap — should throw)");
        tryBook("RW1", "AI202", BookingType.TAKEOFF, base.plusMinutes(22), base.plusMinutes(35));

        // Exactly after gap: 06:20 + 5 = 06:25 → 06:25 start is fine
        section("Book RW1: AI202 TAKEOFF  06:25–06:40 (after gap — should succeed)");
        RunwayBooking b2 = svc.bookRunway("RW1", "AI202", BookingType.TAKEOFF,
                base.plusMinutes(25), base.plusMinutes(40));
        System.out.println(b2);

        // Different runway — no conflict
        section("Book RW2: AI303 LANDING  06:00–06:20 (different runway — should succeed)");
        RunwayBooking b3 = svc.bookRunway("RW2", "AI303", BookingType.LANDING,
                base, base.plusMinutes(20));
        System.out.println(b3);

        // ── Schedule view ─────────────────────────────────────────────────────
        section("RW1 schedule for 2026-05-20");
        LocalDateTime dayStart = LocalDateTime.of(2026, 5, 20, 0, 0);
        LocalDateTime dayEnd   = LocalDateTime.of(2026, 5, 20, 23, 59);
        svc.getRunwaySchedule("RW1", dayStart, dayEnd).forEach(System.out::println);

        // ── Cancellation ──────────────────────────────────────────────────────
        section("Cancel booking " + b2.getBookingId());
        svc.cancelBooking(b2.getBookingId());
        System.out.println("Cancelled: " + b2);

        section("RW1 schedule after cancel");
        svc.getRunwaySchedule("RW1", dayStart, dayEnd).forEach(System.out::println);

        // ── Available slots ───────────────────────────────────────────────────
        section("Available slots on RW1, window 06:00–08:00, min 15 min");
        List<LocalDateTime[]> slots = svc.getAvailableSlots(
                "RW1", base, base.plusHours(2), 15);
        slots.forEach(s -> System.out.println("  " + s[0] + " → " + s[1]));

        // ── Flight bookings ───────────────────────────────────────────────────
        section("All bookings for AI101");
        svc.getFlightBookings("AI101").forEach(System.out::println);

        // ── Concurrency test: two threads race the same slot ──────────────────
        section("Concurrent race for RW2 07:00–07:20");
        concurrencyTest(base.plusHours(1), base.plusHours(1).plusMinutes(20));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void setup() {
        svc.addRunway("RW1", "Runway 1 (North-South)");
        svc.addRunway("RW2", "Runway 2 (East-West)");
        svc.addFlight("AI101", "Air India");
        svc.addFlight("AI202", "IndiGo");
        svc.addFlight("AI303", "SpiceJet");
        svc.addFlight("FL-A",  "AlphaAir");
        svc.addFlight("FL-B",  "BetaAir");
    }

    private static void tryBook(String runwayId, String flightId,
                                 BookingType type,
                                 LocalDateTime start, LocalDateTime end) {
        try {
            System.out.println(svc.bookRunway(runwayId, flightId, type, start, end));
        } catch (RunwayException e) {
            System.out.println("  [EXPECTED] " + e.getMessage());
        }
    }

    private static void section(String title) {
        System.out.println("\n=== " + title + " ===");
    }

    private static void concurrencyTest(LocalDateTime start, LocalDateTime end) {
        int[] wins = {0};
        Thread ta = new Thread(() -> {
            try {
                svc.bookRunway("RW2", "FL-A", BookingType.LANDING, start, end);
                synchronized (wins) { wins[0]++; }
                System.out.println("  FL-A got the slot");
            } catch (RunwayException e) {
                System.out.println("  FL-A failed: " + e.getMessage());
            }
        });
        Thread tb = new Thread(() -> {
            try {
                svc.bookRunway("RW2", "FL-B", BookingType.TAKEOFF, start, end);
                synchronized (wins) { wins[0]++; }
                System.out.println("  FL-B got the slot");
            } catch (RunwayException e) {
                System.out.println("  FL-B failed: " + e.getMessage());
            }
        });
        ta.start(); tb.start();
        try { ta.join(); tb.join(); } catch (InterruptedException ignored) {}
        System.out.println("  Winners: " + wins[0] + " (expected 1)");
    }
}
