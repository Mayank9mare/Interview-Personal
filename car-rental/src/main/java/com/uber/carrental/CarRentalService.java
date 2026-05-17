package com.uber.carrental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * In-memory car rental service that manages a fleet of cars and customer bookings.
 * <p>
 * A car is blocked from a booking's {@code fromDate} until the later of its {@code tillDate}
 * (planned return) and the actual return date recorded at {@link #endTrip}. Billing charges
 * for the number of days used (minimum = booked duration) plus per-kilometre overage beyond
 * the free daily allowance. Not thread-safe.
 */
public class CarRentalService {

    /**
     * Immutable tariff information for a single car.
     */
    private static class Car {
        /** Daily rental charge. */
        final int costPerDay;
        /** Kilometres included in the daily rate. */
        final int freeKmsPerDay;
        /** Charge per kilometre beyond the free allowance. */
        final int costPerKm;

        /**
         * @param cpd  cost per day
         * @param fkpd free kilometres per day
         * @param cpk  cost per extra kilometre
         */
        Car(int cpd, int fkpd, int cpk) {
            costPerDay = cpd; freeKmsPerDay = fkpd; costPerKm = cpk;
        }
    }

    /**
     * Mutable state for a single customer booking, updated as the trip progresses.
     */
    private static class Booking {
        /** Licence plate of the booked car. */
        final String carPlate;
        /** Booking start date. */
        final LocalDate fromDate;
        /** Planned return date. */
        final LocalDate tillDate;
        /** Odometer reading recorded at trip start. */
        int startOdometer;
        /** Actual return date; {@code null} until {@link CarRentalService#endTrip} is called. */
        LocalDate actualEndDate;

        /**
         * @param plate licence plate of the car
         * @param from  booking start date
         * @param till  planned return date
         */
        Booking(String plate, LocalDate from, LocalDate till) {
            carPlate = plate; fromDate = from; tillDate = till;
        }

        /** Returns {@code true} once {@link #endTrip} has been called for this booking. */
        boolean isCompleted() { return actualEndDate != null; }

        /** Returns the date until which the car remains blocked (actual end or planned end). */
        LocalDate blockedUntil() {
            return isCompleted() ? actualEndDate : tillDate;
        }
    }

    /** Registry of cars keyed by licence plate. */
    private final Map<String, Car> cars = new HashMap<>();

    /** All bookings keyed by order ID. */
    private final Map<String, Booking> bookings = new HashMap<>();

    /**
     * Adds a car to the fleet. Ignored if the licence plate is already registered.
     *
     * @param licensePlate   unique identifier for the car
     * @param costPerDay     daily rental rate
     * @param freeKmsPerDay  kilometres included in the daily rate
     * @param costPerKm      charge per kilometre beyond the free allowance
     */
    public void addCar(String licensePlate, int costPerDay, int freeKmsPerDay, int costPerKm) {
        cars.putIfAbsent(licensePlate, new Car(costPerDay, freeKmsPerDay, costPerKm));
    }

    /**
     * Books a car for the given date range if the car exists and is not already blocked.
     *
     * @param orderId          unique identifier for this booking
     * @param carLicensePlate  licence plate of the car to book
     * @param fromDate         start date in ISO format (yyyy-MM-dd)
     * @param tillDate         planned return date in ISO format (yyyy-MM-dd)
     * @return {@code true} if the booking was created; {@code false} if the car is unknown or unavailable
     */
    public boolean bookCar(String orderId, String carLicensePlate, String fromDate, String tillDate) {
        if (!cars.containsKey(carLicensePlate)) return false;
        LocalDate from = LocalDate.parse(fromDate), till = LocalDate.parse(tillDate);
        for (Booking b : bookings.values()) {
            if (!b.carPlate.equals(carLicensePlate)) continue;
            LocalDate bEnd = b.blockedUntil();
            if (!from.isAfter(bEnd) && !b.fromDate.isAfter(till)) return false;
        }
        bookings.put(orderId, new Booking(carLicensePlate, from, till));
        return true;
    }

    /**
     * Records the odometer reading at the start of the trip.
     *
     * @param orderId         the booking to update
     * @param odometerReading current odometer value in kilometres
     */
    public void startTrip(String orderId, int odometerReading) {
        bookings.get(orderId).startOdometer = odometerReading;
    }

    /**
     * Closes the trip, records the actual return date, and computes the total charge.
     * <p>
     * Days billed = 1 + days between {@code fromDate} and {@code max(endDate, tillDate)}.
     * Extra kilometres = {@code max(0, tripKms - days * freeKmsPerDay)}.
     *
     * @param orderId              the booking to close
     * @param finalOdometerReading odometer value at the end of the trip
     * @param endDate              actual return date in ISO format (yyyy-MM-dd)
     * @return total charge (day rate × days + per-km rate × extra kilometres)
     */
    public int endTrip(String orderId, int finalOdometerReading, String endDate) {
        Booking b = bookings.get(orderId);
        Car car = cars.get(b.carPlate);
        LocalDate end = LocalDate.parse(endDate);
        b.actualEndDate = end;
        LocalDate effectiveEnd = end.isAfter(b.tillDate) ? end : b.tillDate;
        long days = 1 + ChronoUnit.DAYS.between(b.fromDate, effectiveEnd);
        int tripKms = finalOdometerReading - b.startOdometer;
        long freeAllowance = days * car.freeKmsPerDay;
        int extraKms = (int) Math.max(0, tripKms - freeAllowance);
        return (int)(days * car.costPerDay) + extraKms * car.costPerKm;
    }
}
