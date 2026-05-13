import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class JobScheduler {

    // ═══════════════════════════════════════════════════════════════════════════
    // Job Scheduler — implements ScheduledExecutorService semantics
    //
    // API:
    //   schedule(task, delayMs)                  — run once after delayMs
    //   scheduleAtFixedRate(task, initMs, perMs) — run periodically
    //   shutdown()                               — drain and stop
    //
    // Design:
    //   • PriorityQueue<ScheduledTask> ordered by trigger time
    //     (raw PriorityQueue + ReentrantLock, NOT DelayQueue/BlockingQueue)
    //   • Single dispatcher thread wakes at the next trigger time,
    //     pulls the task, and submits it to a fixed worker pool.
    //   • Periodic tasks re-enqueue themselves after each execution.
    //   • A new earlier task signals the dispatcher to re-evaluate sleep time.
    //
    // Thread safety: all queue access under ReentrantLock + Condition.
    // Complexity: enqueue O(log n), dispatch O(log n) per task.
    // ═══════════════════════════════════════════════════════════════════════════

    static class ScheduledTask implements Comparable<ScheduledTask> {
        final Runnable  action;
        final long      periodMs;       // 0 = one-shot, >0 = periodic
        volatile long   triggerMs;      // absolute epoch-ms when to fire

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

    private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>();
    private final ReentrantLock                lock  = new ReentrantLock();
    private final Condition                    ready = lock.newCondition();
    private final ExecutorService              pool;
    private volatile boolean                   running = true;
    private final Thread                       dispatcher;

    public JobScheduler(int poolSize) {
        pool = Executors.newFixedThreadPool(poolSize);
        dispatcher = new Thread(this::dispatchLoop, "sched-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void schedule(Runnable task, long delayMs) {
        enqueue(new ScheduledTask(task, delayMs, 0));
    }

    public void scheduleAtFixedRate(Runnable task, long initialDelayMs, long periodMs) {
        if (periodMs <= 0) throw new IllegalArgumentException("period must be > 0");
        enqueue(new ScheduledTask(task, initialDelayMs, periodMs));
    }

    public void shutdown() {
        running = false;
        lock.lock();
        try { ready.signalAll(); } finally { lock.unlock(); }
        pool.shutdown();
        try { pool.awaitTermination(2, TimeUnit.SECONDS); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private void enqueue(ScheduledTask st) {
        lock.lock();
        try {
            queue.offer(st);
            ready.signal(); // wake dispatcher — might be an earlier task
        } finally {
            lock.unlock();
        }
    }

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
