package com.uber.hashmap;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CustomHashMapTest {
    @Test
    void example1_basicPutGetUpdateRemove() {
        CustomHashMap map = new CustomHashMap(0.25, 0.75);
        map.put("a", "one");
        map.put("bb", "two");
        assertEquals("one", map.get("a"));
        assertEquals("", map.get("x"));
        map.put("a", "ONE");
        assertEquals("ONE", map.get("a"));
        assertEquals("two", map.remove("bb"));
        assertEquals("", map.remove("bb"));
        assertEquals(1, map.size());
    }

    @Test
    void example2_bucketCollisions() {
        CustomHashMap map = new CustomHashMap(0.25, 0.75);
        // hash("c")=4, hash("g")=8; with 2 buckets: 4%2=0, 8%2=0 → same bucket
        // After 2nd put LF=1.0>0.75 → grows to 4; 4%4=0, 8%4=0 → still same bucket 0
        map.put("c", "v1");
        map.put("g", "v2");
        assertEquals(4, map.bucketsCount());
        assertEquals(List.of("c","g"), map.getBucketKeys(0));
        assertEquals(List.of(), map.getBucketKeys(1));
    }

    @Test
    void example3_rehashGrow() {
        CustomHashMap map = new CustomHashMap(0.25, 0.75);
        assertEquals(2, map.bucketsCount());
        map.put("a", "1"); // LF=0.50, no rehash
        map.put("b", "2"); // LF=1.00 > 0.75 → grow to 4
        assertEquals(4, map.bucketsCount());
        assertEquals(2, map.size());
    }

    @Test
    void rehashingExample_5inserts() {
        CustomHashMap map = new CustomHashMap(0.25, 0.75);
        map.put("a", "1");
        map.put("bb", "2");   // triggers grow to 4
        map.put("abcd", "3"); // LF=0.75, no rehash
        map.put("m", "4");    // triggers grow to 8
        map.put("zzz", "5");  // LF=0.63, no rehash
        assertEquals(8, map.bucketsCount());
        assertEquals(5, map.size());
        // bucket 2: "a" (2%8=2) and "abcd" (26%8=2)
        assertEquals(List.of("a","abcd"), map.getBucketKeys(2));
        assertEquals(List.of("m"), map.getBucketKeys(6)); // 14%8=6
        assertEquals(List.of("zzz"), map.getBucketKeys(7)); // 87%8=7
        assertEquals(List.of("bb"), map.getBucketKeys(0)); // 8%8=0
    }

    @Test
    void rehashShrink() {
        CustomHashMap map = new CustomHashMap(0.50, 0.75);
        map.put("a","1"); map.put("b","2"); map.put("c","3"); map.put("d","4"); map.put("e","5"); map.put("f","6"); map.put("g","7"); map.put("h","8");
        // After grows, remove until shrink triggers
        int bc = map.bucketsCount();
        assertTrue(bc >= 8);
        map.remove("a"); map.remove("b"); map.remove("c"); map.remove("d"); map.remove("e");
        // Should have shrunk
        assertTrue(map.bucketsCount() < bc);
        assertEquals(3, map.size());
    }

    @Test
    void getBucketKeys_outOfBounds() {
        CustomHashMap map = new CustomHashMap(0.25, 0.75);
        assertEquals(List.of(), map.getBucketKeys(-1));
        assertEquals(List.of(), map.getBucketKeys(100));
    }
}
