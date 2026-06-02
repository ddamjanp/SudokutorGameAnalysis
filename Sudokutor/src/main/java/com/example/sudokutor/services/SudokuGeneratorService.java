package com.example.sudokutor.services;

import com.example.sudokutor.util.BoardUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SudokuGeneratorService {

    private final Random random = new Random();

    public record GeneratedPuzzle(String puzzle, String solution) {}

    public GeneratedPuzzle generate(String difficulty) {
        int cellsToRemove = switch (difficulty.toLowerCase()) {
            case "easy"   -> 35;
            case "medium" -> 46;
            case "hard"   -> 52;
            default -> throw new IllegalArgumentException("Unsupported difficulty: " + difficulty);
        };

        int[][] solved = generateSolvedBoard();
        int[][] puzzle = createPuzzle(deepCopy(solved), cellsToRemove);

        return new GeneratedPuzzle(BoardUtils.boardToString(puzzle), BoardUtils.boardToString(solved));
    }

    private int[][] generateSolvedBoard() {
        int[][] board = new int[9][9];
        fillBoard(board);
        return board;
    }

    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    List<Integer> candidates = candidates(board, row, col);
                    Collections.shuffle(candidates, random);
                    for (int num : candidates) {
                        board[row][col] = num;
                        if (fillBoard(board)) return true;
                        board[row][col] = 0;
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private int[][] createPuzzle(int[][] board, int cellsToRemove) {
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                positions.add(new int[]{r, c});
        Collections.shuffle(positions, random);

        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) break;
            int backup = board[pos[0]][pos[1]];
            board[pos[0]][pos[1]] = 0;
            if (countSolutions(deepCopy(board)) == 1) {
                removed++;
            } else {
                board[pos[0]][pos[1]] = backup;
            }
        }
        return board;
    }

    private int countSolutions(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    int count = 0;
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            count += countSolutions(board);
                            board[row][col] = 0;
                            if (count > 1) return count;
                        }
                    }
                    return count;
                }
            }
        }
        return 1;
    }

    private List<Integer> candidates(int[][] board, int row, int col) {
        Set<Integer> used = new HashSet<>();
        for (int c = 0; c < 9; c++) used.add(board[row][c]);
        for (int r = 0; r < 9; r++) used.add(board[r][col]);
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++)
            for (int c = bc; c < bc + 3; c++)
                used.add(board[r][c]);
        List<Integer> result = new ArrayList<>();
        for (int n = 1; n <= 9; n++)
            if (!used.contains(n)) result.add(n);
        return result;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int c = 0; c < 9; c++) if (board[row][c] == num) return false;
        for (int r = 0; r < 9; r++) if (board[r][col] == num) return false;
        int br = (row / 3) * 3, bc = (col / 3) * 3;
        for (int r = br; r < br + 3; r++)
            for (int c = bc; c < bc + 3; c++)
                if (board[r][c] == num) return false;
        return true;
    }

    private int[][] deepCopy(int[][] board) {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++)
            copy[i] = Arrays.copyOf(board[i], 9);
        return copy;
    }
}
