package com.uber.ratelimiter;

/**
 * Demo runner for the Rate Limiter system.
 *
 * Shows both algorithms in action:
 *   1. Token Bucket  — burst allowance + refill over time
 *   2. Sliding Window — exact window enforcement
 */
public class App {

    public static void main(String[] args) throws InterruptedException {

        // =====================================================================
        // DEMO 1: Token Bucket (capacity=3, refill=1 token/s)
        // =====================================================================
        System.out.println("=== Token Bucket Rate Limiter (capacity=3, refill=1/s) ===");

        RateLimiter tokenBucket = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        RateLimiterConfig tbConfig = new RateLimiterConfig(3, 1, 1000);
        tokenBucket.configure("user-A", tbConfig);

        System.out.println("\n-- Sending 5 rapid requests (expect 3 allowed, 2 blocked) --");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = tokenBucket.allowRequest("user-A");
            System.out.printf("  Request %d: %s%n", i, allowed ? "ALLOWED" : "BLOCKED");
        }

        System.out.println("\n-- Waiting 2 seconds for 2 tokens to refill... --");
        Thread.sleep(2000);

        System.out.println("\n-- Sending 2 more requests (expect both allowed) --");
        for (int i = 6; i <= 7; i++) {
            boolean allowed = tokenBucket.allowRequest("user-A");
            System.out.printf("  Request %d: %s%n", i, allowed ? "ALLOWED" : "BLOCKED");
        }

        // =====================================================================
        // DEMO 2: Sliding Window (capacity=3, window=1000ms)
        // =====================================================================
        System.out.println("\n=== Sliding Window Rate Limiter (capacity=3, window=1000ms) ===");

        RateLimiter slidingWindow = RateLimiterFactory.create(RateLimiterFactory.Algorithm.SLIDING_WINDOW);
        RateLimiterConfig swConfig = new RateLimiterConfig(3, 0, 1000);
        slidingWindow.configure("user-B", swConfig);

        System.out.println("\n-- Sending 4 rapid requests (expect 3 allowed, 1 blocked) --");
        for (int i = 1; i <= 4; i++) {
            boolean allowed = slidingWindow.allowRequest("user-B");
            System.out.printf("  Request %d: %s%n", i, allowed ? "ALLOWED" : "BLOCKED");
        }

        System.out.println("\n-- Waiting 1100ms for window to expire... --");
        Thread.sleep(1100);

        System.out.println("\n-- Sending 2 more requests (expect both allowed — window reset) --");
        for (int i = 5; i <= 6; i++) {
            boolean allowed = slidingWindow.allowRequest("user-B");
            System.out.printf("  Request %d: %s%n", i, allowed ? "ALLOWED" : "BLOCKED");
        }

        // =====================================================================
        // DEMO 3: Unconfigured client is denied
        // =====================================================================
        System.out.println("\n=== Unconfigured client ===");
        boolean result = tokenBucket.allowRequest("unknown-client");
        System.out.println("  unknown-client request: " + (result ? "ALLOWED" : "BLOCKED (as expected)"));

        // =====================================================================
        // DEMO 4: Multiple isolated clients
        // =====================================================================
        System.out.println("\n=== Multiple isolated clients (Token Bucket, capacity=2) ===");
        RateLimiter multi = RateLimiterFactory.create(RateLimiterFactory.Algorithm.TOKEN_BUCKET);
        RateLimiterConfig smallCap = new RateLimiterConfig(2, 1, 1000);
        multi.configure("client-1", smallCap);
        multi.configure("client-2", smallCap);

        // Exhaust client-1
        System.out.println("  Exhausting client-1 (3 requests, cap=2):");
        for (int i = 1; i <= 3; i++) {
            System.out.printf("    client-1 req %d: %s%n", i,
                    multi.allowRequest("client-1") ? "ALLOWED" : "BLOCKED");
        }

        // client-2 should still have its full bucket
        System.out.println("  client-2 after client-1 exhausted (should still work):");
        for (int i = 1; i <= 2; i++) {
            System.out.printf("    client-2 req %d: %s%n", i,
                    multi.allowRequest("client-2") ? "ALLOWED" : "BLOCKED");
        }

        System.out.println("\nDemo complete.");
    }
}
