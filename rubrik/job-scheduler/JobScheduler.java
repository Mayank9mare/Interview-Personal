import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Job scheduler that supports one-shot and periodic task execution using raw concurrency
 * primitives — no {@code DelayQueue}, {@code BlockingQueue}, or {@code ScheduledExecutorService}.
 *
 * <p>Architecture:
 * <ul>
 *   <li>A single dispatcher thread sleeps until the next trigger time, then submits
 *       the due task to a fixed worker-thread pool.</li>
 *   <li>A {@code PriorityQueue} ordered by trigger time holds pending tasks; all access
 *       is protected by a {@code ReentrantLock + Condition}.</li>
 *   <li>When a new task with an earlier trigger is enqueued, it calls {@code signal()}
 *       on the condition to wake the dispatcher immediately.</li>
 *   <li>Periodic tasks re-enqueue themselves inside the worker callback after each run.</li>
 * </ul>
 *
 * <p>Thread safety: all queue mutations are guarded by {@code lock}.
 */
public class JobScheduler {

    /**
     * A pending task entry in the scheduler queue.
     *
     * <p>Comparable by {@code triggerMs} so the {@link PriorityQueue} always surfaces
     * the next-due task at its head.
     */
    static class ScheduledTask implements Comparable<ScheduledTask> {
        /** The work to run when the trigger fires. */
        final Runnable  action;

        /** Repetition period; {@code 0} = one-shot, {@code > 0} = run every {@code periodMs} ms. */
        final long      periodMs;

        /** Absolute epoch-ms at which this task should next fire. */
        volatile long   triggerMs;

        /**
         * @param action   the task to run
         * @param delayMs  initial delay before first execution
         * @param periodMs repetition period (0 for one-shot)
         */
        ScheduledTask(Runnable action, long delayMs, long periodMs) {
            this.action    = action;
            this.periodMs  = periodMs;
            this.triggerMs = System.currentTimeMillis() + delayMs;
        }

        @Override
        public int compareTo(ScheduledTask o) {
            return Long.compare(this.triggerMs, o.triggerMs);
        }
    }

    /** Min-heap of pending tasks; head = earliest trigger time. */
    private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>();

    /** Guards all access to {@code queue}. */
    private final ReentrantLock                lock  = new ReentrantLock();

    /** Signalled when a new task is enqueued or when the scheduler shuts down. */
    private final Condition                    ready = lock.newCondition();

    /** Thread pool that executes task actions. */
    private final ExecutorService              pool;

    /** Set to {@code false} by {@link #shutdown()} to terminate the dispatcher. */
    private volatile boolean                   running = true;

    /** Background thread that pulls due tasks and submits them to {@code pool}. */
    private final Thread                       dispatcher;

    /**
     * Creates a scheduler with the given worker pool size and starts the dispatcher.
     *
     * @param poolSize number of threads in the worker pool
     */
    public JobScheduler(int poolSize) {
        pool = Executors.newFixedThreadPool(poolSize);
        dispatcher = new Thread(this::dispatchLoop, "sched-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Schedules {@code task} to run once after {@code delayMs} milliseconds.
     *
     * @param task    the work to execute
     * @param delayMs delay before execution (0 = run as soon as possible)
     */
    public void schedule(Runnable task, long delayMs) {
        enqueue(new ScheduledTask(task, delayMs, 0));
    }

    /**
     * Schedules {@code task} to run repeatedly at a fixed rate.
     *
     * @param task            the work to execute
     * @param initialDelayMs  delay before the first execution
     * @param periodMs        interval between subsequent executions (must be &gt; 0)
     * @throws IllegalArgumentException if {@code periodMs} is not positive
     */
    public void scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) {
        if (periodMs <= 0) throw new IllegalArgumentException("period must be > 0");
        enqueue(new ScheduledTask(task, initialDelayMs, periodMs));
    }

    /**
     * Signals the dispatcher to stop and waits up to 2 seconds for the worker pool to drain.
     */
    public void shutdown() {
        running = false;
        lock.lock();
        try { ready.signalAll(); } finally { lock.unlock(); }
        pool.shutdown();
        try { pool.awaitTermination(2, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    /** Adds {@code st} to the queue under the lock and signals the dispatcher. */
    private void enqueue(ScheduledTask st) {
        lock.lock();
        try {
            queue.offer(st);
            ready.signal(); // wake dispatcher — might be an earlier task
        } finally {
            lock.unlock();
        }
    }

    /**
     * Main loop of the dispatcher thread. Sleeps until the next trigger time, submits
     * the due task to the pool, and re-evaluates. Terminates when {@link #running} is false.
     */
    private void dispatchLoop() {
        while (running) {
            lock.lock();
            try {
                // Sleep until a task is queued
                while (running && queue.isEmpty()) ready.await();
                if (!running) break;

                long waitMs = queue.peek().triggerMs - System.currentTimeMillis();
                if (waitMs > 0) {
                    // Sleep until trigger time or until a new task is added
                    ready.awaitNanos(waitMs * 1_000_000L);
                    continue; // re-check — a new earlier task may have arrived
                }

                // Task is due — pull and run
                ScheduledTask task = queue.poll();
                pool.submit(() -> {
                    try {
                        task.action.run();
                    } catch (Exception e) {
                        System.err.println("[scheduler] task error: " + e.getMessage());
                    }
                    if (task.periodMs > 0 && running) {
                        task.triggerMs = System.currentTimeMillis() + task.periodMs;
                        enqueue(task);
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } finally {
                lock.unlock();
            }
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        long t0 = System.currentTimeMillis();
        JobScheduler s = new JobScheduler(4);

        s.schedule(() -> log(t0, "One-shot A  (delay 100ms)"), 100);
        s.schedule(() -> log(t0, "One-shot B  (delay  50ms)"), 50);
        s.schedule(() -> log(t0, "One-shot C  (delay 300ms)"), 300);

        s.scheduleAtFixedRate(() -> log(t0, "Periodic P1 (every 200ms)"), 0,   200);
        s.scheduleAtFixedRate(() -> log(t0, "Periodic P2 (every 350ms)"), 100, 350);

        Thread.sleep(1500);
        s.shutdown();
        System.out.println("Scheduler shut down after " + (System.currentTimeMillis() - t0) + "ms.");
    }

    private static void log(long t0, String msg) {
        System.out.printf("[+%4dms] [%s] %s%n",
            System.currentTimeMillis() - t0,
            Thread.currentThread().getName(), msg);
    }
}
