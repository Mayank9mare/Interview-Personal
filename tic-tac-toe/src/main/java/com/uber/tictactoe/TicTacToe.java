// Companies: Google, Amazon, Apple, Uber, Microsoft
package com.uber.tictactoe;

/**
 * n×n Tic-Tac-Toe board for two players.
 *
 * <p>Uses O(n) counter arrays instead of an O(n²) board scan. For each player the
 * algorithm tracks how many marks that player has placed in each row, each column,
 * the main diagonal, and the anti-diagonal. A win is detected in O(1) per move.
 *
 * <p>Not thread-safe.
 */
public class TicTacToe {
    /** Board dimension. */
    private final int n;
    /** Row mark counts per player: {@code rows[player-1][row]}. */
    private final int[][] rows;
    /** Column mark counts per player: {@code cols[player-1][col]}. */
    private final int[][] cols;
    /** Main-diagonal mark counts per player: {@code diag[player-1]}. */
    private final int[] diag;
    /** Anti-diagonal mark counts per player: {@code antiDiag[player-1]}. */
    private final int[] antiDiag;

    /**
     * @param n board size (n×n grid)
     */
    public TicTacToe(int n) {
        this.n = n;
        rows = new int[2][n];
        cols = new int[2][n];
        diag = new int[2];
        antiDiag = new int[2];
    }

    /**
     * Places a mark for {@code player} at {@code (row, col)} and checks for a win.
     *
     * @param row    zero-based row index
     * @param col    zero-based column index
     * @param player 1 or 2
     * @return the winning player number (1 or 2) if this move wins the game; 0 otherwise
     */
    public int move(int row, int col, int player) {
        int p = player - 1;
        rows[p][row]++;
        cols[p][col]++;
        if (row == col) diag[p]++;
        if (row + col == n - 1) antiDiag[p]++;
        if (rows[p][row] == n || cols[p][col] == n || diag[p] == n || antiDiag[p] == n)
            return player;
        return 0;
    }
}
