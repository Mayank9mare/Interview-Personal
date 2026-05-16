// Companies: Amazon
// Amazon Locker System — assign packages to size-matched lockers, release with a code.

import java.util.*;

/**
 * Models Amazon's parcel locker network at convenience-store pickup points.
 *
 * <p>Each locker has a size (Small, Medium, Large). A package is assigned the
 * <em>smallest available locker that fits its size</em> to maximise locker utilisation.
 * The system generates a one-time access code; the recipient opens the locker with
 * the code and the package is removed.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code available}: {@code EnumMap<LockerSize, Queue<Locker>>} — O(1) assignment
 *       of the smallest fitting locker.</li>
 *   <li>{@code codeToLocker}: {@code HashMap<code, Locker>} — O(1) open-locker lookup.</li>
 *   <li>{@code packageToLocker}: {@code HashMap<packageId, Locker>} — O(1) return/cancel.</li>
 * </ul>
 *
 * <p>Core invariant: a locker is in exactly one state — available (in an available queue)
 * or assigned (in both lookup maps). It is never in both states simultaneously.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class LockerSystem {

    // ── Types ─────────────────────────────────────────────────────────────

    enum LockerSize { SMALL, MEDIUM, LARGE }

    /** Represents a physical locker unit. */
    static class Locker {
        final int        lockerId;
        final LockerSize size;
        String           packageId;    // null when empty
        String           accessCode;   // null when empty
        int              assignedDay;  // day of assignment for expiry tracking

        Locker(int lockerId, LockerSize size) {
            this.lockerId = lockerId;
            this.size     = size;
        }

        boolean isOccupied() { return packageId != null; }
    }

    // ── Fields ────────────────────────────────────────────────────────────

    /** Per-size queues of free lockers for O(1) assignment. */
    private final Map<LockerSize, Queue<Locker>> available = new EnumMap<>(LockerSize.class);

    /**
     * Maps access code → locker. Used when a recipient opens the locker.
     * Entry removed when the locker is opened or the package is reclaimed.
     */
    private final Map<String, Locker> codeToLocker = new HashMap<>();

    /**
     * Maps packageId → locker for O(1) cancellation and expiry reclaim.
     * Entry removed when the locker is freed.
     */
    private final Map<String, Locker> packageToLocker = new HashMap<>();

    /** Number of days a package may remain before the locker is reclaimed. */
    private final int maxHoldDays;

    private int codeCounter = 0;

    // ── Constructor ───────────────────────────────────────────────────────

    /**
     * Constructs a locker station.
     *
     * @param small       number of small lockers
     * @param medium      number of medium lockers
     * @param large       number of large lockers
     * @param maxHoldDays packages not collected within this many days are reclaimed
     */
    public LockerSystem(int small, int medium, int large, int maxHoldDays) {
        this.maxHoldDays = maxHoldDays;
        for (LockerSize s : LockerSize.values())
            available.put(s, new LinkedList<>());
        int id = 1;
        for (int i = 0; i < small;  i++) available.get(LockerSize.SMALL).add(new Locker(id++, LockerSize.SMALL));
        for (int i = 0; i < medium; i++) available.get(LockerSize.MEDIUM).add(new Locker(id++, LockerSize.MEDIUM));
        for (int i = 0; i < large;  i++) available.get(LockerSize.LARGE).add(new Locker(id++, LockerSize.LARGE));
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Assigns the smallest available locker that fits {@code packageSize} to the package.
     *
     * <p>Returns a one-time access code the recipient uses to open the locker.
     * If no suitable locker is free, returns {@code null}.
     *
     * @param packageId   unique identifier for the package
     * @param packageSize required locker size (package fits in same-size or larger locker)
     * @param currentDay  today's day number (for expiry tracking)
     * @return access code string, or {@code null} if no locker is available
     * @throws IllegalArgumentException if packageId is already assigned to a locker
     */
    public String assignLocker(String packageId, LockerSize packageSize, int currentDay) {
        if (packageToLocker.containsKey(packageId))
            throw new IllegalArgumentException("Package already assigned: " + packageId);

        for (LockerSize size : fittingSizes(packageSize)) {
            Queue<Locker> q = available.get(size);
            if (!q.isEmpty()) {
                Locker locker = q.poll();
                String code = String.format("CODE-%04d", ++codeCounter);
                locker.packageId   = packageId;
                locker.accessCode  = code;
                locker.assignedDay = currentDay;
                codeToLocker.put(code, locker);
                packageToLocker.put(packageId, locker);
                return code;
            }
        }
        return null;  // no suitable locker available
    }

    /**
     * Opens the locker identified by {@code code} and returns the package ID.
     *
     * <p>Frees the locker for future assignments after a successful open.
     *
     * @param code the access code provided to the recipient
     * @return the packageId that was inside, or {@code null} if the code is invalid
     */
    public String openLocker(String code) {
        Locker locker = codeToLocker.remove(code);
        if (locker == null) return null;  // invalid or already-used code
        String packageId = locker.packageId;
        packageToLocker.remove(packageId);
        freeLocker(locker);
        return packageId;
    }

    /**
     * Releases all lockers whose packages have not been collected within {@code maxHoldDays}.
     * Expired packages are discarded (real systems would return them to the sender).
     *
     * @param currentDay today's day number
     * @return number of lockers reclaimed
     */
    public int releaseExpired(int currentDay) {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Locker> e : packageToLocker.entrySet()) {
            if (currentDay - e.getValue().assignedDay >= maxHoldDays)
                expired.add(e.getKey());
        }
        for (String pid : expired) {
            Locker locker = packageToLocker.remove(pid);
            codeToLocker.remove(locker.accessCode);
            freeLocker(locker);
            System.out.println("Reclaimed locker " + locker.lockerId + " (package " + pid + " expired)");
        }
        return expired.size();
    }

    /**
     * Returns the count of available lockers of the given size.
     *
     * @param size locker size to query
     * @return number of free lockers of that size
     */
    public int getAvailableCount(LockerSize size) { return available.get(size).size(); }

    /** Prints availability for all sizes and total occupied count. */
    public void printStatus() {
        System.out.println("=== Locker Station Status ===");
        for (LockerSize s : LockerSize.values())
            System.out.printf("  %-6s available: %d%n", s, getAvailableCount(s));
        System.out.println("  Occupied: " + packageToLocker.size());
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    /** Clears a locker's fields and returns it to the available queue. */
    private void freeLocker(Locker locker) {
        locker.packageId   = null;
        locker.accessCode  = null;
        locker.assignedDay = 0;
        available.get(locker.size).add(locker);
    }

    /**
     * Returns locker sizes that can hold a package of the given size, smallest first.
     * A SMALL package can fit in SMALL, MEDIUM, or LARGE; LARGE fits LARGE only.
     */
    private static List<LockerSize> fittingSizes(LockerSize packageSize) {
        switch (packageSize) {
            case SMALL:  return Arrays.asList(LockerSize.SMALL, LockerSize.MEDIUM, LockerSize.LARGE);
            case MEDIUM: return Arrays.asList(LockerSize.MEDIUM, LockerSize.LARGE);
            case LARGE:  return Arrays.asList(LockerSize.LARGE);
            default:     return Collections.emptyList();
        }
    }

    // ── Main ──────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // 3 small, 2 medium, 2 large lockers; packages expire after 3 days
        LockerSystem ls = new LockerSystem(3, 2, 2, 3);

        ls.printStatus();
        // Expected: Small: 3, Medium: 2, Large: 2

        System.out.println();

        // Assign a small package — should get a small locker
        String code1 = ls.assignLocker("PKG001", LockerSize.SMALL, 1);
        System.out.println("PKG001 (SMALL) -> code: " + code1);
        // Expected: CODE-0001

        // Assign a large package — should get a large locker
        String code2 = ls.assignLocker("PKG002", LockerSize.LARGE, 1);
        System.out.println("PKG002 (LARGE) -> code: " + code2);
        // Expected: CODE-0002

        // Assign another large package — second large locker
        String code3 = ls.assignLocker("PKG003", LockerSize.LARGE, 1);
        System.out.println("PKG003 (LARGE) -> code: " + code3);
        // Expected: CODE-0003

        // No more large lockers — null returned
        String code4 = ls.assignLocker("PKG004", LockerSize.LARGE, 1);
        System.out.println("PKG004 (LARGE, none free) -> code: " + code4);
        // Expected: null

        // Medium package overflows to LARGE when MEDIUM is occupied
        ls.assignLocker("PKG005", LockerSize.MEDIUM, 1);  // takes first medium locker (CODE-0005)
        ls.assignLocker("PKG006", LockerSize.MEDIUM, 1);  // takes second medium locker (CODE-0006)
        // Now no MEDIUM lockers left and no LARGE left — should fail
        String code7 = ls.assignLocker("PKG007", LockerSize.MEDIUM, 1);
        System.out.println("PKG007 (MEDIUM, none free) -> code: " + code7);
        // Expected: null

        System.out.println();

        // Open a locker with the correct code
        String retrieved = ls.openLocker(code1);
        System.out.println("openLocker(" + code1 + ") -> package: " + retrieved);
        // Expected: PKG001

        // Using the same code again fails (already used)
        System.out.println("openLocker(" + code1 + ") again -> " + ls.openLocker(code1));
        // Expected: null

        System.out.println();
        ls.printStatus();
        // Small: 3 (PKG001 freed), Medium: 0 (PKG005,PKG006 occupying)

        System.out.println();

        // Advance to day 5 — PKG002 and PKG003 (assigned day 1) expire (5-1 >= 3)
        System.out.println("--- Day 5: releasing expired packages ---");
        int reclaimed = ls.releaseExpired(5);
        System.out.println("Reclaimed: " + reclaimed + " lockers");
        // Expected: reclaimed >= 2 (PKG002, PKG003, PKG005, PKG006 all expire; PKG001 already freed)

        System.out.println();
        ls.printStatus();
        // Expected: all lockers available again except PKG007 never assigned
    }
}
