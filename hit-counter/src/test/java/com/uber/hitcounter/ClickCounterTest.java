package com.uber.hitcounter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClickCounterTest {
    private ClickCounter counter;

    @BeforeEach void setUp() { counter = new ClickCounter(); }

    @Test
    void exampleFromProblem() {
        counter.recordClick(1);
        counter.recordClick(2);
        counter.recordClick(3);
        assertEquals(3, counter.getRecentClicks(4));
        counter.recordClick(300);
        assertEquals(4, counter.getRecentClicks(300));
        assertEquals(3, counter.getRecentClicks(301));
    }

    @Test
    void noClicks_returnsZero() {
        assertEquals(0, counter.getRecentClicks(100));
    }

    @Test
    void multipleClicksSameTimestamp() {
        counter.recordClick(5);
        counter.recordClick(5);
        counter.recordClick(5);
        assertEquals(3, counter.getRecentClicks(5));
    }

    @Test
    void clickExactlyAtBoundary_included() {
        counter.recordClick(1);
        // queryTime=300: window is t > 0, so t=1 is included
        assertEquals(1, counter.getRecentClicks(300));
    }

    @Test
    void clickExactlyAtBoundary_excluded() {
        counter.recordClick(1);
        // queryTime=301: window is t > 1, so t=1 is excluded
        assertEquals(0, counter.getRecentClicks(301));
    }

    @Test
    void allClicksWithinWindow() {
        counter.recordClick(100);
        counter.recordClick(200);
        counter.recordClick(300);
        // queryTime=400: window is t > 100, so only 200, 300 included
        assertEquals(2, counter.getRecentClicks(400));
    }

    @Test
    void queryBeforeAllClicks_returnsZero() {
        counter.recordClick(100);
        assertEquals(0, counter.getRecentClicks(50));
    }
}
