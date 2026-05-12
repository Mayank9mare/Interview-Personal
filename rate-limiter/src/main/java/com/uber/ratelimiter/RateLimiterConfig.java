package com.uber.ratelimiter;

/**
 * Immutable configuration for a rate limiter.
 *
 * - capacity:     max tokens in the bucket (Token Bucket) OR max requests in the window (Sliding Window)
 * - refillRate:   tokens added per second (Token Bucket only)
 * - windowSizeMs: length of the sliding window in milliseconds (Sliding Window only)
 *
 * Immutability ensures configs can be shared safely across threads without defensive copying.
 */
public class RateLimiterConfig {

    private final int capacity;       // max tokens / max requests in window
    private final int refillRate;     // tokens per second (for token bucket)
    private final long windowSizeMs;  // window size in ms (for sliding window)

    public RateLimiterConfig(int capacity, int refillRate, long windowSizeMs) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (refillRate < 0) throw new IllegalArgumentException("refillRate must be >= 0");
        if (windowSizeMs <= 0) throw new IllegalArgumentException("windowSizeMs must be > 0");
        this.capacity = capacity;
        this.refillRate = refillRate;
        this.windowSizeMs = windowSizeMs;
    }

    /** Maximum number of tokens the bucket can hold, or max requests allowed in one window. */
    public int getCapacity() {
        return capacity;
    }

    /** Rate at which tokens are added to the bucket, in tokens per second (Token Bucket only). */
    public int getRefillRate() {
        return refillRate;
    }

    /** Duration of the sliding window in milliseconds (Sliding Window only). */
    public long getWindowSizeMs() {
        return windowSizeMs;
    }

    @Override
    public String toString() {
        return "RateLimiterConfig{capacity=" + capacity
                + ", refillRate=" + refillRate + "/s"
                + ", windowSizeMs=" + windowSizeMs + "ms}";
    }
}
