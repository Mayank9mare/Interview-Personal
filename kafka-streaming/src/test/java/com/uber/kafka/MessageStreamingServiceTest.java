package com.uber.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MessageStreamingServiceTest {
    private MessageStreamingService svc;

    @BeforeEach
    void setUp() { svc = new MessageStreamingService(); }

    @Test
    void createTopic_returnsTrue() {
        assertTrue(svc.createTopic("payments", 2));
    }

    @Test
    void createTopic_duplicateReturnsFalse() {
        svc.createTopic("payments", 2);
        assertFalse(svc.createTopic("payments", 3));
    }

    @Test
    void publish_returnsCorrectIdentifier() {
        svc.createTopic("payments", 2);
        assertEquals("p0:0", svc.publish("payments", 0, "pay#A"));
        assertEquals("p0:1", svc.publish("payments", 0, "pay#B"));
        assertEquals("p1:0", svc.publish("payments", 1, "pay#C"));
    }

    @Test
    void consume_returnsMessagesInOrder() {
        svc.createTopic("payments", 2);
        svc.publish("payments", 0, "pay#A");
        svc.publish("payments", 0, "pay#B");
        svc.publish("payments", 1, "pay#C");
        assertEquals(List.of("pay#A", "pay#B"), svc.consume("payments", "c1", 0, 10));
        assertEquals(List.of(), svc.consume("payments", "c1", 0, 10));
        assertEquals(List.of("pay#C"), svc.consume("payments", "c1", 1, 10));
    }

    @Test
    void consume_independentConsumerCursors() {
        svc.createTopic("orders", 1);
        svc.publish("orders", 0, "o1");
        svc.publish("orders", 0, "o2");
        svc.consume("orders", "c1", 0, 1); // c1 reads o1
        assertEquals(List.of("o1", "o2"), svc.consume("orders", "c2", 0, 10));
    }

    @Test
    void consume_maxMessagesLimitsBatch() {
        svc.createTopic("metrics", 1);
        svc.publish("metrics", 0, "cpu=90");
        svc.publish("metrics", 0, "mem=70");
        svc.publish("metrics", 0, "disk=40");
        assertEquals(List.of("cpu=90", "mem=70"), svc.consume("metrics", "c1", 0, 2));
        assertEquals(List.of("disk=40"), svc.consume("metrics", "c1", 0, 2));
        assertEquals(List.of(), svc.consume("metrics", "c1", 0, 10));
    }

    @Test
    void consume_emptyPartitionReturnsEmpty() {
        svc.createTopic("empty", 1);
        assertEquals(List.of(), svc.consume("empty", "c1", 0, 10));
    }

    @Test
    void consume_newConsumerStartsFromBeginning() {
        svc.createTopic("t", 1);
        svc.publish("t", 0, "m1");
        svc.publish("t", 0, "m2");
        svc.consume("t", "c1", 0, 1);
        // c2 starts from beginning
        assertEquals(List.of("m1", "m2"), svc.consume("t", "c2", 0, 10));
    }

    @Test
    void publish_offsetIncreasesPerPartition() {
        svc.createTopic("t", 3);
        assertEquals("p0:0", svc.publish("t", 0, "a"));
        assertEquals("p1:0", svc.publish("t", 1, "b"));
        assertEquals("p0:1", svc.publish("t", 0, "c"));
        assertEquals("p2:0", svc.publish("t", 2, "d"));
    }
}
