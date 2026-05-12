package com.uber.ratelimiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for TokenBucketRateLimiter and SlidingWindowRateLimiter.
 *
 * Tests are grouped into:
 *   - Token Bucket tests
 *   - Sliding Window tests
 *   - General / cross-cutting tests
 */
class RateLimiterTest {

    // =========================================================================
    // Token Bucket tests
    // =========================================================================

    @Test
    @DisplayName("TokenBucket: allows exactly capacity requests in rapid succession")
    void tokenBucket_allowsUpToCapacity() {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        limiter.configure("client", new RateLimiterConfig(5, 1, 1000));

        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.allowRequest("client"),
                    "Request " + (i + 1) + " should be allowed (within capacity)");
        }
    }

    @Test
    @DisplayName("TokenBucket: blocks requests once bucket is empty")
    void tokenBucket_blocksWhenEmpty() {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        // capacity=2: first two allowed, third blocked
        limiter.configure("client", new RateLimiterConfig(2, 1, 1000));

        assertTrue(limiter.allowRequest("client"),  "1st request should be allowed");
        assertTrue(limiter.allowRequest("client"),  "2nd request should be allowed");
        assertFalse(limiter.allowRequest("client"), "3rd request should be blocked (bucket empty)");
    }

    @Test
    @DisplayName("TokenBucket: refills tokens over time")
    void tokenBucket_refillsOverTime() throws InterruptedException {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        // capacity=1, refillRate=1/s → 1 token refilled per second
        limiter.configure("client", new RateLimiterConfig(1, 1, 1000));

        assertTrue(limiter.allowRequest("client"),  "Initial request should be allowed");
        assertFalse(limiter.allowRequest("client"), "Immediate second request should be blocked");

        // Wait slightly more than 1 second so at least 1 full token refills
        Thread.sleep(1100);

        assertTrue(limiter.allowRequest("client"),
                "Request after 1.1s sleep should be allowed (token refilled)");
    }

    // =========================================================================
    // Sliding Window tests
    // =========================================================================

    @Test
    @DisplayName("SlidingWindow: allows exactly capacity requests in rapid succession")
    void slidingWindow_allowsUpToCapacity() {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.SLIDING_WINDOW);
        limiter.configure("client", new RateLimiterConfig(3, 0, 1000));

        for (int i = 0; i < 3; i++) {
            assertTrue(limiter.allowRequest("client"),
                    "Request " + (i + 1) + " should be allowed (within capacity)");
        }
    }

    @Test
    @DisplayName("SlidingWindow: blocks requests when window is full")
    void slidingWindow_blocksWhenWindowFull() {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.SLIDING_WINDOW);
        // capacity=2 within 1000ms window
        limiter.configure("client", new RateLimiterConfig(2, 0, 1000));

        assertTrue(limiter.allowRequest("client"),  "1st request should be allowed");
        assertTrue(limiter.allowRequest("client"),  "2nd request should be allowed");
        assertFalse(limiter.allowRequest("client"), "3rd request should be blocked (window full)");
    }

    @Test
    @DisplayName("SlidingWindow: allows requests again after window expires")
    void slidingWindow_allowsAfterWindowExpires() throws InterruptedException {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.SLIDING_WINDOW);
        // capacity=1, window=500ms
        limiter.configure("client", new RateLimiterConfig(1, 0, 500));

        assertTrue(limiter.allowRequest("client"),  "1st request should be allowed");
        assertFalse(limiter.allowRequest("client"), "Immediate 2nd request should be blocked");

        // Wait for window to fully expire
        Thread.sleep(600);

        assertTrue(limiter.allowRequest("client"),
                "Request after 600ms should be allowed (window expired)");
    }

    // =========================================================================
    // General / cross-cutting tests
    // =========================================================================

    @Test
    @DisplayName("Unconfigured client is blocked by default (fail closed)")
    void unconfiguredClient_blocked() {
        RateLimiter tokenLimiter  = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        RateLimiter slidingLimiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.SLIDING_WINDOW);

        assertFalse(tokenLimiter.allowRequest("ghost"),
                "TokenBucket: unconfigured client should be blocked");
        assertFalse(slidingLimiter.allowRequest("ghost"),
                "SlidingWindow: unconfigured client should be blocked");
    }

    @Test
    @DisplayName("Multiple clients have isolated, independent buckets")
    void multipleClients_isolatedBuckets() {
        RateLimiter limiter = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        RateLimiterConfig cap2 = new RateLimiterConfig(2, 1, 1000);
        limiter.configure("client-1", cap2);
        limiter.configure("client-2", cap2);

        // Exhaust client-1
        assertTrue(limiter.allowRequest("client-1"),  "client-1 req 1: allowed");
        assertTrue(limiter.allowRequest("client-1"),  "client-1 req 2: allowed");
        assertFalse(limiter.allowRequest("client-1"), "client-1 req 3: blocked (exhausted)");

        // client-2 must be completely unaffected
        assertTrue(limiter.allowRequest("client-2"),  "client-2 req 1: should still be allowed");
        assertTrue(limiter.allowRequest("client-2"),  "client-2 req 2: should still be allowed");
    }
}
