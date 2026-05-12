package com.uber.movieticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MovieTicketServiceTest {
    private MovieTicketService svc;

    @BeforeEach void setUp() { svc = new MovieTicketService(); }

    @Test
    void exampleFromProblem() {
        svc.addCinema(0, 1, 4, 5, 10);
        svc.addShow(1, 4, 0, 1, 1710516108725L, 1710523308725L);
        svc.addShow(2, 11, 0, 3, 1710516108725L, 1710523308725L);
        assertEquals(List.of(), svc.listCinemas(0, 1));
        assertEquals(List.of(1), svc.listShows(4, 0));
        assertEquals(List.of(2), svc.listShows(11, 0));
        assertEquals(50, svc.getFreeSeatsCount(1));
        List<String> seats = svc.bookTicket("tkt-1", 1, 4);
        assertEquals(List.of("0-0","0-1","0-2","0-3"), seats);
        assertEquals(46, svc.getFreeSeatsCount(1));
        assertTrue(svc.cancelTicket("tkt-1"));
        assertEquals(50, svc.getFreeSeatsCount(1));
    }

    @Test
    void bookTicket_returnsEmptyIfNotEnoughSeats() {
        svc.addCinema(1, 1, 1, 1, 3); // 3 seats total
        svc.addShow(10, 1, 1, 1, 100L, 200L);
        svc.bookTicket("t1", 10, 3); // fills all 3
        assertEquals(List.of(), svc.bookTicket("t2", 10, 1)); // no seats left
    }

    @Test
    void cancelTicket_unknownReturnsFalse() {
        assertFalse(svc.cancelTicket("nonexistent"));
    }

    @Test
    void listCinemas_sortedAscending() {
        svc.addCinema(5, 1, 1, 2, 2);
        svc.addCinema(2, 1, 1, 2, 2);
        svc.addCinema(8, 1, 1, 2, 2);
        svc.addShow(1, 1, 5, 1, 100L, 200L);
        svc.addShow(2, 1, 2, 1, 100L, 200L);
        svc.addShow(3, 1, 8, 1, 100L, 200L);
        assertEquals(List.of(2, 5, 8), svc.listCinemas(1, 1));
    }

    @Test
    void listShows_sortedByStartTimeDesc() {
        svc.addCinema(0, 1, 2, 3, 3);
        svc.addShow(1, 1, 0, 1, 100L, 200L);
        svc.addShow(2, 1, 0, 2, 300L, 400L);
        svc.addShow(3, 1, 0, 1, 200L, 300L);
        List<Integer> shows = svc.listShows(1, 0);
        // showId 2 (start=300) first, then 3 (start=200), then 1 (start=100)
        assertEquals(List.of(2, 3, 1), shows);
    }

    @Test
    void getFreeSeatsCount_unknownShowReturnsZero() {
        assertEquals(0, svc.getFreeSeatsCount(999));
    }
}
