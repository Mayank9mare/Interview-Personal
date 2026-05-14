import java.util.*;

public class RestaurantWaitlist {

    // ═══════════════════════════════════════════════════════════════════════════
    // Restaurant Waitlist Manager — Google onsite LLD problem
    //
    // A host desk manages a queue of waiting parties and seats them as tables
    // become available.
    //
    // API:
    //   addParty(name, size)            — add party to end of waitlist
    //   seatNextFitting(tableCapacity)  — seat first party whose size ≤ capacity
    //                                     returns party name, or null if none fits
    //   removeParty(name)               — party left (cancellation)
    //   getWaitlist()                   — all parties in order (oldest first)
    //   getPosition(name)               — 1-based position in line, or -1 if absent
    //   estimatedWaitTime(name, avgServiceMin) — parties ahead × avgServiceMin
    //   getStats()                      — queue depth, total size waiting
    //
    // Design:
    //   • LinkedList<Party> as the FIFO queue (O(1) add-to-tail, O(1) remove head).
    //   • HashMap<name, Party> for O(1) position look-up and cancellation.
    //   • seatNextFitting: linear scan from front — first-fit policy.
    //     Parties behind a party that is too large for the current table are
    //     skipped, which is realistic (host checks next smaller party).
    //   • Duplicate party names are rejected.
    //
    // Complexity: addParty O(1), seatNextFitting O(n), removeParty O(n),
    //             getPosition O(n), getWaitlist O(n).
    // ═══════════════════════════════════════════════════════════════════════════

    static class Party {
        final String name;
        final int    size;
        final long   arrivedAt; // epoch ms, for estimated wait display

        Party(String name, int size) {
            this.name      = name;
            this.size      = size;
            this.arrivedAt = System.currentTimeMillis();
        }

        @Override public String toString() {
            return String.format("%s(party of %d)", name, size);
        }
    }

    private final LinkedList<Party>    queue   = new LinkedList<>();
    private final Map<String, Party>   byName  = new HashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

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

    // Seat the first party from the front whose size fits the table.
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

    // Cancel — party left the waitlist voluntarily.
    public boolean removeParty(String name) {
        Party p = byName.remove(name);
        if (p == null) return false;
        queue.remove(p); // O(n) — acceptable for interview; can use doubly-linked set for O(1)
        return true;
    }

    // 1-based position, or -1 if not in line.
    public int getPosition(String name) {
        int pos = 1;
        for (Party p : queue) {
            if (p.name.equals(name)) return pos;
            pos++;
        }
        return -1;
    }

    // Estimated wait = number of parties ahead × average service time per party.
    public double estimatedWaitTime(String name, double avgServiceMinutes) {
        int pos = getPosition(name);
        if (pos == -1) return -1;
        return (pos - 1) * avgServiceMinutes; // parties before this one
    }

    public List<Party> getWaitlist() { return new ArrayList<>(queue); }

    public int size()           { return queue.size(); }
    public boolean isEmpty()    { return queue.isEmpty(); }

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
