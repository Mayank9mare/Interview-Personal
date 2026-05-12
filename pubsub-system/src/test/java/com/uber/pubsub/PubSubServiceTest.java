package com.uber.pubsub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PubSubServiceTest {
    private PubSubService ps;

    @BeforeEach
    void setUp() { ps = new PubSubService(); }

    @Test
    void sendMessage_deliveredToMatchingSubscriber() {
        ps.addSubscriber("s1", List.of("click", "scroll"));
        ps.sendMessage("click", "btn-clicked");
        assertEquals(1, ps.countProcessedMessages("s1"));
    }

    @Test
    void sendMessage_notDeliveredWhenTypeMismatch() {
        ps.addSubscriber("s1", List.of("scroll"));
        ps.sendMessage("click", "btn-clicked");
        assertEquals(0, ps.countProcessedMessages("s1"));
    }

    @Test
    void sendMessage_deliveredToMultipleSubscribers() {
        ps.addSubscriber("s1", List.of("click"));
        ps.addSubscriber("s2", List.of("click", "scroll"));
        ps.sendMessage("click", "msg");
        assertEquals(1, ps.countProcessedMessages("s1"));
        assertEquals(1, ps.countProcessedMessages("s2"));
    }

    @Test
    void removeSubscriber_stopsDelivery() {
        ps.addSubscriber("s1", List.of("click"));
        ps.sendMessage("click", "msg1");
        ps.removeSubscriber("s1");
        ps.sendMessage("click", "msg2");
        assertEquals(1, ps.countProcessedMessages("s1"));
    }

    @Test
    void countProcessedMessages_persistsAcrossResubscribe() {
        ps.addSubscriber("s1", List.of("click"));
        ps.sendMessage("click", "msg1");
        ps.removeSubscriber("s1");
        ps.sendMessage("click", "msg2"); // not received
        ps.addSubscriber("s1", List.of("click"));
        ps.sendMessage("click", "msg3");
        assertEquals(2, ps.countProcessedMessages("s1")); // msg1 + msg3
    }

    @Test
    void countProcessedMessages_unknownSubscriberReturnsZero() {
        assertEquals(0, ps.countProcessedMessages("unknown"));
    }

    @Test
    void sendMessage_noSubscribersNoError() {
        ps.sendMessage("click", "msg"); // should not throw
    }

    @Test
    void addSubscriber_multipleEventTypes() {
        ps.addSubscriber("s1", List.of("a", "b", "c"));
        ps.sendMessage("a", "1");
        ps.sendMessage("b", "2");
        ps.sendMessage("c", "3");
        ps.sendMessage("d", "4");
        assertEquals(3, ps.countProcessedMessages("s1"));
    }
}
