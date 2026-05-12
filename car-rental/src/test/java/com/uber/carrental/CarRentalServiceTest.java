package com.uber.carrental;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarRentalServiceTest {
    private CarRentalService svc;

    @BeforeEach
    void setUp() { svc = new CarRentalService(); }

    @Test
    void exampleA_withinWindowNoExtraKms() {
        svc.addCar("KA01AB1234", 1200, 100, 10);
        assertTrue(svc.bookCar("ORD-1", "KA01AB1234", "2025-08-28", "2025-08-30"));
        svc.startTrip("ORD-1", 5000);
        assertEquals(3600, svc.endTrip("ORD-1", 5250, "2025-08-29"));
    }

    @Test
    void exampleB_extendedWithExtraKms() {
        svc.addCar("DL09CD4321", 1500, 120, 8);
        assertTrue(svc.bookCar("ORD-2", "DL09CD4321", "2025-09-01", "2025-09-02"));
        svc.startTrip("ORD-2", 20000);
        assertEquals(6560, svc.endTrip("ORD-2", 20550, "2025-09-04"));
    }

    @Test
    void exampleC_overlappingBookingRejected() {
        svc.addCar("MH12EF9999", 1000, 80, 12);
        assertTrue(svc.bookCar("ORD-3", "MH12EF9999", "2025-08-10", "2025-08-12"));
        assertFalse(svc.bookCar("ORD-4", "MH12EF9999", "2025-08-12", "2025-08-15"));
    }

    @Test
    void bookCar_sameDayBooking() {
        svc.addCar("CAR1", 100, 50, 5);
        assertTrue(svc.bookCar("O1", "CAR1", "2025-06-01", "2025-06-01"));
        svc.startTrip("O1", 1000);
        assertEquals(100, svc.endTrip("O1", 1040, "2025-06-01")); // 1 day, 40 km, free=50 → 0 extra
    }

    @Test
    void bookCar_earlyReturnFreesCarSooner() {
        svc.addCar("CAR1", 100, 50, 5);
        assertTrue(svc.bookCar("O1", "CAR1", "2025-07-01", "2025-07-10"));
        svc.startTrip("O1", 1000);
        svc.endTrip("O1", 1100, "2025-07-05"); // returns early on Jul 5
        // Car should be available from Jul 6 (blocked until Jul 5 actual end)
        assertTrue(svc.bookCar("O2", "CAR1", "2025-07-06", "2025-07-08"));
    }

    @Test
    void bookCar_earlyReturnSameDayIsStillBlocked() {
        svc.addCar("CAR1", 100, 50, 5);
        assertTrue(svc.bookCar("O1", "CAR1", "2025-07-01", "2025-07-10"));
        svc.startTrip("O1", 1000);
        svc.endTrip("O1", 1100, "2025-07-05"); // ends Jul 5
        // Jul 5 itself still blocked
        assertFalse(svc.bookCar("O2", "CAR1", "2025-07-05", "2025-07-08"));
    }

    @Test
    void addCar_duplicateLicensePlateIgnored() {
        svc.addCar("CAR1", 100, 50, 5);
        svc.addCar("CAR1", 200, 100, 10); // should be ignored
        assertTrue(svc.bookCar("O1", "CAR1", "2025-01-01", "2025-01-01"));
        svc.startTrip("O1", 0);
        assertEquals(100, svc.endTrip("O1", 0, "2025-01-01")); // still uses original 100/day
    }

    @Test
    void endTrip_exactlyFreeKmsNoCost() {
        svc.addCar("CAR1", 1000, 100, 10);
        svc.bookCar("O1", "CAR1", "2025-03-01", "2025-03-03"); // 3 days
        svc.startTrip("O1", 0);
        // 3 days * 100 = 300 free kms
        assertEquals(3000, svc.endTrip("O1", 300, "2025-03-03")); // no extra kms
    }

    @Test
    void endTrip_lateReturnChargesExtraDays() {
        svc.addCar("CAR1", 1000, 100, 10);
        svc.bookCar("O1", "CAR1", "2025-03-01", "2025-03-02"); // 2 days
        svc.startTrip("O1", 0);
        // Returns on Mar 5 → effectiveEnd = Mar 5 → 5 days
        // 5 days * 1000 = 5000 day cost, 0 extra kms
        assertEquals(5000, svc.endTrip("O1", 0, "2025-03-05"));
    }
}
