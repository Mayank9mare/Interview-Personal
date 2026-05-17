package com.uber.hotel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hotel room booking service supporting availability search, booking, and cancellation.
 *
 * <p>Two date intervals overlap when {@code checkIn < existingCheckOut && checkOut > existingCheckIn}
 * (exclusive-end convention: checking out on date D frees the room for a new guest on D).
 * Cancelled bookings do not block availability.
 *
 * <p>Data structures: {@code HashMap<roomId, Room>} and {@code HashMap<bookingId, Booking>}
 * for O(1) lookups. Availability search is O(r × b) where r = rooms and b = bookings.
 *
 * <p>Not thread-safe.
 */
public class HotelBookingService {
    /** Category of a hotel room. */
    public enum RoomType { SINGLE, DOUBLE, SUITE }
    /** Lifecycle state of a booking. */
    public enum BookingStatus { CONFIRMED, CANCELLED }

    /**
     * Immutable description of a hotel room.
     */
    public static class Room {
        /** Unique room identifier. */
        public final String roomId;
        /** Category of the room. */
        public final RoomType type;
        /** Nightly rate in the system's base currency. */
        public final int pricePerNight;

        /**
         * @param roomId       unique room identifier
         * @param type         room category
         * @param pricePerNight nightly rate
         */
        public Room(String roomId, RoomType type, int pricePerNight) {
            this.roomId = roomId;
            this.type = type;
            this.pricePerNight = pricePerNight;
        }
    }

    /**
     * Mutable booking record; only {@link #status} changes after creation.
     */
    public static class Booking {
        /** UUID-based unique booking identifier. */
        public final String bookingId;
        /** Guest who made the reservation. */
        public final String guestId;
        /** Room that was reserved. */
        public final String roomId;
        /** Arrival date (inclusive). */
        public final LocalDate checkIn;
        /** Departure date (exclusive — room is free again on this day). */
        public final LocalDate checkOut;
        /** Current lifecycle status. */
        public BookingStatus status;

        private Booking(String bookingId, String guestId, String roomId, LocalDate checkIn, LocalDate checkOut) {
            this.bookingId = bookingId;
            this.guestId = guestId;
            this.roomId = roomId;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = BookingStatus.CONFIRMED;
        }
    }

    /** roomId → Room. */
    private final Map<String, Room> rooms = new HashMap<>();
    /** bookingId → Booking. */
    private final Map<String, Booking> bookings = new HashMap<>();

    /**
     * Registers a room in the system.
     *
     * @param roomId        unique room identifier
     * @param type          room category
     * @param pricePerNight nightly rate
     */
    public void addRoom(String roomId, RoomType type, int pricePerNight) {
        rooms.put(roomId, new Room(roomId, type, pricePerNight));
    }

    /**
     * Finds all rooms of the given type that are available for the requested stay.
     *
     * @param type     room category to search for
     * @param checkIn  desired arrival date
     * @param checkOut desired departure date (must be after checkIn)
     * @return rooms sorted by room ID ascending
     * @throws IllegalArgumentException if {@code checkIn} is not before {@code checkOut}
     */
    public List<Room> searchAvailable(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return rooms.values().stream()
                .filter(room -> room.type == type)
                .filter(room -> isAvailable(room.roomId, checkIn, checkOut))
                .sorted(Comparator.comparing(room -> room.roomId))
                .collect(Collectors.toList());
    }

    /**
     * Books the first available room of the requested type.
     *
     * @param guestId  guest making the reservation
     * @param type     desired room type
     * @param checkIn  arrival date
     * @param checkOut departure date
     * @return the confirmed booking
     * @throws IllegalStateException    if no room of the requested type is available
     * @throws IllegalArgumentException if the date range is invalid
     */
    public Booking book(String guestId, RoomType type, LocalDate checkIn, LocalDate checkOut) {
        Room room = searchAvailable(type, checkIn, checkOut).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no room available"));
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, guestId, room.roomId, checkIn, checkOut);
        bookings.put(bookingId, booking);
        return booking;
    }

    /**
     * Cancels a booking, making the room available again for the same dates.
     *
     * @param bookingId the booking to cancel
     * @throws IllegalArgumentException if the booking does not exist
     */
    public void cancel(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("unknown booking");
        booking.status = BookingStatus.CANCELLED;
    }

    /** Returns {@code true} if no confirmed booking for {@code roomId} overlaps the given range. */
    private boolean isAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        for (Booking booking : bookings.values()) {
            if (!booking.roomId.equals(roomId) || booking.status == BookingStatus.CANCELLED) continue;
            boolean overlaps = checkIn.isBefore(booking.checkOut) && checkOut.isAfter(booking.checkIn);
            if (overlaps) return false;
        }
        return true;
    }

    /** @throws IllegalArgumentException if {@code checkIn} is not strictly before {@code checkOut} */
    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) throw new IllegalArgumentException("invalid dates");
    }
}
