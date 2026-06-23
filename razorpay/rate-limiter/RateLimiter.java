// Companies: Razorpay
// Per-user sliding-window rate limiter: allows at most maxRequests calls within
// any rolling windowMs milliseconds. Domain-essential for fintech APIs.

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Per-user sliding-window rate limiter.
 *
 * <p>Each user maintains a {@link Deque} of timestamps of their recent requests.
 * On every {@link #isAllowed} call the deque is purged of entries older than
 * {@code windowMs} before the count is checked — this is "lazy sliding window"
 * expiry with O(1) amortised cost per request.
 *
 * <p>Why sliding window over token bucket?
 * <ul>
 *   <li>Token bucket allows bursts up to bucket capacity at any instant.</li>
 *   <li>Sliding window gives a hard guarantee: no more than {@code maxRequests}
 *       in any {@code windowMs}-wide interval, regardless of timing.</li>
 * </ul>
 *
 * <p>Thread safety: a per-user {@link ReentrantLock} guards each user's deque,
 * allowing different users to be served concurrently without contention.
 */
public class RateLimiter {

    /** Maximum requests allowed in the rolling window. */
    private final int maxRequests;

    /** Rolling window duration in milliseconds. */
    private final long windowMs;

    /** userId → timestamps of their recent requests within the window. */
    private final Map<String, Deque<Long>> userWindows = new HashMap<>();

    /** userId → per-user lock (finer-grained than a single global lock). */
    private final Map<String, ReentrantLock> userLocks = new HashMap<>();

    /** Guards the userWindows and userLocks maps themselves. */
    private final ReentrantLock mapLock = new ReentrantLock();

    /**
     * Constructs a RateLimiter.
     *
     * @param maxRequests maximum requests allowed in any rolling window
     * @param windowMs    rolling window duration in milliseconds
     */
    public RateLimiter(int maxRequests, long windowMs) {
        if (maxRequests <= 0) throw new IllegalArgumentException("maxRequests must be > 0");
        if (windowMs <= 0) throw new IllegalArgumentException("windowMs must be > 0");
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Records a request at the current wall-clock time and returns whether
     * it is within the rate limit for the given user.
     *
     * <p>Slides the window by discarding all timestamps older than
     * {@code now - windowMs} before checking the count.
     *
     * @param userId non-null user identifier
     * @return {@code true} if the request is allowed; {@code false} if throttled
     */
    public boolean isAllowed(String userId) {
        return isAllowed(userId, System.currentTimeMillis());
    }

    /**
     * Records a request at a specified timestamp (useful for deterministic tests).
     *
     * @param userId    non-null user identifier
     * @param timestamp request timestamp in milliseconds
     * @return {@code true} if allowed; {@code false} if throttled
     */
    public boolean isAllowed(String userId, long timestamp) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("userId cannot be blank");

        ReentrantLock lock = getOrCreateLock(userId);
        lock.lock();
        try {
            Deque<Long> window = userWindows.computeIfAbsent(userId, k -> new ArrayDeque<>());
            long cutoff = timestamp - windowMs;

            // Slide: remove all timestamps older than the window
            while (!window.isEmpty() && window.peekFirst() <= cutoff) {
                window.pollFirst();
            }

            if (window.size() < maxRequests) {
                window.addLast(timestamp);
                return true;
            }
            return false; // rate limited
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the number of requests made by {@code userId} still within the
     * current window (without recording a new request).
     *
     * @param userId user identifier
     * @return current request count in the active window
     */
    public int currentCount(String userId) {
        return currentCount(userId, System.currentTimeMillis());
    }

    /**
     * Returns the current in-window count at a specific timestamp (for tests).
     *
     * @param userId    user identifier
     * @param timestamp reference time in milliseconds
     * @return number of requests in the active window
     */
    public int currentCount(String userId, long timestamp) {
        ReentrantLock lock = getOrCreateLock(userId);
        lock.lock();
        try {
            Deque<Long> window = userWindows.getOrDefault(userId, new ArrayDeque<>());
            long cutoff = timestamp - windowMs;
            int count = 0;
            for (long t : window) {
                if (t > cutoff) count++;
            }
            return count;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Resets all request history for a user (e.g., after authentication refresh).
     *
     * @param userId user identifier to reset
     */
    public void reset(String userId) {
        ReentrantLock lock = getOrCreateLock(userId);
        lock.lock();
        try {
            userWindows.remove(userId);
        } finally {
            lock.unlock();
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /** Returns the per-user lock, creating it if absent (under mapLock). */
    private ReentrantLock getOrCreateLock(String userId) {
        mapLock.lock();
        try {
            return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        } finally {
            mapLock.unlock();
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────

    public static void main(String[] args) throws InterruptedException {
        // Allow 3 requests per 1000ms window
        RateLimiter limiter = new RateLimiter(3, 1000);

        System.out.println("=== Basic allow/deny (3 req / 1000ms window) ===");
        long t = 0;
        System.out.println(limiter.isAllowed("alice", t));        // Expected: true  (1st)
        System.out.println(limiter.isAllowed("alice", t + 100));  // Expected: true  (2nd)
        System.out.println(limiter.isAllowed("alice", t + 200));  // Expected: true  (3rd)
        System.out.println(limiter.isAllowed("alice", t + 300));  // Expected: false (4th, throttled)

        System.out.println("\n=== Window slides — old requests expire ===");
        // At t=1100 the first request (t=0) falls out of the 1000ms window
        System.out.println(limiter.isAllowed("alice", t + 1100)); // Expected: true  (t=0 expired)
        System.out.println(limiter.isAllowed("alice", t + 1200)); // Expected: true  (cutoff=200, only t=1100 remains)

        System.out.println("\n=== Different users are independent ===");
        System.out.println(limiter.isAllowed("bob", t));          // Expected: true  (bob fresh)
        System.out.println(limiter.isAllowed("bob", t + 100));    // Expected: true
        System.out.println(limiter.isAllowed("bob", t + 200));    // Expected: true
        System.out.println(limiter.isAllowed("bob", t + 300));    // Expected: false (bob throttled)

        System.out.println("\n=== Current count for bob at t=300 ===");
        System.out.println(limiter.currentCount("bob", t + 300)); // Expected: 3

        System.out.println("\n=== Reset alice, she can make requests again ===");
        limiter.reset("alice");
        System.out.println(limiter.isAllowed("alice", t + 300));  // Expected: true  (reset)
        System.out.println(limiter.isAllowed("alice", t + 400));  // Expected: true
        System.out.println(limiter.isAllowed("alice", t + 500));  // Expected: true
        System.out.println(limiter.isAllowed("alice", t + 600));  // Expected: false

        System.out.println("\n=== Invalid userId throws ===");
        try {
            limiter.isAllowed("", t);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage()); // Expected: userId cannot be blank
        }
    }
}
