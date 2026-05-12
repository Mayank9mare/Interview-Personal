import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class Lock {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. synchronized — built-in, simplest
    //    Use when: basic mutual exclusion, no need for tryLock or fairness
    // ─────────────────────────────────────────────────────────────────────────
    static class ParkingLot {
        private int freeSpots = 10;
        private final Object lock = new Object();

        public boolean park() {
            synchronized (lock) {
                if (freeSpots == 0) return false;
                freeSpots--;
                return true;
            }
        }

        public void leave() {
            synchronized (lock) {
                freeSpots++;
            }
        }

        public int getFreeSpots() {
            synchronized (lock) {
                return freeSpots;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. ReentrantLock — explicit lock/unlock, supports tryLock + timeout
    //    Use when: need tryLock, timed acquisition, or interruptible waits
    // ─────────────────────────────────────────────────────────────────────────
    static class TicketCounter {
        private int tickets = 5;
        private final ReentrantLock lock = new ReentrantLock();

        // blocks until lock is available
        public boolean buyTicket() {
            lock.lock();
            try {
                if (tickets == 0) return false;
                tickets--;
                return true;
            } finally {
                lock.unlock(); // always in finally — never skip this
            }
        }

        // won't wait — moves on if lock is busy
        public boolean tryBuyTicket() throws InterruptedException {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    if (tickets == 0) return false;
                    tickets--;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
            return false; // couldn't acquire lock in time
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. ReadWriteLock — multiple readers OR one writer, never both
    //    Use when: reads are frequent, writes are rare (e.g. cache, leaderboard)
    // ─────────────────────────────────────────────────────────────────────────
    static class Leaderboard {
        private final java.util.Map<String, Integer> scores = new java.util.HashMap<>();
        private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

        // many threads can read at the same time
        public int getScore(String player) {
            rwLock.readLock().lock();
            try {
                return scores.getOrDefault(player, 0);
            } finally {
                rwLock.readLock().unlock();
            }
        }

        // only one thread can write; blocks all readers during write
        public void updateScore(String player, int delta) {
            rwLock.writeLock().lock();
            try {
                scores.merge(player, delta, Integer::sum);
            } finally {
                rwLock.writeLock().unlock();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Semaphore — limits how many threads can enter concurrently (not just 1)
    //    Use when: rate limiting, connection pools, bounded resource access
    // ─────────────────────────────────────────────────────────────────────────
    static class DatabaseConnectionPool {
        private final Semaphore semaphore;

        DatabaseConnectionPool(int maxConnections) {
            semaphore = new Semaphore(maxConnections);
        }

        public void executeQuery(String query) throws InterruptedException {
            semaphore.acquire(); // blocks if maxConnections threads already inside
            try {
                System.out.println(Thread.currentThread().getName() + " running: " + query);
                Thread.sleep(100); // simulate query
            } finally {
                semaphore.release();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. AtomicInteger / AtomicReference — lock-free, CAS-based
    //    Use when: single counter or flag, no compound operations needed
    // ─────────────────────────────────────────────────────────────────────────
    static class WebpageVisitCounter {
        private final AtomicIntegerArray counts;

        WebpageVisitCounter(int totalPages) {
            counts = new AtomicIntegerArray(totalPages);
        }

        public void increment(int pageIndex) {
            counts.incrementAndGet(pageIndex); // no lock needed
        }

        public int get(int pageIndex) {
            return counts.get(pageIndex);
        }
    }

    static class IdGenerator {
        private final AtomicInteger next = new AtomicInteger(0);

        public int nextId() {
            return next.getAndIncrement(); // thread-safe, no lock
        }

        // compareAndSet: only update if current value matches expected
        public boolean reserveId(int expected, int newId) {
            return next.compareAndSet(expected, newId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Demo
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {
        // --- synchronized ---
        ParkingLot lot = new ParkingLot();
        lot.park();
        lot.park();
        System.out.println("Free spots: " + lot.getFreeSpots()); // 8

        // --- ReentrantLock ---
        TicketCounter counter = new TicketCounter();
        System.out.println("Bought: " + counter.buyTicket()); // true
        System.out.println("Bought: " + counter.tryBuyTicket()); // true

        // --- ReadWriteLock ---
        Leaderboard lb = new Leaderboard();
        lb.updateScore("alice", 100);
        lb.updateScore("alice", 50);
        System.out.println("Alice: " + lb.getScore("alice")); // 150

        // --- Semaphore: 3 threads, only 2 connections allowed ---
        DatabaseConnectionPool pool = new DatabaseConnectionPool(2);
        for (int i = 0; i < 3; i++) {
            final int n = i;
            new Thread(() -> {
                try { pool.executeQuery("SELECT " + n); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "T" + i).start();
        }
        Thread.sleep(500);

        // --- Atomic ---
        WebpageVisitCounter visits = new WebpageVisitCounter(3);
        visits.increment(0);
        visits.increment(0);
        visits.increment(1);
        System.out.println("Page 0 visits: " + visits.get(0)); // 2
        System.out.println("Page 1 visits: " + visits.get(1)); // 1

        IdGenerator gen = new IdGenerator();
        System.out.println("IDs: " + gen.nextId() + ", " + gen.nextId()); // 0, 1
    }
}
