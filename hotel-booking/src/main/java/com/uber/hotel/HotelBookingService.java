package com.uber.hotel;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HotelBookingService {
    public enum RoomType { SINGLE, DOUBLE, SUITE }
    public enum BookingStatus { CONFIRMED, CANCELLED }

    public static class Room {
        public final String roomId;
        public final RoomType type;
        public final int pricePerNight;

        public Room(String roomId, RoomType type, int pricePerNight) {
            this.roomId = roomId;
            this.type = type;
            this.pricePerNight = pricePerNight;
        }
    }

    public static class Booking {
        public final String bookingId;
        public final String guestId;
        public final String roomId;
        public final LocalDate checkIn;
        public final LocalDate checkOut;
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

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();

    public void addRoom(String roomId, RoomType type, int pricePerNight) {
        rooms.put(roomId, new Room(roomId, type, pricePerNight));
    }

    public List<Room> searchAvailable(RoomType type, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        return rooms.values().stream()
                .filter(room -> room.type == type)
                .filter(room -> isAvailable(room.roomId, checkIn, checkOut))
                .sorted(Comparator.comparing(room -> room.roomId))
                .collect(Collectors.toList());
    }

    public Booking book(String guestId, RoomType type, LocalDate checkIn, LocalDate checkOut) {
        Room room = searchAvailable(type, checkIn, checkOut).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no room available"));
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, guestId, room.roomId, checkIn, checkOut);
        bookings.put(bookingId, booking);
        return booking;
    }

    public void cancel(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("unknown booking");
        booking.status = BookingStatus.CANCELLED;
    }

    private boolean isAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        for (Booking booking : bookings.values()) {
            if (!booking.roomId.equals(roomId) || booking.status == BookingStatus.CANCELLED) continue;
            boolean overlaps = checkIn.isBefore(booking.checkOut) && checkOut.isAfter(booking.checkIn);
            if (overlaps) return false;
        }
        return true;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut)) throw new IllegalArgumentException("invalid dates");
    }
}
