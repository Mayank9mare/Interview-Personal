// Companies: Amazon, Google, Meta
package com.uber.snakegame;

import java.util.*;

/**
 * Simulates the classic Snake game on a fixed grid.
 *
 * <p>The snake starts at (0, 0) with length 1 and grows by one cell each time it eats a
 * food item. Food items are consumed in the order they appear in the input list.
 *
 * <p>Data structures: {@code Deque<int[]>} for O(1) head/tail access; {@code HashSet<String>}
 * for O(1) body-collision detection using "row,col" keys.
 *
 * <p>Not thread-safe.
 */
public class SnakeGame {
    /** Grid width in columns. */
    private final int width;
    /** Grid height in rows. */
    private final int height;
    /** Pre-parsed food positions as [row, col] pairs, in order. */
    private final int[][] food;
    /** Index of the next food item to consume. */
    private int foodIdx = 0;
    /** Current score (one point per food item eaten). */
    private int score = 0;
    /** True once the snake hits a wall or its own body. */
    private boolean gameOver = false;
    /** Snake body cells from head (front) to tail (back). */
    private final Deque<int[]> snake = new ArrayDeque<>();
    /** "row,col" strings of all currently occupied cells for O(1) collision check. */
    private final Set<String> body = new HashSet<>();

    /**
     * @param width  number of columns in the grid
     * @param height number of rows in the grid
     * @param food   list of food positions as {@code "row,col"} strings, in consumption order
     */
    public SnakeGame(int width, int height, List<String> food) {
        this.width = width;
        this.height = height;
        this.food = new int[food.size()][2];
        for (int i = 0; i < food.size(); i++) {
            String[] p = food.get(i).split(",");
            this.food[i][0] = Integer.parseInt(p[0]);
            this.food[i][1] = Integer.parseInt(p[1]);
        }
        snake.addFirst(new int[]{0, 0});
        body.add("0,0");
    }

    /**
     * Moves the snake one step in the given direction.
     *
     * <p>The tail vacates its cell before the wall/body collision check, matching the
     * original game rule that the tail moves simultaneously with the head.
     *
     * @param direction one of {@code "U"}, {@code "D"}, {@code "L"}, {@code "R"}
     * @return current score after the move, or {@code -1} if the game is over
     */
    public int move(String direction) {
        if (gameOver) return -1;
        int[] head = snake.peekFirst();
        int r = head[0], c = head[1];
        switch (direction) {
            case "U": r--; break;
            case "D": r++; break;
            case "L": c--; break;
            default:  c++; break; // "R"
        }
        if (r < 0 || r >= height || c < 0 || c >= width) { gameOver = true; return -1; }

        // Remove tail before collision check (tail moves away simultaneously)
        int[] tail = snake.pollLast();
        body.remove(tail[0] + "," + tail[1]);

        if (body.contains(r + "," + c)) { gameOver = true; return -1; }

        snake.addFirst(new int[]{r, c});
        body.add(r + "," + c);

        // Check food
        if (foodIdx < food.length && food[foodIdx][0] == r && food[foodIdx][1] == c) {
            snake.addLast(tail);
            body.add(tail[0] + "," + tail[1]);
            score++;
            foodIdx++;
        }
        return score;
    }
}
