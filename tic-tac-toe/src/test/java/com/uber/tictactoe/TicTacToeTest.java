package com.uber.tictactoe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TicTacToeTest {
    @Test
    void player1WinsRow() {
        TicTacToe g = new TicTacToe(3);
        assertEquals(0, g.move(0,0,1));
        assertEquals(0, g.move(1,0,2));
        assertEquals(0, g.move(0,1,1));
        assertEquals(0, g.move(1,1,2));
        assertEquals(1, g.move(0,2,1)); // row 0 complete
    }

    @Test
    void player2WinsCol() {
        TicTacToe g = new TicTacToe(3);
        assertEquals(0, g.move(0,0,1));
        assertEquals(0, g.move(0,2,2));
        assertEquals(0, g.move(1,0,1));
        assertEquals(0, g.move(1,2,2));
        assertEquals(0, g.move(2,1,1));
        assertEquals(2, g.move(2,2,2)); // col 2 complete
    }

    @Test
    void player1WinsDiagonal() {
        TicTacToe g = new TicTacToe(3);
        assertEquals(0, g.move(0,0,1));
        assertEquals(0, g.move(0,1,2));
        assertEquals(0, g.move(1,1,1));
        assertEquals(0, g.move(0,2,2));
        assertEquals(1, g.move(2,2,1)); // main diagonal
    }

    @Test
    void player1WinsAntiDiagonal() {
        TicTacToe g = new TicTacToe(3);
        assertEquals(0, g.move(0,2,1));
        assertEquals(0, g.move(0,0,2));
        assertEquals(0, g.move(1,1,1));
        assertEquals(0, g.move(1,0,2));
        assertEquals(1, g.move(2,0,1)); // anti-diagonal
    }

    @Test
    void noWinner() {
        // 3x3 drawn game: X O X / O X O / O X O  — no complete row/col/diag for either player
        TicTacToe g = new TicTacToe(3);
        assertEquals(0, g.move(0,0,1)); // X
        assertEquals(0, g.move(0,1,2)); // O
        assertEquals(0, g.move(0,2,1)); // X
        assertEquals(0, g.move(1,0,2)); // O
        assertEquals(0, g.move(1,1,1)); // X
        assertEquals(0, g.move(1,2,2)); // O
        assertEquals(0, g.move(2,1,1)); // X
        assertEquals(0, g.move(2,0,2)); // O
        assertEquals(0, g.move(2,2,2)); // O — board full, no winner
    }

    @Test
    void largerBoard() {
        TicTacToe g = new TicTacToe(5);
        for (int i = 0; i < 4; i++) assertEquals(0, g.move(i, i, 1));
        assertEquals(1, g.move(4, 4, 1)); // diagonal win
    }
}
