package com.uber.snakegame;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SnakeGameTest {
    @Test
    void example1() {
        SnakeGame g = new SnakeGame(4, 3, List.of("0,2","1,2","1,1"));
        assertEquals(0, g.move("R"));
        assertEquals(1, g.move("R"));
        assertEquals(2, g.move("D"));
        assertEquals(3, g.move("L"));
        assertEquals(3, g.move("L"));
    }

    @Test
    void example2_wallCollision() {
        SnakeGame g = new SnakeGame(3, 3, List.of("2,0"));
        assertEquals(0, g.move("D"));
        assertEquals(1, g.move("D"));
        assertEquals(-1, g.move("D"));
        assertEquals(-1, g.move("R"));
    }

    @Test
    void example3_selfCollision() {
        SnakeGame g = new SnakeGame(2, 2, List.of("0,1","1,1"));
        assertEquals(1, g.move("R"));
        assertEquals(2, g.move("D"));
        assertEquals(2, g.move("L"));
        assertEquals(-1, g.move("R"));
    }

    @Test
    void example4_noFood() {
        SnakeGame g = new SnakeGame(2, 2, List.of());
        assertEquals(0, g.move("R"));
        assertEquals(0, g.move("L"));
        assertEquals(0, g.move("D"));
    }

    @Test
    void movingIntoOldTailAllowed() {
        // Snake at (0,0) length=1. Move R to (0,1), tail was (0,0) → now free. Move L back → valid
        SnakeGame g = new SnakeGame(3, 3, List.of());
        assertEquals(0, g.move("R"));
        assertEquals(0, g.move("L")); // move back into (0,0) which tail just left — valid
    }
}
