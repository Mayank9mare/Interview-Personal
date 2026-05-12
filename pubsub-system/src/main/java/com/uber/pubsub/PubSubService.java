package com.uber.pubsub;

import java.util.*;

public class PubSubService {
    private final Map<String, Set<String>> subscriberTypes = new HashMap<>();
    private final Map<String, Integer> processedCount = new HashMap<>();

    public void addSubscriber(String id, List<String> eventTypes) {
        subscriberTypes.put(id, new HashSet<>(eventTypes));
        processedCount.putIfAbsent(id, 0);
    }

    public void removeSubscriber(String id) {
        subscriberTypes.remove(id);
    }

    public void sendMessage(String eventType, String message) {
        for (Map.Entry<String, Set<String>> entry : subscriberTypes.entrySet()) {
            if (entry.getValue().contains(eventType)) {
                processedCount.merge(entry.getKey(), 1, Integer::sum);
            }
        }
    }

    public int countProcessedMessages(String id) {
        return processedCount.getOrDefault(id, 0);
    }
}
