// Companies: Salesforce, Microsoft, Uber
package com.uber.movieticket;

import java.util.*;

/**
 * Movie-ticket booking service supporting multiple cinemas, screens, shows, and seat selection.
 *
 * <p>Seat availability is stored as a {@code boolean[][][]} per cinema
 * ({@code screens[screenIdx][row][col] == true} means the seat is free).
 * Booking attempts first try to find consecutive seats in a single row;
 * if that fails, it falls back to picking the required number of individual seats
 * in scan order.
 *
 * <p>Not thread-safe.
 */
public class MovieTicketService {
    /** Runtime state for a single cinema (multiple screens). */
    private static class Cinema {
        final int cityId;
        /** {@code screens[screenIdx][row][col] == true} when the seat is unoccupied. */
        final boolean[][][] screens;
        Cinema(int cityId, int screenCount, int rows, int cols) {
            this.cityId = cityId;
            screens = new boolean[screenCount][rows][cols];
            for (boolean[][] s : screens) for (boolean[] r : s) Arrays.fill(r, true);
        }
    }

    /** A single scheduled showing of a movie on a specific screen. */
    private static class Show {
        final int movieId, cinemaId, screenIdx;
        final long startTime;
        final int rows, cols;
        Show(int movieId, int cinemaId, int screenIdx, long start, int rows, int cols) {
            this.movieId = movieId; this.cinemaId = cinemaId;
            this.screenIdx = screenIdx; this.startTime = start;
            this.rows = rows; this.cols = cols;
        }
    }

    /** A issued ticket referencing a show and the seats that were reserved. */
    private static class Ticket {
        final int showId;
        /** Reserved seats as [row, col] pairs. */
        final List<int[]> seats;
        boolean cancelled = false;
        Ticket(int showId, List<int[]> seats) { this.showId = showId; this.seats = seats; }
    }

    /** Cinema ID → Cinema. */
    private final Map<Integer, Cinema> cinemas = new HashMap<>();
    /** Show ID → Show. */
    private final Map<Integer, Show> shows = new HashMap<>();
    /** Ticket ID → Ticket. */
    private final Map<String, Ticket> tickets = new HashMap<>();

    /**
     * Registers a cinema.
     *
     * @param cinemaId     unique cinema identifier
     * @param cityId       city the cinema belongs to
     * @param screenCount  number of screens
     * @param screenRow    rows per screen
     * @param screenColumn columns (seats per row) per screen
     */
    public void addCinema(int cinemaId, int cityId, int screenCount, int screenRow, int screenColumn) {
        cinemas.put(cinemaId, new Cinema(cityId, screenCount, screenRow, screenColumn));
    }

    /**
     * Schedules a movie show on a screen.
     *
     * @param showId      unique show identifier
     * @param movieId     the movie being shown
     * @param cinemaId    cinema hosting the show
     * @param screenIndex 1-based screen number within the cinema
     * @param startTime   epoch-millis start time
     * @param endTime     epoch-millis end time (stored for reference; not used in conflict checks)
     */
    public void addShow(int showId, int movieId, int cinemaId, int screenIndex, long startTime, long endTime) {
        Cinema c = cinemas.get(cinemaId);
        int sIdx = screenIndex - 1; // convert to 0-based
        shows.put(showId, new Show(movieId, cinemaId, sIdx, startTime, c.screens[sIdx].length, c.screens[sIdx][0].length));
    }

    /**
     * Books consecutive seats (preferred) or individual seats for a show.
     *
     * @param ticketId     unique ticket identifier
     * @param showId       show to book for
     * @param ticketsCount number of seats to reserve
     * @return list of seat identifiers formatted as {@code "row-col"};
     *         empty if not enough seats are available or the show does not exist
     */
    public List<String> bookTicket(String ticketId, int showId, int ticketsCount) {
        Show show = shows.get(showId);
        if (show == null) return Collections.emptyList();
        Cinema cinema = cinemas.get(show.cinemaId);
        boolean[][] screen = cinema.screens[show.screenIdx];
        int rows = screen.length, cols = screen[0].length;

        // Try consecutive seats in each row
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c <= cols - ticketsCount; c++) {
                boolean ok = true;
                for (int k = 0; k < ticketsCount; k++) if (!screen[r][c + k]) { ok = false; break; }
                if (ok) {
                    List<int[]> seats = new ArrayList<>();
                    for (int k = 0; k < ticketsCount; k++) { screen[r][c + k] = false; seats.add(new int[]{r, c + k}); }
                    tickets.put(ticketId, new Ticket(showId, seats));
                    return formatSeats(seats);
                }
            }
        }

        // Fall back: pick individual seats sorted by (row, col)
        List<int[]> available = new ArrayList<>();
        for (int r = 0; r < rows; r++) for (int c = 0; c < cols; c++) if (screen[r][c]) available.add(new int[]{r, c});
        if (available.size() < ticketsCount) return Collections.emptyList();
        List<int[]> seats = available.subList(0, ticketsCount);
        for (int[] s : seats) screen[s[0]][s[1]] = false;
        tickets.put(ticketId, new Ticket(showId, new ArrayList<>(seats)));
        return formatSeats(seats);
    }

    /** Converts seat coordinates to {@code "row-col"} strings. */
    private List<String> formatSeats(List<int[]> seats) {
        List<String> result = new ArrayList<>();
        for (int[] s : seats) result.add(s[0] + "-" + s[1]);
        return result;
    }

    /**
     * Cancels a ticket and releases its seats.
     *
     * @param ticketId the ticket to cancel
     * @return {@code true} if the ticket existed and was not already cancelled
     */
    public boolean cancelTicket(String ticketId) {
        Ticket t = tickets.get(ticketId);
        if (t == null || t.cancelled) return false;
        t.cancelled = true;
        Show show = shows.get(t.showId);
        boolean[][] screen = cinemas.get(show.cinemaId).screens[show.screenIdx];
        for (int[] s : t.seats) screen[s[0]][s[1]] = true;
        return true;
    }

    /**
     * @param showId the show to check
     * @return number of currently available seats, or 0 if the show does not exist
     */
    public int getFreeSeatsCount(int showId) {
        Show show = shows.get(showId);
        if (show == null) return 0;
        boolean[][] screen = cinemas.get(show.cinemaId).screens[show.screenIdx];
        int count = 0;
        for (boolean[] row : screen) for (boolean seat : row) if (seat) count++;
        return count;
    }

    /**
     * Finds all cinemas in a city that have at least one show for the given movie.
     *
     * @param movieId the movie to search for
     * @param cityId  the city to search in
     * @return sorted list of cinema IDs
     */
    public List<Integer> listCinemas(int movieId, int cityId) {
        Set<Integer> result = new TreeSet<>();
        for (Show show : shows.values()) {
            if (show.movieId == movieId && cinemas.get(show.cinemaId).cityId == cityId)
                result.add(show.cinemaId);
        }
        return new ArrayList<>(result);
    }

    /**
     * Lists shows for a movie at a specific cinema, sorted by start time descending
     * then show ID ascending.
     *
     * @param movieId  the movie to filter by
     * @param cinemaId the cinema to filter by
     * @return ordered list of show IDs
     */
    public List<Integer> listShows(int movieId, int cinemaId) {
        List<Show> matching = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, Show> e : shows.entrySet()) {
            if (e.getValue().movieId == movieId && e.getValue().cinemaId == cinemaId)
                matching.add(e.getValue());
        }
        // Sort by startTime desc, then showId asc
        List<Map.Entry<Integer, Show>> entries = new ArrayList<>();
        for (Map.Entry<Integer, Show> e : shows.entrySet())
            if (e.getValue().movieId == movieId && e.getValue().cinemaId == cinemaId)
                entries.add(e);
        entries.sort((a, b) -> {
            int cmp = Long.compare(b.getValue().startTime, a.getValue().startTime);
            return cmp != 0 ? cmp : Integer.compare(a.getKey(), b.getKey());
        });
        for (Map.Entry<Integer, Show> e : entries) ids.add(e.getKey());
        return ids;
    }
}
