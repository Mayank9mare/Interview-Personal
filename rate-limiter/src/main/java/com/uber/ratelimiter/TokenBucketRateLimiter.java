package com.uber.ratelimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token Bucket Rate Limiter
 *
 * Algorithm:
 *   - Each client has a bucket that holds up to `capacity` tokens.
 *   - Tokens are added continuously at `refillRate` tokens/second (lazy refill on each request).
 *   - Each allowed request consumes 1 token.
 *   - If the bucket is empty (< 1 token), the request is rejected.
 *
 * Characteristics:
 *   - Allows short bursts up to `capacity` tokens.
 *   - Smooths traffic over time via the refill rate.
 *   - O(1) time per request; O(clients) space.
 *
 * Thread safety:
 *   - ConcurrentHashMap for the outer map (safe concurrent reads/writes per key).
 *   - Each Bucket synchronizes on itself for the consume() critical section,
 *     preventing races on `tokens` and `lastRefillTime`.
 */
public class TokenBucketRateLimiter implements RateLimiter {

    // One Bucket per client; ConcurrentHashMap avoids locking on reads across different clients
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Inner class: per-client bucket state
    // -------------------------------------------------------------------------

    private static class Bucket {
        double tokens;          // current token count (double for fractional accumulation)
        long lastRefillTime;    // epoch-ms when tokens were last topped up
        final RateLimiterConfig config;

        Bucket(RateLimiterConfig config) {
            this.config = config;
            this.tokens = config.getCapacity();  // start full
            this.lastRefillTime = System.currentTimeMillis();
        }

        /**
         * Refills tokens based on elapsed time, then tries to consume one.
         * Synchronized on the Bucket instance — only one thread can consume at a time per client.
         *
         * @return true if a token was consumed (request allowed), false otherwise
         */
        synchronized boolean consume() {
            long now = System.currentTimeMillis();
            double elapsedSeconds = (now - lastRefillTime) / 1000.0;

            // Add tokens proportional to elapsed time, but never exceed capacity
            tokens = Math.min(config.getCapacity(),
                              tokens + elapsedSeconds * config.getRefillRate());
            lastRefillTime = now;

            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;   // request allowed
            }
            return false;      // bucket empty, request rejected
        }
    }

    // -------------------------------------------------------------------------
    // RateLimiter implementation
    // -------------------------------------------------------------------------

    @Override
    public void configure(String clientId, RateLimiterConfig config) {
        // Replacing the bucket resets the client's state with the new config
        buckets.put(clientId, new Bucket(config));
    }

    @Override
    public boolean allowRequest(String clientId) {
        Bucket bucket = buckets.get(clientId);
        if (bucket == null) {
            // Unknown client — fail closed (deny by default)
            return false;
        }
        return bucket.consume();
    }
}
