package com.example.sudokutor.services;

import com.example.sudokutor.dtos.GameStateResponse;
import com.example.sudokutor.dtos.MoveRequest;
import com.example.sudokutor.dtos.MoveResponse;
import com.example.sudokutor.dtos.SnapshotResponse;
import com.example.sudokutor.models.BoardSnapshot;
import com.example.sudokutor.models.GameSession;
import com.example.sudokutor.models.Move;
import com.example.sudokutor.dtos.NewGameResponse;
import com.example.sudokutor.models.enums.GameStatus;
import com.example.sudokutor.repositories.BoardSnapshotRepository;
import com.example.sudokutor.repositories.GameSessionRepository;
import com.example.sudokutor.repositories.MoveRepository;
import com.example.sudokutor.util.BoardUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {

    private final GameSessionRepository gameSessionRepository;
    private final MoveRepository moveRepository;
    private final BoardSnapshotRepository boardSnapshotRepository;
    private final SudokuGeneratorService sudokuGeneratorService;

    public GameService(GameSessionRepository gameSessionRepository,
                       MoveRepository moveRepository,
                       BoardSnapshotRepository boardSnapshotRepository,
                       SudokuGeneratorService sudokuGeneratorService) {
        this.gameSessionRepository = gameSessionRepository;
        this.moveRepository = moveRepository;
        this.boardSnapshotRepository = boardSnapshotRepository;
        this.sudokuGeneratorService = sudokuGeneratorService;
    }

    public NewGameResponse startNewGame(String difficulty) {
        SudokuGeneratorService.GeneratedPuzzle generated = sudokuGeneratorService.generate(difficulty);
        String initialBoard = generated.puzzle();
        String solutionBoard = generated.solution();

        GameSession gameSession = new GameSession();
        gameSession.setDifficulty(difficulty.toLowerCase());
        gameSession.setInitialBoard(initialBoard);
        gameSession.setSolutionBoard(solutionBoard);
        gameSession.setCurrentBoard(initialBoard);
        gameSession.setMistakeCount(0);
        gameSession.setMaxMistakes(5);
        gameSession.setStatus(GameStatus.ACTIVE);
        gameSession.setStartedAt(LocalDateTime.now());

        gameSessionRepository.save(gameSession);

        return new NewGameResponse(
                gameSession.getId(),
                gameSession.getDifficulty(),
                gameSession.getCurrentBoard(),
                gameSession.getMaxMistakes(),
                gameSession.getStatus().name()
        );
    }

    public GameStateResponse getGameState(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));

        return new GameStateResponse(
                session.getId(),
                session.getDifficulty(),
                session.getCurrentBoard(),
                session.getMistakeCount(),
                session.getMaxMistakes(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getEndedAt()
        );
    }

    public List<SnapshotResponse> getSnapshots(Long sessionId) {
        if (!gameSessionRepository.existsById(sessionId)) {
            throw new IllegalArgumentException("Game session not found: " + sessionId);
        }

        return boardSnapshotRepository.findByGameSessionIdOrderByCapturedAtAsc(sessionId)
                .stream()
                .map(s -> new SnapshotResponse(s.getId(), s.getBoardState(), s.getCapturedAt()))
                .toList();
    }

    public MoveResponse makeMove(Long sessionId, MoveRequest request) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));

        if (session.getStatus() != GameStatus.ACTIVE) {
            return new MoveResponse(false, session.getCurrentBoard(), session.getMistakeCount(),
                    session.getStatus().name(), "Game is already over.");
        }

        int row = request.getRow();
        int col = request.getCol();
        int value = request.getValue();

        int[][] solution = BoardUtils.parseBoard(session.getSolutionBoard());
        boolean correct = solution[row][col] == value;

        Move move = new Move();
        move.setGameSession(session);
        move.setRowIndex(row);
        move.setColIndex(col);
        move.setValue(value);
        move.setCorrect(correct);
        move.setTimestamp(LocalDateTime.now());
        moveRepository.save(move);

        String message;
        if (correct) {
            int[][] current = BoardUtils.parseBoard(session.getCurrentBoard());
            current[row][col] = value;
            session.setCurrentBoard(BoardUtils.boardToString(current));

            if (BoardUtils.isBoardComplete(current)) {
                session.setStatus(GameStatus.WON);
                session.setEndedAt(LocalDateTime.now());
                message = "Congratulations! You solved the puzzle!";
            } else {
                message = "Correct!";
            }
        } else {
            session.setMistakeCount(session.getMistakeCount() + 1);
            if (session.getMistakeCount() >= session.getMaxMistakes()) {
                session.setStatus(GameStatus.LOST);
                session.setEndedAt(LocalDateTime.now());
                message = "Game over! Too many mistakes.";
            } else {
                int remaining = session.getMaxMistakes() - session.getMistakeCount();
                message = "Wrong move! " + remaining + " mistake" + (remaining == 1 ? "" : "s") + " remaining.";
            }
        }

        gameSessionRepository.save(session);

        BoardSnapshot snapshot = new BoardSnapshot();
        snapshot.setGameSession(session);
        snapshot.setBoardState(session.getCurrentBoard());
        snapshot.setCapturedAt(LocalDateTime.now());
        boardSnapshotRepository.save(snapshot);

        return new MoveResponse(correct, session.getCurrentBoard(), session.getMistakeCount(),
                session.getStatus().name(), message);
    }
}
