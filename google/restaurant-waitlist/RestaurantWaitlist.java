import java.util.*;

/**
 * Restaurant waitlist manager for a host desk — Google onsite LLD problem.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code queue}: {@code LinkedList<Party>} FIFO — O(1) enqueue/dequeue.</li>
 *   <li>{@code byName}: {@code HashMap<name, Party>} — O(1) lookup for position queries
 *       and duplicate detection.</li>
 * </ul>
 *
 * <p>Seating policy ({@link #seatNextFitting}): first-fit from the front of the queue —
 * parties behind an oversized group are skipped, matching realistic restaurant behaviour.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class RestaurantWaitlist {

    /**
     * An immutable snapshot of a waiting party.
     */
    static class Party {
        /** The party's name (used as unique key). */
        final String name;

        /** Number of people in the party. */
        final int    size;

        /** Epoch-ms when the party joined the waitlist (used for estimated wait display). */
        final long   arrivedAt;

        /**
         * @param name unique party name
         * @param size number of people
         */
        Party(String name, int size) {
            this.name      = name;
            this.size      = size;
            this.arrivedAt = System.currentTimeMillis();
        }

        @Override public String toString() {
            return String.format("%s(party of %d)", name, size);
        }
    }

    /** FIFO queue of waiting parties; head = first to arrive. */
    private final LinkedList<Party>    queue   = new LinkedList<>();

    /** Index for O(1) party lookup by name; also serves as duplicate guard. */
    private final Map<String, Party>   byName  = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Adds a party to the end of the waitlist.
     *
     * @param name unique party name; duplicates are rejected with a warning
     * @param size number of people (must be &gt; 0)
     * @return {@code true} if added; {@code false} if the party is already on the list
     * @throws IllegalArgumentException if size is not positive
     */
    public boolean addParty(String name, int size) {
        if (size <= 0) throw new IllegalArgumentException("Party size must be > 0");
        if (byName.containsKey(name)) {
            System.out.println("  [warn] Party '" + name + "' already on waitlist.");
            return false;
        }
        Party p = new Party(name, size);
        queue.addLast(p);
        byName.put(name, p);
        return true;
    }

    /**
     * Scans from the front and seats the first party whose size fits the available table.
     * Parties behind an oversized group are skipped (first-fit policy).
     *
     * @param tableCapacity the number of seats at the available table
     * @return the seated {@link Party}, or {@code null} if no waiting party fits
     */
    public Party seatNextFitting(int tableCapacity) {
        Iterator<Party> it = queue.iterator();
        while (it.hasNext()) {
            Party p = it.next();
            if (p.size <= tableCapacity) {
                it.remove();
                byName.remove(p.name);
                return p;
            }
        }
        return null; // no party fits current table
    }

    /**
     * Removes a party from the waitlist (voluntary cancellation).
     *
     * @param name the party to remove
     * @return {@code true} if found and removed; {@code false} if the name was not on the list
     */
    public boolean removeParty(String name) {
        Party p = byName.remove(name);
        if (p == null) return false;
        queue.remove(p); // O(n) — acceptable for interview; can use doubly-linked set for O(1)
        return true;
    }

    /**
     * Returns the 1-based position of the party in the waitlist.
     *
     * @param name the party to look up
     * @return 1-based position, or {@code -1} if the party is not in the queue
     */
    public int getPosition(String name) {
        int pos = 1;
        for (Party p : queue) {
            if (p.name.equals(name)) return pos;
            pos++;
        }
        return -1;
    }

    /**
     * Estimates how many minutes the party will wait, based on parties ahead of them.
     *
     * @param name               the party to estimate for
     * @param avgServiceMinutes  average number of minutes each party ahead takes
     * @return estimated wait in minutes, or {@code -1} if the party is not in the queue
     */
    public double estimatedWaitTime(String name, double avgServiceMinutes) {
        int pos = getPosition(name);
        if (pos == -1) return -1;
        return (pos - 1) * avgServiceMinutes; // parties before this one
    }

    /**
     * Returns a snapshot of all parties currently on the waitlist in arrival order.
     *
     * @return new list; modifying it does not affect the waitlist
     */
    public List<Party> getWaitlist() { return new ArrayList<>(queue); }

    /** Returns the number of parties currently waiting. */
    public int size()           { return queue.size(); }

    /** Returns {@code true} if there are no waiting parties. */
    public boolean isEmpty()    { return queue.isEmpty(); }

    /** Prints the waitlist in arrival order with 1-based positions. */
    public void printWaitlist() {
        if (queue.isEmpty()) { System.out.println("  (waitlist empty)"); return; }
        int pos = 1;
        for (Party p : queue)
            System.out.printf("  %d. %s%n", pos++, p);
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {

        RestaurantWaitlist wl = new RestaurantWaitlist();

        System.out.println("══════════════════════════════════════════");
        System.out.println(" Scenario: evening rush                   ");
        System.out.println("══════════════════════════════════════════");
        wl.addParty("Smith",   4);
        wl.addParty("Johnson", 2);
        wl.addParty("Lee",     6);
        wl.addParty("Garcia",  3);
        wl.addParty("Patel",   2);

        System.out.println("Waitlist on arrival:");
        wl.printWaitlist();

        System.out.println("\nPosition of 'Garcia': " + wl.getPosition("Garcia")); // 4
        System.out.printf("Estimated wait for Garcia (15 min avg): %.0f min%n",
            wl.estimatedWaitTime("Garcia", 15)); // 3 parties ahead × 15 = 45 min

        // Table for 4 opens up
        Party seated = wl.seatNextFitting(4);
        System.out.println("\nTable for 4 available → seated: " + seated); // Smith(4)
        wl.printWaitlist();

        // Table for 2 opens up — skips Lee(6) and Garcia(3), seats Johnson(2)
        seated = wl.seatNextFitting(2);
        System.out.println("\nTable for 2 available → seated: " + seated); // Johnson(2)
        wl.printWaitlist();

        // Lee(6) cancels — party left
        System.out.println("\nLee's party decides not to wait.");
        wl.removeParty("Lee");
        wl.printWaitlist();

        // Duplicate attempt
        wl.addParty("Garcia", 3); // should warn, not add

        // Seat everyone remaining (big table)
        System.out.println("\nLarge table for 10 opens:");
        while (!wl.isEmpty()) {
            seated = wl.seatNextFitting(10);
            System.out.println("  Seated: " + seated);
        }

        System.out.println("\nWaitlist after seating all:");
        wl.printWaitlist();

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" Edge cases                               ");
        System.out.println("══════════════════════════════════════════");
        RestaurantWaitlist wl2 = new RestaurantWaitlist();
        wl2.addParty("BigGroup", 8);

        // No table fits the only party
        Party result = wl2.seatNextFitting(4);
        System.out.println("Table for 4 → seated: " + result); // null

        // Remove only party
        wl2.removeParty("BigGroup");
        System.out.println("After removal, size: " + wl2.size()); // 0

        // Remove non-existent
        System.out.println("Remove non-existent: " + wl2.removeParty("Ghost")); // false
    }
}
