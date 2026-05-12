package com.uber.hotel;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class HotelBookingServiceTest {
    @Test
    void bookingRemovesRoomFromAvailabilityForOverlappingDates() {
        HotelBookingService service = new HotelBookingService();
        service.addRoom("101", HotelBookingService.RoomType.SINGLE, 100);

        service.book("guest", HotelBookingService.RoomType.SINGLE, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        assertTrue(service.searchAvailable(HotelBookingService.RoomType.SINGLE,
                LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 4)).isEmpty());
    }

    @Test
    void cancellingBookingMakesRoomAvailableAgain() {
        HotelBookingService service = new HotelBookingService();
        service.addRoom("101", HotelBookingService.RoomType.SINGLE, 100);
        HotelBookingService.Booking booking = service.book("guest", HotelBookingService.RoomType.SINGLE,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 3));

        service.cancel(booking.bookingId);

        assertEquals(1, service.searchAvailable(HotelBookingService.RoomType.SINGLE,
                LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 4)).size());
    }
}
