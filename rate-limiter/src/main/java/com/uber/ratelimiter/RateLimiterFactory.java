package com.uber.ratelimiter;

/**
 * Factory for creating RateLimiter instances by algorithm type.
 *
 * Centralises construction so callers are decoupled from concrete implementations.
 * New algorithms can be added here without touching client code.
 */
public class RateLimiterFactory {

    /** Supported rate-limiting algorithms. */
    public enum Algorithm {
        /** Allows bursts up to capacity; refills at a fixed token rate over time. */
        TOKEN_BUCKET,
        /** Exact sliding window log; no burst beyond capacity within any window interval. */
        SLIDING_WINDOW
    }

    private RateLimiterFactory() {
        // utility class — no instances
    }

    /**
     * Creates a new, unconfigured RateLimiter for the given algorithm.
     * Call {@link RateLimiter#configure(String, RateLimiterConfig)} before use.
     *
     * @param algo the desired algorithm
     * @return a fresh RateLimiter instance
     * @throws IllegalArgumentException for unknown algorithm values
     */
    public static RateLimiter create(Algorithm algo) {
        switch (algo) {
            case TOKEN_BUCKET:
                return new TokenBucketRateLimiter();
            case SLIDING_WINDOW:
                return new SlidingWindowRateLimiter();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algo);
        }
    }
}
