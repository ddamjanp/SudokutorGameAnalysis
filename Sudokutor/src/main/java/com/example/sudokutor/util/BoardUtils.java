package com.example.sudokutor.util;

public class BoardUtils {


    public static int[][] parseBoard(String boardString) {
        String[] rows = boardString.split("\\|");
        int[][] board = new int[9][9];

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = Character.getNumericValue(rows[i].charAt(j));
            }
        }
        return board;
    }

    public static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            if (i > 0) sb.append("|");
            for (int j = 0; j < 9; j++) {
                sb.append(board[i][j]);
            }
        }
        return sb.toString();
    }

    public static boolean isBoardComplete(int[][] board) {
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) return false;
            }
        }
        return true;
    }
}

