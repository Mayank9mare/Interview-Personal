package com.uber.ratelimiter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding Window Log Rate Limiter
 *
 * Algorithm:
 *   - Each client maintains a log (deque) of timestamps of past requests.
 *   - On each request: evict timestamps older than (now - windowSizeMs).
 *   - If the remaining log size < capacity, the request is allowed and its timestamp is appended.
 *   - Otherwise the request is rejected.
 *
 * Characteristics:
 *   - Exact rate limiting — no boundary burst artefact of fixed windows.
 *   - Memory usage is O(capacity) per client (log never exceeds `capacity` entries).
 *   - O(k) eviction per request where k = number of expired entries (amortized O(1)).
 *
 * Thread safety:
 *   - ConcurrentHashMap for the outer map.
 *   - Each WindowState synchronizes on itself for the allow() critical section,
 *     preventing races on the shared timestamps deque.
 */
public class SlidingWindowRateLimiter implements RateLimiter {

    private final Map<String, WindowState> windows = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Inner class: per-client sliding window state
    // -------------------------------------------------------------------------

    private static class WindowState {
        // Timestamps of requests inside the current window; oldest at front
        final Deque<Long> timestamps = new ArrayDeque<>();
        final RateLimiterConfig config;

        WindowState(RateLimiterConfig config) {
            this.config = config;
        }

        /**
         * Evicts stale timestamps, then decides whether to admit the request.
         * Synchronized on the WindowState instance — one thread at a time per client.
         *
         * @return true if the request is within the rate limit, false otherwise
         */
        synchronized boolean allow() {
            long now = System.currentTimeMillis();
            long windowStart = now - config.getWindowSizeMs();

            // Remove timestamps that fall outside the current window
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() < config.getCapacity()) {
                timestamps.addLast(now);  // record this request
                return true;              // request allowed
            }
            return false;                 // window full, request rejected
        }
    }

    // -------------------------------------------------------------------------
    // RateLimiter implementation
    // -------------------------------------------------------------------------

    @Override
    public void configure(String clientId, RateLimiterConfig config) {
        windows.put(clientId, new WindowState(config));
    }

    @Override
    public boolean allowRequest(String clientId) {
        WindowState state = windows.get(clientId);
        if (state == null) {
            // Unknown client — fail closed
            return false;
        }
        return state.allow();
    }
}
