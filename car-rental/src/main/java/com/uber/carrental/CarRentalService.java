package com.uber.carrental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CarRentalService {

    private static class Car {
        final int costPerDay, freeKmsPerDay, costPerKm;
        Car(int cpd, int fkpd, int cpk) {
            costPerDay = cpd; freeKmsPerDay = fkpd; costPerKm = cpk;
        }
    }

    private static class Booking {
        final String carPlate;
        final LocalDate fromDate, tillDate;
        int startOdometer;
        LocalDate actualEndDate;

        Booking(String plate, LocalDate from, LocalDate till) {
            carPlate = plate; fromDate = from; tillDate = till;
        }

        boolean isCompleted() { return actualEndDate != null; }

        LocalDate blockedUntil() {
            return isCompleted() ? actualEndDate : tillDate;
        }
    }

    private final Map<String, Car> cars = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();

    public void addCar(String licensePlate, int costPerDay, int freeKmsPerDay, int costPerKm) {
        cars.putIfAbsent(licensePlate, new Car(costPerDay, freeKmsPerDay, costPerKm));
    }

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

    public void startTrip(String orderId, int odometerReading) {
        bookings.get(orderId).startOdometer = odometerReading;
    }

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
