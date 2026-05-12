package com.uber.kafka;

import java.util.*;

public class MessageStreamingService {
    private final Map<String, List<List<String>>> topics = new HashMap<>();
    private final Map<String, Integer> cursors = new HashMap<>();

    public boolean createTopic(String topicName, int partitionCount) {
        if (topics.containsKey(topicName)) return false;
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < partitionCount; i++) partitions.add(new ArrayList<>());
        topics.put(topicName, partitions);
        return true;
    }

    public String publish(String topicName, int partitionId, String message) {
        List<String> partition = topics.get(topicName).get(partitionId);
        int offset = partition.size();
        partition.add(message);
        return "p" + partitionId + ":" + offset;
    }

    public List<String> consume(String topicName, String consumerId, int partitionId, int maxMessages) {
        String key = topicName + "|" + consumerId + "|" + partitionId;
        int cursor = cursors.getOrDefault(key, 0);
        List<String> partition = topics.get(topicName).get(partitionId);
        if (cursor >= partition.size()) return Collections.emptyList();
        int end = Math.min(cursor + maxMessages, partition.size());
        List<String> result = new ArrayList<>(partition.subList(cursor, end));
        cursors.put(key, end);
        return result;
    }
}
