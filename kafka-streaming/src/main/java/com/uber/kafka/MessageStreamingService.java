package com.uber.kafka;

import java.util.*;

/**
 * Simplified Kafka-style message streaming service supporting multi-partition topics
 * and independent per-consumer cursors.
 *
 * <p>Each topic is divided into a fixed number of partitions at creation time.
 * Messages are appended to a specific partition and assigned a monotonically
 * increasing offset. Consumers track their own read position per
 * (topic, partition) pair, so multiple consumers can read the same partition
 * independently without interfering with each other.
 *
 * <p>Core data structures:
 * <ul>
 *   <li>{@code topics}: {@code HashMap<topicName, List<List<String>>>} — outer list
 *       is indexed by partitionId; inner list is the append-only message log.</li>
 *   <li>{@code cursors}: {@code HashMap<"topic|consumer|partition", offset>} — tracks
 *       each consumer's next unread offset per partition; defaults to 0.</li>
 * </ul>
 *
 * <p>Core invariant: a cursor value always equals the index of the next message
 * to be delivered — it is advanced by exactly the number of messages returned
 * on each {@link #consume} call.
 *
 * <p>Thread safety: Not thread-safe.
 */
public class MessageStreamingService {

    /**
     * All registered topics. Keyed by topic name; value is a list of partitions,
     * each partition being an ordered, append-only list of raw message strings.
     */
    private final Map<String, List<List<String>>> topics = new HashMap<>();

    /**
     * Per-consumer read cursors. Key format: {@code "topicName|consumerId|partitionId"}.
     * Value is the offset of the next message to deliver (0-based). Missing entries
     * are treated as 0 (consumer has not yet read from that partition).
     */
    private final Map<String, Integer> cursors = new HashMap<>();

    /**
     * Creates a new topic with the specified number of empty partitions.
     * Has no effect and returns {@code false} if the topic already exists.
     *
     * @param topicName      unique name for the topic
     * @param partitionCount number of partitions to create (must be &gt; 0)
     * @return {@code true} if the topic was created; {@code false} if it already existed
     */
    public boolean createTopic(String topicName, int partitionCount) {
        if (topics.containsKey(topicName)) return false;
        List<List<String>> partitions = new ArrayList<>();
        for (int i = 0; i < partitionCount; i++) partitions.add(new ArrayList<>());
        topics.put(topicName, partitions);
        return true;
    }

    /**
     * Appends a message to the specified partition of a topic.
     *
     * @param topicName   name of an existing topic
     * @param partitionId 0-based index of the target partition
     * @param message     message payload to append
     * @return offset identifier in the form {@code "p<partitionId>:<offset>"}
     *         (e.g. {@code "p2:7"} for the 8th message on partition 2)
     */
    public String publish(String topicName, int partitionId, String message) {
        List<String> partition = topics.get(topicName).get(partitionId);
        int offset = partition.size();
        partition.add(message);
        return "p" + partitionId + ":" + offset;
    }

    /**
     * Reads the next batch of messages for a consumer from a specific partition,
     * then advances the consumer's cursor by the number of messages returned.
     *
     * <p>Each (consumerId, topicName, partitionId) triple maintains its own cursor,
     * so different consumers reading the same partition receive independent streams.
     *
     * @param topicName   name of an existing topic
     * @param consumerId  unique identifier for the consuming client
     * @param partitionId 0-based index of the partition to read from
     * @param maxMessages maximum number of messages to return in one call
     * @return ordered list of up to {@code maxMessages} messages starting from
     *         the consumer's current cursor; empty list if the partition is fully consumed
     */
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
