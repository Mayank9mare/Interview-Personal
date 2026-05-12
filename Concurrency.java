import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;

public class Concurrency {

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Producer-Consumer — BlockingQueue as the shared buffer
    //    Use when: decoupling producers from consumers; bounded capacity
    //    LinkedBlockingQueue  — linked nodes, separate head/tail locks
    //    ArrayBlockingQueue   — array-backed, single lock, bounded capacity
    //    SynchronousQueue     — no buffer; producer blocks until consumer ready
    // ─────────────────────────────────────────────────────────────────────────
    static class ProducerConsumerDemo {
        private final BlockingQueue<Integer> queue;

        ProducerConsumerDemo(int capacity) {
            queue = new LinkedBlockingQueue<>(capacity);
        }

        class Producer implements Runnable {
            @Override public void run() {
                for (int i = 0; i < 5; i++) {
                    try {
                        queue.put(i); // blocks if full
                        System.out.println("Produced: " + i);
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }

        class Consumer implements Runnable {
            @Override public void run() {
                for (int i = 0; i < 5; i++) {
                    try {
                        int item = queue.take(); // blocks if empty
                        System.out.println("Consumed: " + item);
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Thread Pool — ExecutorService
    //    newFixedThreadPool(n)      — n threads, unbounded queue
    //    newCachedThreadPool()      — unlimited threads, 60s keepalive
    //    newSingleThreadExecutor()  — 1 thread, tasks run in order
    //    newScheduledThreadPool(n)  — delayed / periodic tasks
    //    newWorkStealingPool()      — ForkJoinPool, uses all CPU cores
    // ─────────────────────────────────────────────────────────────────────────
    static void threadPoolDemo() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);

        // Runnable — fire and forget, no return value
        pool.execute(() -> System.out.println("Runnable on: " + Thread.currentThread().getName()));

        // Callable<T> — returns Future<T>; get() blocks until done
        Future<Integer> future = pool.submit(() -> { Thread.sleep(50); return 42; });
        System.out.println("Future.get(): " + future.get()); // 42

        // isDone / cancel
        Future<String> slowFuture = pool.submit(() -> { Thread.sleep(10000); return "slow"; });
        System.out.println("isDone: " + slowFuture.isDone()); // false
        slowFuture.cancel(true); // interrupt the thread
        System.out.println("isCancelled: " + slowFuture.isCancelled()); // true

        // invokeAll — submit batch, wait for ALL (blocks until all done)
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3);
        List<Future<Integer>> results = pool.invokeAll(tasks);
        for (Future<Integer> f : results) System.out.print(f.get() + " "); // 1 2 3
        System.out.println();

        // invokeAny — return result of FIRST completed (cancels rest)
        Integer first = pool.invokeAny(List.of(() -> { Thread.sleep(100); return 1; }, () -> 2));
        System.out.println("invokeAny: " + first); // 2 (faster one)

        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        // ScheduledExecutorService — run-once delay or periodic
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.schedule(() -> System.out.println("Delayed 100ms"), 100, TimeUnit.MILLISECONDS);
        // scheduleAtFixedRate — fixed rate regardless of task duration
        // scheduleWithFixedDelay — delay starts AFTER task completes
        ScheduledFuture<?> periodic = scheduler.scheduleAtFixedRate(
            () -> System.out.print("tick "), 0, 100, TimeUnit.MILLISECONDS);
        Thread.sleep(350);
        periodic.cancel(false);
        System.out.println();
        scheduler.shutdown();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. CompletableFuture — async non-blocking pipelines
    //    supplyAsync   — async task with return value (uses ForkJoinPool)
    //    runAsync      — async task, no return value
    //    thenApply     — transform result (sync in same thread)
    //    thenApplyAsync — transform result (async in another thread)
    //    thenCompose   — flatMap (returns another CompletableFuture)
    //    thenCombine   — combine two independent futures
    //    thenAccept    — consume result, returns CF<Void>
    //    allOf         — wait for all to complete
    //    anyOf         — complete when any completes
    //    exceptionally — handle exception, provide fallback
    //    handle        — runs on both success and failure
    // ─────────────────────────────────────────────────────────────────────────
    static void completableFutureDemo() throws Exception {
        // basic chain
        String result = CompletableFuture
            .supplyAsync(() -> "hello")
            .thenApply(String::toUpperCase)      // HELLO
            .thenApply(s -> s + "!")              // HELLO!
            .get();
        System.out.println("Chain: " + result);

        // thenCompose — when second step is itself async (flatMap)
        String chained = CompletableFuture
            .supplyAsync(() -> "user-42")
            .thenCompose(id -> CompletableFuture.supplyAsync(() -> "profile:" + id))
            .get();
        System.out.println("Compose: " + chained);

        // thenCombine — two independent async tasks merged
        CompletableFuture<String> greeting = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> name     = CompletableFuture.supplyAsync(() -> "Alice");
        String combined = greeting.thenCombine(name, (g, n) -> g + " " + n).get();
        System.out.println("Combine: " + combined); // Hello Alice

        // allOf — wait for all, then collect results
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> 2);
        CompletableFuture<Integer> f3 = CompletableFuture.supplyAsync(() -> 3);
        CompletableFuture.allOf(f1, f2, f3).get();
        System.out.println("allOf sum: " + (f1.get() + f2.get() + f3.get())); // 6

        // anyOf — first completed wins
        CompletableFuture<Object> any = CompletableFuture.anyOf(
            CompletableFuture.supplyAsync(() -> { try { Thread.sleep(500); } catch (Exception e) {} return "slow"; }),
            CompletableFuture.supplyAsync(() -> "fast")
        );
        System.out.println("anyOf: " + any.get()); // fast

        // exceptionally — fallback on error
        String safe = CompletableFuture
            .supplyAsync(() -> { if (true) throw new RuntimeException("fail"); return "ok"; })
            .exceptionally(ex -> "fallback:" + ex.getMessage())
            .get();
        System.out.println("Exceptionally: " + safe);

        // handle — runs always (success or failure)
        String handled = CompletableFuture
            .supplyAsync(() -> "success")
            .handle((res, ex) -> ex != null ? "error" : res.toUpperCase())
            .get();
        System.out.println("Handle: " + handled); // SUCCESS

        // whenComplete — side effect, does not transform result
        CompletableFuture.supplyAsync(() -> 42)
            .whenComplete((res, ex) -> System.out.println("whenComplete: " + res))
            .get();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. CountDownLatch — one-shot gate; N countdowns then open
    //    Use when: main thread waits for N workers to finish init
    //    Cannot be reset (use CyclicBarrier if reuse needed)
    // ─────────────────────────────────────────────────────────────────────────
    static void countDownLatchDemo() throws InterruptedException {
        int n = 3;
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            final int id = i;
            new Thread(() -> {
                System.out.println("Worker " + id + " done");
                latch.countDown(); // count–1; does NOT block the worker
            }).start();
        }

        latch.await(); // blocks until count == 0
        System.out.println("All workers finished");

        // await with timeout — don't block forever
        CountDownLatch timedLatch = new CountDownLatch(1);
        boolean reached = timedLatch.await(100, TimeUnit.MILLISECONDS);
        System.out.println("Timed await result: " + reached); // false (nobody counted down)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. CyclicBarrier — N threads meet at barrier, then all continue
    //    Use when: multiple phases — all threads finish phase i before phase i+1
    //    Reusable (cyclic). Optional Runnable fires when all threads arrive.
    // ─────────────────────────────────────────────────────────────────────────
    static void cyclicBarrierDemo() throws Exception {
        int n = 3;
        CyclicBarrier barrier = new CyclicBarrier(n,
            () -> System.out.println("--- All at barrier, advancing ---"));

        for (int i = 0; i < n; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + id + " phase 1");
                    barrier.await(); // wait for all n threads
                    System.out.println("Thread " + id + " phase 2");
                } catch (Exception e) { Thread.currentThread().interrupt(); }
            }).start();
        }
        Thread.sleep(300);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6. Phaser — flexible multi-phase barrier with dynamic registration
    //    Use when: dynamic number of tasks, tasks can join/leave mid-flight
    // ─────────────────────────────────────────────────────────────────────────
    static void phaserDemo() throws InterruptedException {
        Phaser phaser = new Phaser(1); // register main thread as party

        for (int i = 0; i < 3; i++) {
            phaser.register(); // register worker before spawning
            final int id = i;
            new Thread(() -> {
                System.out.println("Worker " + id + " phase " + phaser.getPhase());
                phaser.arriveAndAwaitAdvance(); // arrive + wait for all parties
                System.out.println("Worker " + id + " phase " + phaser.getPhase());
                phaser.arriveAndDeregister(); // done, reduce party count
            }).start();
        }

        phaser.arriveAndAwaitAdvance(); // main arrives at phase 0 barrier
        System.out.println("Phase 0 complete, phase now: " + phaser.getPhase());
        phaser.arriveAndDeregister(); // main done
        Thread.sleep(200);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Fork/Join — divide-and-conquer parallelism
    //    RecursiveTask<V>   — returns a value
    //    RecursiveAction    — void, no return value
    //    Work-stealing: idle threads steal tasks from busy threads' queues
    // ─────────────────────────────────────────────────────────────────────────
    static class SumTask extends RecursiveTask<Long> {
        private final int[] arr;
        private final int lo, hi;
        private static final int THRESHOLD = 1000;

        SumTask(int[] arr, int lo, int hi) { this.arr = arr; this.lo = lo; this.hi = hi; }

        @Override
        protected Long compute() {
            if (hi - lo <= THRESHOLD) {
                long sum = 0;
                for (int i = lo; i < hi; i++) sum += arr[i];
                return sum;
            }
            int mid = (lo + hi) / 2;
            SumTask left = new SumTask(arr, lo, mid);
            SumTask right = new SumTask(arr, mid, hi);
            left.fork();                        // async execution
            return right.compute() + left.join(); // right inline + wait for left
        }
    }

    static void forkJoinDemo() {
        int[] arr = new int[10_000];
        Arrays.fill(arr, 1);
        ForkJoinPool pool = ForkJoinPool.commonPool(); // shared pool (avoid creating new ones)
        long sum = pool.invoke(new SumTask(arr, 0, arr.length));
        System.out.println("Fork/Join sum: " + sum); // 10000
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Exchanger — two threads swap data at a synchronization point
    //    Use when: pipeline handoff; producer fills buffer, consumer drains it
    // ─────────────────────────────────────────────────────────────────────────
    static void exchangerDemo() throws InterruptedException {
        Exchanger<String> exchanger = new Exchanger<>();

        new Thread(() -> {
            try {
                String got = exchanger.exchange("from-thread");
                System.out.println("Thread got: " + got);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }).start();

        String got = exchanger.exchange("from-main");
        System.out.println("Main got: " + got);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 9. StampedLock — optimistic reads; faster than ReadWriteLock when
    //    reads rarely conflict with writes
    //    tryOptimisticRead — no lock, but must validate stamp before use
    //    readLock — shared, like ReadWriteLock
    //    writeLock — exclusive
    // ─────────────────────────────────────────────────────────────────────────
    static class Point {
        private double x, y;
        private final StampedLock lock = new StampedLock();

        void move(double dx, double dy) {
            long stamp = lock.writeLock();
            try { x += dx; y += dy; } finally { lock.unlockWrite(stamp); }
        }

        double distanceFromOrigin() {
            long stamp = lock.tryOptimisticRead(); // no lock — just a stamp
            double cx = x, cy = y;
            if (!lock.validate(stamp)) { // check if write happened since stamp
                stamp = lock.readLock(); // fall back to real read lock
                try { cx = x; cy = y; } finally { lock.unlockRead(stamp); }
            }
            return Math.sqrt(cx * cx + cy * cy);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 10. Thread-local state — each thread has its own isolated copy
    //     Use when: user context, request IDs, date formatters (not thread-safe)
    // ─────────────────────────────────────────────────────────────────────────
    static final ThreadLocal<Integer> REQUEST_ID = ThreadLocal.withInitial(() -> 0);

    static void threadLocalDemo() throws InterruptedException {
        Runnable task = () -> {
            REQUEST_ID.set((int)(Math.random() * 1000));
            System.out.println(Thread.currentThread().getName() + " id=" + REQUEST_ID.get());
            REQUEST_ID.remove(); // always remove to avoid leaks in thread pools
        };
        Thread t1 = new Thread(task, "T1");
        Thread t2 = new Thread(task, "T2");
        t1.start(); t2.start();
        t1.join(); t2.join();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 11. Common Concurrency Gotchas
    //
    //  Deadlock   — A holds lock1, wants lock2; B holds lock2, wants lock1
    //               Fix: always acquire locks in the SAME ORDER
    //
    //  Livelock   — threads keep responding to each other but no progress
    //               Fix: add random backoff
    //
    //  Starvation — low-priority thread never gets CPU
    //               Fix: fair locks (new ReentrantLock(true))
    //
    //  Race cond  — outcome depends on thread scheduling
    //               Fix: synchronize shared mutable state
    //
    //  Visibility — writes on one thread not visible to another without sync
    //               Fix: volatile, happens-before via lock/unlock
    // ─────────────────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────────────────────────────────────
    // Demo
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.out.println("=== Producer-Consumer ===");
        ProducerConsumerDemo pc = new ProducerConsumerDemo(2);
        Thread p = new Thread(pc.new Producer());
        Thread c = new Thread(pc.new Consumer());
        p.start(); c.start();
        p.join(); c.join();

        System.out.println("\n=== Thread Pool ===");
        threadPoolDemo();

        System.out.println("\n=== CompletableFuture ===");
        completableFutureDemo();

        System.out.println("\n=== CountDownLatch ===");
        countDownLatchDemo();

        System.out.println("\n=== CyclicBarrier ===");
        cyclicBarrierDemo();

        System.out.println("\n=== Phaser ===");
        phaserDemo();

        System.out.println("\n=== Fork/Join ===");
        forkJoinDemo();

        System.out.println("\n=== Exchanger ===");
        exchangerDemo();

        System.out.println("\n=== StampedLock ===");
        Point pt = new Point();
        pt.move(3, 4);
        System.out.printf("Distance: %.1f%n", pt.distanceFromOrigin()); // 5.0

        System.out.println("\n=== ThreadLocal ===");
        threadLocalDemo();
    }
}
