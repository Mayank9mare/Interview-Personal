// Companies: Amazon, Google, Meta
package com.uber.snakegame;

import java.util.*;

public class SnakeGame {
    private final int width, height;
    private final int[][] food;
    private int foodIdx = 0, score = 0;
    private boolean gameOver = false;
    private final Deque<int[]> snake = new ArrayDeque<>();
    private final Set<String> body = new HashSet<>();

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
