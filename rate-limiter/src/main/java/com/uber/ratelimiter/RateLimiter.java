package com.uber.ratelimiter;

/**
 * RateLimiter interface defining the contract for all rate limiting algorithms.
 *
 * Design considerations:
 * - Per-client configuration allows different limits for different consumers (e.g., premium vs free tier)
 * - allowRequest is the hot path — must be thread-safe and low-latency
 * - Returns boolean so callers decide whether to reject, queue, or retry
 */
public interface RateLimiter {

    /**
     * Determines whether the request from the given client should be allowed.
     *
     * @param clientId unique identifier for the client (e.g., user ID, API key, IP address)
     * @return true if the request is within rate limits and should proceed, false if it should be rejected
     */
    boolean allowRequest(String clientId);

    /**
     * Configures (or reconfigures) the rate limit parameters for a specific client.
     * Calling this again for an existing client resets their state with the new config.
     *
     * @param clientId unique identifier for the client
     * @param config   rate limiting configuration (capacity, refill rate, window size)
     */
    void configure(String clientId, RateLimiterConfig config);
}
