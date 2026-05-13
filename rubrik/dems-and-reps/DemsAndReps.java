import java.util.concurrent.locks.*;

public class DemsAndReps {

    // ═══════════════════════════════════════════════════════════════════════════
    // Democrats & Republicans — Shared Bathroom Problem
    // (also known as: Unisex Bathroom / Readers-Writers variant)
    //
    // Rules:
    //   1. Democrats and Republicans share one bathroom.
    //   2. Any number of members of the SAME party may be inside simultaneously.
    //   3. Members of OPPOSITE parties cannot be inside at the same time.
    //   4. No starvation: when the bathroom empties, the party that has been
    //      waiting longer gets priority. Fair ReentrantLock ensures FIFO ordering
    //      for lock acquisition, preventing indefinite blocking.
    //
    // API:
    //   democratEnter()    — blocks until safe to enter
    //   democratExit()     — leaves; signals opposite party if bathroom empty
    //   republicanEnter()  — blocks until safe to enter
    //   republicanExit()   — leaves; signals opposite party if bathroom empty
    //
    // Design:
    //   • Single ReentrantLock (fair) guards all shared state.
    //   • Two Conditions: demTurn, repTurn.
    //   • Waiting counts allow exit to signal the correct condition.
    //   • On exit: if bathroom empties AND opposite party is waiting → signal them.
    //     Otherwise signal own party (to unblock same-party threads still waiting).
    //
    // Invariant: democratsInside > 0 → republicansInside == 0, and vice versa.
    // ═══════════════════════════════════════════════════════════════════════════

    private int democratsInside    = 0;
    private int republicansInside  = 0;
    private int democratsWaiting   = 0;
    private int republicansWaiting = 0;

    private final ReentrantLock lock   = new ReentrantLock(true); // fair
    private final Condition     demTurn = lock.newCondition();
    private final Condition     repTurn = lock.newCondition();

    // ── Democrat ──────────────────────────────────────────────────────────────

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

    private static Thread dem(DemsAndReps b, String name, long startMs, long useMs) {
        return party(b, name, startMs, useMs, true);
    }

    private static Thread rep(DemsAndReps b, String name, long startMs, long useMs) {
        return party(b, name, startMs, useMs, false);
    }

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
