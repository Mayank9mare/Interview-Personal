package com.uber.leaderboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LeaderboardTest {
    private Leaderboard lb;

    @BeforeEach
    void setUp() { lb = new Leaderboard(); }

    @Test
    void getTopK_singleUser() {
        lb.addUser("u1", List.of("p1", "p2"));
        lb.addScore("p1", 10);
        lb.addScore("p2", 5);
        assertEquals(List.of("u1"), lb.getTopK(1));
    }

    @Test
    void getTopK_sortedByScore() {
        lb.addUser("u1", List.of("p1"));
        lb.addUser("u2", List.of("p2"));
        lb.addScore("p1", 10);
        lb.addScore("p2", 20);
        assertEquals(List.of("u2", "u1"), lb.getTopK(2));
    }

    @Test
    void getTopK_tieBrokenByUserId() {
        lb.addUser("u1", List.of("p1"));
        lb.addUser("u2", List.of("p2"));
        lb.addScore("p1", 10);
        lb.addScore("p2", 10);
        assertEquals(List.of("u1", "u2"), lb.getTopK(2));
    }

    @Test
    void getTopK_kLargerThanUsers() {
        lb.addUser("u1", List.of("p1"));
        lb.addScore("p1", 5);
        assertEquals(List.of("u1"), lb.getTopK(10));
    }

    @Test
    void addScore_accumulatesDeltas() {
        lb.addUser("u1", List.of("p1"));
        lb.addScore("p1", 5);
        lb.addScore("p1", 3);
        lb.addScore("p1", 2);
        assertEquals(List.of("u1"), lb.getTopK(1));
        // score should be 10
        lb.addUser("u2", List.of("p2"));
        lb.addScore("p2", 9);
        assertEquals(List.of("u1", "u2"), lb.getTopK(2));
    }

    @Test
    void getTopK_sharedPlayerAcrossUsers() {
        lb.addUser("u1", List.of("p1", "p2"));
        lb.addUser("u2", List.of("p2", "p3"));
        lb.addScore("p1", 5);
        lb.addScore("p2", 10);
        lb.addScore("p3", 3);
        // u1 score = 5+10=15, u2 score = 10+3=13
        assertEquals(List.of("u1", "u2"), lb.getTopK(2));
    }

    @Test
    void getTopK_zeroScoreUsers() {
        lb.addUser("u1", List.of("p1"));
        lb.addUser("u2", List.of("p2"));
        // no scores added — all zero
        List<String> top = lb.getTopK(2);
        assertEquals(2, top.size());
        // tied at 0, sort by userId
        assertEquals("u1", top.get(0));
        assertEquals("u2", top.get(1));
    }

    @Test
    void getTopK_emptyLeaderboard() {
        assertEquals(List.of(), lb.getTopK(5));
    }
}
