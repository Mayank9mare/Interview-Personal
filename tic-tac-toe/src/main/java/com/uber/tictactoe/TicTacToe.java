// Companies: Google, Amazon, Apple, Uber, Microsoft
package com.uber.tictactoe;

public class TicTacToe {
    private final int n;
    private final int[][] rows;   // rows[player-1][row]
    private final int[][] cols;   // cols[player-1][col]
    private final int[] diag;     // diag[player-1]
    private final int[] antiDiag; // antiDiag[player-1]

    public TicTacToe(int n) {
        this.n = n;
        rows = new int[2][n];
        cols = new int[2][n];
        diag = new int[2];
        antiDiag = new int[2];
    }

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
