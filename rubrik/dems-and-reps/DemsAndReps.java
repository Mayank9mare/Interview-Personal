import java.util.concurrent.locks.*;

/**
 * Shared-bathroom concurrency problem (Readers-Writers variant): Democrats and Republicans
 * share one bathroom; members of the same party may use it simultaneously, but members of
 * opposite parties cannot overlap.
 *
 * <p>Design:
 * <ul>
 *   <li>Single <em>fair</em> {@link ReentrantLock} guards all shared state — FIFO lock
 *       acquisition prevents indefinite starvation.</li>
 *   <li>Two {@link Condition}s ({@code demTurn}, {@code repTurn}) — exiting the bathroom
 *       signals the opposite party if any are waiting, otherwise signals same-party threads.</li>
 * </ul>
 *
 * <p>Core invariant: {@code democratsInside > 0 → republicansInside == 0} and vice versa.
 *
 * <p>Thread safety: Fully thread-safe; designed for concurrent multi-threaded use.
 */
public class DemsAndReps {

    /** Democrats currently inside the bathroom. */
    private int democratsInside    = 0;

    /** Republicans currently inside the bathroom. */
    private int republicansInside  = 0;

    /** Democrats blocked waiting to enter. */
    private int democratsWaiting   = 0;

    /** Republicans blocked waiting to enter. */
    private int republicansWaiting = 0;

    /** Fair lock: FIFO ordering prevents starvation between parties. */
    private final ReentrantLock lock   = new ReentrantLock(true);

    /** Condition signalled when the bathroom becomes available for Democrats. */
    private final Condition     demTurn = lock.newCondition();

    /** Condition signalled when the bathroom becomes available for Republicans. */
    private final Condition     repTurn = lock.newCondition();

    // ── Democrat ──────────────────────────────────────────────────────────────

    /**
     * Blocks until no Republicans are inside, then enters the bathroom.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void democratEnter() throws InterruptedException {
        lock.lock();
        try {
            democratsWaiting++;
            while (republicansInside > 0)
                demTurn.await();
            democratsWaiting--;
            democratsInside++;
            log("entered ", "D");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records a Democrat leaving. If the bathroom empties, signals Republicans if any are
     * waiting; otherwise signals remaining Democrats.
     */
    public void democratExit() {
        lock.lock();
        try {
            democratsInside--;
            log("exited  ", "D");
            if (democratsInside == 0) {
                // Bathroom empty — give priority to the other party if waiting
                if (republicansWaiting > 0) repTurn.signalAll();
                else                         demTurn.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    // ── Republican ────────────────────────────────────────────────────────────

    /**
     * Blocks until no Democrats are inside, then enters the bathroom.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void republicanEnter() throws InterruptedException {
        lock.lock();
        try {
            republicansWaiting++;
            while (democratsInside > 0)
                repTurn.await();
            republicansWaiting--;
            republicansInside++;
            log("entered ", "R");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Records a Republican leaving. If the bathroom empties, signals Democrats if any are
     * waiting; otherwise signals remaining Republicans.
     */
    public void republicanExit() {
        lock.lock();
        try {
            republicansInside--;
            log("exited  ", "R");
            if (republicansInside == 0) {
                if (democratsWaiting > 0) demTurn.signalAll();
                else                       repTurn.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    /** Prints the current enter/exit event and live inside/waiting counts. */
    private void log(String action, String party) {
        System.out.printf("%-10s (%s) %s | inside: D=%-2d R=%-2d | waiting: D=%-2d R=%-2d%n",
            Thread.currentThread().getName(), party, action,
            democratsInside, republicansInside,
            democratsWaiting, republicansWaiting);
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        DemsAndReps bathroom = new DemsAndReps();

        // Scenario: D1 and D2 arrive early; then R1,R2 arrive while Ds are inside;
        // then more Ds and Rs to show alternation and no starvation.
        Thread[] threads = {
            dem(bathroom, "Dem-1",  0,   300),  // arrives first
            dem(bathroom, "Dem-2",  50,  250),  // overlaps with Dem-1
            rep(bathroom, "Rep-1",  100, 300),  // must wait for Dems to leave
            rep(bathroom, "Rep-2",  120, 250),  // joins Rep-1 once Dems leave
            dem(bathroom, "Dem-3",  200, 200),  // arrives while Reps waiting — must wait
            dem(bathroom, "Dem-4",  400, 150),  // should enter with Dem-3 after Reps done
            rep(bathroom, "Rep-3",  500, 200),
            dem(bathroom, "Dem-5",  600, 100),
        };

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        System.out.println("\nAll threads finished. Bathroom empty.");
        System.out.println("Invariant held if no line shows both D>0 and R>0 simultaneously.");
    }

    /** Creates a Democrat thread that sleeps {@code startMs}, uses the bathroom for {@code useMs}. */
    private static Thread dem(DemsAndReps b, String name, long startMs, long useMs) {
        return party(b, name, startMs, useMs, true);
    }

    /** Creates a Republican thread that sleeps {@code startMs}, uses the bathroom for {@code useMs}. */
    private static Thread rep(DemsAndReps b, String name, long startMs, long useMs) {
        return party(b, name, startMs, useMs, false);
    }

    /** Shared factory: creates a daemon thread that calls enter/exit on the bathroom object. */
    private static Thread party(DemsAndReps b, String name, long startMs, long useMs, boolean dem) {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(startMs);
                if (dem) b.democratEnter();  else b.republicanEnter();
                Thread.sleep(useMs);
                if (dem) b.democratExit();   else b.republicanExit();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, name);
        t.setDaemon(true);
        return t;
    }
}
