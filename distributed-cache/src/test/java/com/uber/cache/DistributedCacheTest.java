package com.uber.cache;

import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

class DistributedCacheTest {
    static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @Test
    void putAndGetRoutesThroughConsistentHashRing() {
        DistributedCache cache = new DistributedCache(10, 10, Clock.systemUTC());
        cache.addNode("n1");
        cache.addNode("n2");

        cache.put("city", "bangalore", 60_000);

        assertEquals("bangalore", cache.get("city"));
        assertNotNull(cache.ownerOf("city"));
    }

    @Test
    void expiredValueReturnsNull() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        DistributedCache cache = new DistributedCache(3, 10, clock);
        cache.addNode("n1");

        cache.put("k", "v", 100);
        assertEquals("v", cache.get("k"));

        clock.advanceMillis(101);

        assertNull(cache.get("k"));
    }
}
