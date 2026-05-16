# Concurrency.py — Python concurrency reference
# Run: python Concurrency.py

import threading
import queue
import time
import asyncio
from concurrent.futures import ThreadPoolExecutor, ProcessPoolExecutor, as_completed
import multiprocessing

# ─────────────────────────────────────────────────────────────────────────────
# 1. Thread Creation
#    threading.Thread(target=fn, args=(...), daemon=True)
#    start()  — schedules the thread; does NOT block
#    join()   — caller blocks until the thread finishes
#    daemon   — thread dies automatically when main thread exits (no join needed)
#    GIL NOTE: CPython's Global Interpreter Lock means only one thread runs
#              Python bytecode at a time → threads don't speed up CPU-bound work.
#              Use threads for I/O-bound tasks; use multiprocessing for CPU-bound.
# ─────────────────────────────────────────────────────────────────────────────
def thread_creation_demo():
    def worker(n):
        print(f"  Thread {n}: {threading.current_thread().name}")

    t1 = threading.Thread(target=worker, args=(1,), name="worker-1")
    t2 = threading.Thread(target=worker, args=(2,), daemon=True)  # dies with main
    t1.start(); t2.start()
    t1.join()  # wait for t1; t2 daemon — no join required

    # subclass approach
    class MyThread(threading.Thread):
        def __init__(self, label):
            super().__init__(name=label)
        def run(self):
            print(f"  MyThread running: {self.name}")

    t3 = MyThread("custom")
    t3.start(); t3.join()

    # interrupt equivalent — use an Event flag; Python threads can't be hard-killed
    stop_flag = threading.Event()
    def interruptible():
        while not stop_flag.is_set():
            time.sleep(0.01)
        print("  Worker stopped via flag")

    t4 = threading.Thread(target=interruptible)
    t4.start()
    time.sleep(0.05)
    stop_flag.set()
    t4.join()

    print(f"  Active threads: {threading.active_count()}")


# ─────────────────────────────────────────────────────────────────────────────
# 2. Lock / RLock — mutual exclusion
#    Lock     — simple mutex; same thread acquiring twice → deadlock
#    RLock    — reentrant; same thread can acquire multiple times (must release same count)
#    Always use with-statement so the lock is released even on exception.
# ─────────────────────────────────────────────────────────────────────────────
class Counter:
    def __init__(self):
        self._count = 0
        self._lock = threading.Lock()

    def increment(self):
        with self._lock:       # acquire on enter, release on exit
            self._count += 1

    def decrement(self):
        with self._lock:
            self._count -= 1

    @property
    def value(self):
        with self._lock:
            return self._count


def lock_demo():
    counter = Counter()
    inc = threading.Thread(target=lambda: [counter.increment() for _ in range(1000)])
    dec = threading.Thread(target=lambda: [counter.decrement() for _ in range(500)])
    inc.start(); dec.start()
    inc.join();  dec.join()
    print(f"  Counter: {counter.value}")  # always 500

    # RLock — reentrant lock (same thread can re-acquire)
    rlock = threading.RLock()
    def outer():
        with rlock:
            inner()  # safe because RLock is reentrant
    def inner():
        with rlock:
            print("  RLock: inner acquired by same thread")
    outer()

    # trylock equivalent — acquire(blocking=False)
    lock = threading.Lock()
    got = lock.acquire(blocking=False)  # returns True if acquired, False otherwise
    if got:
        print("  Got lock immediately")
        lock.release()

    # timed acquire
    lock2 = threading.Lock()
    lock2.acquire()
    got2 = lock2.acquire(timeout=0.05)  # False — already held
    print(f"  Timed acquire: {got2}")
    lock2.release()


# ─────────────────────────────────────────────────────────────────────────────
# 3. Condition — wait / notify (monitor pattern)
#    condition.wait()         — releases lock and suspends; reacquires on wakeup
#    condition.notify()       — wakes one waiting thread
#    condition.notify_all()   — wakes all waiting threads (safer default)
#    Always call inside 'with condition:' block.
#    Always re-check the predicate in a while loop (spurious wakeups).
# ─────────────────────────────────────────────────────────────────────────────
class SharedBuffer:
    def __init__(self, capacity):
        self._buf = []
        self._capacity = capacity
        self._cond = threading.Condition()

    def produce(self, item):
        with self._cond:
            while len(self._buf) == self._capacity:
                self._cond.wait()  # release lock; suspend
            self._buf.append(item)
            print(f"  Produced: {item}")
            self._cond.notify_all()

    def consume(self):
        with self._cond:
            while not self._buf:
                self._cond.wait()
            item = self._buf.pop(0)
            print(f"  Consumed: {item}")
            self._cond.notify_all()
            return item


def condition_demo():
    buf = SharedBuffer(2)

    def producer():
        for i in range(5):
            buf.produce(i)

    def consumer():
        for _ in range(5):
            buf.consume()

    p = threading.Thread(target=producer)
    c = threading.Thread(target=consumer)
    p.start(); c.start()
    p.join();  c.join()


# ─────────────────────────────────────────────────────────────────────────────
# 4. Semaphore / BoundedSemaphore
#    Semaphore(n)        — allows up to n threads in the critical section at once
#    BoundedSemaphore(n) — same, but raises ValueError if released more than acquired
#    Use for: connection pools, rate limiting, resource guards
# ─────────────────────────────────────────────────────────────────────────────
def semaphore_demo():
    sem = threading.Semaphore(3)  # at most 3 concurrent workers

    def worker(i):
        with sem:
            print(f"  Worker {i} inside (active ≤ 3)")
            time.sleep(0.05)

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(7)]
    for t in threads: t.start()
    for t in threads: t.join()


# ─────────────────────────────────────────────────────────────────────────────
# 5. Event — one-time signal from one thread to others
#    event.set()       — set flag; all waiters unblock
#    event.clear()     — reset flag
#    event.wait()      — block until flag is set (optional timeout)
#    event.is_set()    — check without blocking
#    Use for: "start gun" (all workers wait until main signals go),
#             or "shutdown" flag (cleaner than a raw boolean).
# ─────────────────────────────────────────────────────────────────────────────
def event_demo():
    start_event = threading.Event()

    def worker(i):
        start_event.wait()  # block until signalled
        print(f"  Worker {i} started after event")

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(3)]
    for t in threads: t.start()
    time.sleep(0.05)
    print("  Firing start event")
    start_event.set()  # all workers unblock simultaneously
    for t in threads: t.join()


# ─────────────────────────────────────────────────────────────────────────────
# 6. Barrier — N threads meet, then all proceed together (like CyclicBarrier)
#    threading.Barrier(n, action=None)
#    barrier.wait() — blocks until all n parties have called wait()
#    action         — optional callable run once per phase by the last thread to arrive
# ─────────────────────────────────────────────────────────────────────────────
def barrier_demo():
    barrier = threading.Barrier(3, action=lambda: print("  --- All at barrier ---"))

    def worker(i):
        print(f"  Thread {i} phase 1")
        barrier.wait()
        print(f"  Thread {i} phase 2")

    threads = [threading.Thread(target=worker, args=(i,)) for i in range(3)]
    for t in threads: t.start()
    for t in threads: t.join()


# ─────────────────────────────────────────────────────────────────────────────
# 7. queue.Queue — thread-safe producer-consumer buffer
#    Queue(maxsize)     — FIFO; blocks put() when full, get() when empty
#    LifoQueue          — stack (LIFO)
#    PriorityQueue      — min-heap; items must be comparable (use (priority, data))
#    put(item)          — blocks if full (use put_nowait() or timeout= to avoid)
#    get()              — blocks if empty
#    task_done()        — signal that a consumed item is fully processed
#    join()             — blocks until all items have been task_done()'d
# ─────────────────────────────────────────────────────────────────────────────
def queue_demo():
    q = queue.Queue(maxsize=3)

    def producer():
        for i in range(6):
            q.put(i)
            print(f"  Put: {i}")

    def consumer():
        for _ in range(6):
            item = q.get()
            print(f"  Got: {item}")
            q.task_done()

    p = threading.Thread(target=producer)
    c = threading.Thread(target=consumer)
    p.start(); c.start()
    q.join()   # wait until all task_done() called
    p.join(); c.join()

    # PriorityQueue — (priority, value); lower number = higher priority
    pq = queue.PriorityQueue()
    pq.put((3, "low"))
    pq.put((1, "high"))
    pq.put((2, "medium"))
    while not pq.empty():
        print(f"  PQ: {pq.get()}")


# ─────────────────────────────────────────────────────────────────────────────
# 8. ThreadPoolExecutor — high-level thread pool
#    submit(fn, *args)  — returns Future; non-blocking
#    map(fn, iterable)  — like built-in map but concurrent; results in order
#    as_completed(futs) — yields futures as they complete (order not guaranteed)
#    shutdown(wait=True)— graceful shutdown after current tasks finish
#    Use as context manager for automatic shutdown.
# ─────────────────────────────────────────────────────────────────────────────
def thread_pool_demo():
    def task(n):
        time.sleep(0.05)
        return n * n

    with ThreadPoolExecutor(max_workers=4) as pool:
        # submit + future
        fut = pool.submit(task, 5)
        print(f"  Future result: {fut.result()}")  # 25

        # map — preserves input order
        results = list(pool.map(task, range(5)))
        print(f"  Map results: {results}")  # [0, 1, 4, 9, 16]

        # as_completed — process whichever finishes first
        futures = {pool.submit(task, i): i for i in range(5)}
        for f in as_completed(futures):
            print(f"  as_completed: {futures[f]}² = {f.result()}")


# ─────────────────────────────────────────────────────────────────────────────
# 9. ProcessPoolExecutor — bypass GIL for CPU-bound tasks
#    API mirrors ThreadPoolExecutor; each worker is a separate OS process.
#    Arguments and return values must be picklable.
#    Overhead: process spawn + IPC; only worth it for heavy computation.
#    MUST be inside if __name__ == '__main__' on Windows (spawn start method).
# ─────────────────────────────────────────────────────────────────────────────
def cpu_task(n):  # must be top-level (picklable) for multiprocessing
    return sum(range(n))

def process_pool_demo():
    with ProcessPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(cpu_task, [10**6, 10**6, 10**6]))
        print(f"  Process pool results: {results}")


# ─────────────────────────────────────────────────────────────────────────────
# 10. asyncio — single-threaded cooperative concurrency (event loop)
#     async def  — defines a coroutine
#     await      — suspends coroutine until awaitable completes; yields control
#     asyncio.run(coro)          — entry point; creates and runs event loop
#     asyncio.create_task(coro)  — schedule coroutine concurrently (like submit)
#     asyncio.gather(*coros)     — run all concurrently, return all results
#     asyncio.sleep(n)           — yields control without blocking the event loop
#     Use for: async I/O (HTTP, DB, sockets) — NOT for CPU-bound work
# ─────────────────────────────────────────────────────────────────────────────
async def fetch(url, delay):
    await asyncio.sleep(delay)          # simulates I/O wait; non-blocking
    return f"Response from {url}"

async def async_demo():
    # sequential — slow (runs one after another)
    r1 = await fetch("api/users", 0.1)
    print(f"  Sequential: {r1}")

    # concurrent with gather — fast (all run simultaneously)
    results = await asyncio.gather(
        fetch("api/users",    0.1),
        fetch("api/orders",   0.1),
        fetch("api/products", 0.1),
    )
    for r in results:
        print(f"  Gather: {r}")

    # create_task — schedule without waiting immediately
    task1 = asyncio.create_task(fetch("api/a", 0.1))
    task2 = asyncio.create_task(fetch("api/b", 0.05))
    print(f"  Task2 (faster): {await task2}")
    print(f"  Task1 (slower): {await task1}")

    # timeout
    try:
        await asyncio.wait_for(asyncio.sleep(10), timeout=0.05)
    except asyncio.TimeoutError:
        print("  Timed out as expected")


# ─────────────────────────────────────────────────────────────────────────────
# 11. Common Gotchas
#
#  GIL         — only one thread runs Python bytecode at a time; use
#                multiprocessing or asyncio for true parallelism
#
#  Mutable     — default args evaluated once: def f(x=[]) shares the list
#  defaults      across all calls; use None + if x is None: x = []
#
#  Deadlock    — two threads each hold a lock the other needs
#               Fix: always acquire locks in the same order
#
#  Race cond   — counter += 1 is NOT atomic in Python (read-modify-write)
#               Fix: use Lock, or threading.local, or queue
#
#  Daemon leak — daemon threads die abruptly; don't use for I/O that must flush
# ─────────────────────────────────────────────────────────────────────────────


if __name__ == "__main__":
    print("=== Thread Creation ===")
    thread_creation_demo()

    print("\n=== Lock / RLock ===")
    lock_demo()

    print("\n=== Condition (wait/notify) ===")
    condition_demo()

    print("\n=== Semaphore ===")
    semaphore_demo()

    print("\n=== Event ===")
    event_demo()

    print("\n=== Barrier ===")
    barrier_demo()

    print("\n=== Queue ===")
    queue_demo()

    print("\n=== ThreadPoolExecutor ===")
    thread_pool_demo()

    print("\n=== ProcessPoolExecutor ===")
    process_pool_demo()

    print("\n=== asyncio ===")
    asyncio.run(async_demo())
