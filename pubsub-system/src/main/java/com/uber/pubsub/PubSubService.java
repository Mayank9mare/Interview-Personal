package com.uber.pubsub;

import java.util.*;

/**
 * Simple in-memory publish-subscribe service.
 * <p>
 * Subscribers register interest in one or more named event types; publishers broadcast messages
 * to all matching subscribers. The service counts how many messages each subscriber has received
 * but does not actually deliver message content (message delivery is assumed to happen in a real
 * integration layer). Not thread-safe.
 */
public class PubSubService {
    /** Maps subscriber ID to the set of event types they are subscribed to. */
    private final Map<String, Set<String>> subscriberTypes = new HashMap<>();

    /** Tracks the total number of messages processed (matched) per subscriber ID. */
    private final Map<String, Integer> processedCount = new HashMap<>();

    /**
     * Registers a subscriber with the given set of event types.
     * If the subscriber already exists, their subscriptions are replaced.
     *
     * @param id         unique subscriber identifier
     * @param eventTypes list of event type names the subscriber is interested in
     */
    public void addSubscriber(String id, List<String> eventTypes) {
        subscriberTypes.put(id, new HashSet<>(eventTypes));
        processedCount.putIfAbsent(id, 0);
    }

    /**
     * Unregisters a subscriber. Subsequent messages will not be counted for this ID.
     *
     * @param id the subscriber to remove
     */
    public void removeSubscriber(String id) {
        subscriberTypes.remove(id);
    }

    /**
     * Publishes a message of the given event type to all subscribers interested in that type,
     * incrementing each matching subscriber's processed-message count.
     *
     * @param eventType the type of the event being published
     * @param message   the message payload (informational; not stored)
     */
    public void sendMessage(String eventType, String message) {
        for (Map.Entry<String, Set<String>> entry : subscriberTypes.entrySet()) {
            if (entry.getValue().contains(eventType)) {
                processedCount.merge(entry.getKey(), 1, Integer::sum);
            }
        }
    }

    /**
     * Returns the total number of messages that have matched this subscriber since registration.
     *
     * @param id the subscriber to query
     * @return message count, or {@code 0} if the subscriber is unknown
     */
    public int countProcessedMessages(String id) {
        return processedCount.getOrDefault(id, 0);
    }
}
