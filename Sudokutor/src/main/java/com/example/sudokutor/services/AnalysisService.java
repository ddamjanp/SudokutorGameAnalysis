package com.example.sudokutor.services;

import com.example.sudokutor.dtos.AnalysisResponse;
import com.example.sudokutor.models.BoardSnapshot;
import com.example.sudokutor.models.GameSession;
import com.example.sudokutor.models.Move;
import com.example.sudokutor.repositories.BoardSnapshotRepository;
import com.example.sudokutor.repositories.GameSessionRepository;
import com.example.sudokutor.repositories.MoveRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final GameSessionRepository gameSessionRepository;
    private final MoveRepository moveRepository;
    private final BoardSnapshotRepository boardSnapshotRepository;
    private final LlmService llmService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisService(GameSessionRepository gameSessionRepository,
                           MoveRepository moveRepository,
                           BoardSnapshotRepository boardSnapshotRepository,
                           LlmService llmService) {
        this.gameSessionRepository = gameSessionRepository;
        this.moveRepository = moveRepository;
        this.boardSnapshotRepository = boardSnapshotRepository;
        this.llmService = llmService;
    }

    public AnalysisResponse analyze(Long sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + sessionId));

        List<Move> moves = moveRepository.findByGameSessionIdOrderByTimestampAsc(sessionId);
        List<BoardSnapshot> snapshots = boardSnapshotRepository.findByGameSessionIdOrderByCapturedAtAsc(sessionId);

        long total = moves.size();
        long correct = moves.stream().filter(Move::isCorrect).count();
        long incorrect = total - correct;
        double avgSeconds = computeAverageSecondsBetweenSnapshots(snapshots);
        String hardestBox = computeHardestBox(moves);

        LlmFeedback llm = callLlm(session, moves, snapshots, total, correct, incorrect, avgSeconds, hardestBox);

        return new AnalysisResponse(total, correct, incorrect, avgSeconds, hardestBox,
                llm.feedback(), llm.strengths(), llm.tips());
    }

    private LlmFeedback callLlm(GameSession session, List<Move> moves, List<BoardSnapshot> snapshots,
                                 long total, long correct, long incorrect,
                                 double avgSeconds, String hardestBox) {
        try {
            String prompt = buildPrompt(session, moves, snapshots, total, correct, incorrect, avgSeconds, hardestBox);
            String json = stripMarkdown(llmService.generate(prompt));
            JsonNode root = objectMapper.readTree(json);
            String feedback = root.get("feedback").asText();
            List<String> strengths = objectMapper.convertValue(root.get("strengths"), new TypeReference<>() {});
            List<String> tips = objectMapper.convertValue(root.get("tips"), new TypeReference<>() {});
            return new LlmFeedback(feedback, strengths, tips);
        } catch (Exception e) {
            e.printStackTrace();
            return new LlmFeedback("Analysis unavailable: " + e.getMessage(), List.of(), List.of());
        }
    }

    private String buildPrompt(GameSession session, List<Move> moves, List<BoardSnapshot> snapshots,
                                long total, long correct, long incorrect,
                                double avgSeconds, String hardestBox) {
        double accuracy = total > 0 ? (double) correct / total * 100 : 0;

        StringBuilder timeline = new StringBuilder();
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);
            long secondsSincePrev = i == 0 ? 0
                    : Duration.between(moves.get(i - 1).getTimestamp(), m.getTimestamp()).getSeconds();
            timeline.append(String.format(
                    "  Move %d: placed %d at row %d, col %d (box %d,%d) -> %s [%ds since last move]%n",
                    i + 1, m.getValue(), m.getRowIndex(), m.getColIndex(),
                    m.getRowIndex() / 3, m.getColIndex() / 3,
                    m.isCorrect() ? "CORRECT" : "WRONG",
                    secondsSincePrev
            ));

            if (!m.isCorrect() && i < snapshots.size()) {
                timeline.append("  Board state when this mistake was made:\n");
                timeline.append(formatBoard(snapshots.get(i).getBoardState()));
            }
        }

        return """
                You are a strict but fair Sudoku coach. Analyze this player's game and give honest, specific, educational feedback.
                Do not sugarcoat. Reference actual moves and board positions. Teach concrete techniques based on where they struggled.
                You have the full puzzle, solution, and board state at every mistake — use them to give specific, actionable advice.

                INITIAL PUZZLE (0 = empty cell):
                %s

                SOLUTION:
                %s

                GAME SESSION:
                  Difficulty: %s
                  Outcome: %s
                  Total moves: %d
                  Correct: %d
                  Wrong: %d
                  Accuracy: %.1f%%
                  Average seconds per move: %.1f
                  Box with most mistakes: %s

                MOVE TIMELINE (board shown after each wrong move):
                %s

                TECHNIQUES TO REFERENCE WHEN RELEVANT:
                - Naked Single: only one digit possible in a cell after elimination
                - Hidden Single: a digit can only fit in one cell within a row, column, or box
                - Naked Pair/Triple: two or three cells in a unit share the same candidates, eliminating those from the rest
                - Pointing Pairs: a digit in a box is confined to one row or column, eliminating it outside that box
                - Box-Line Reduction: a digit in a row or column is confined to one box, eliminating it from the rest of that box
                - X-Wing: a digit in exactly two rows appears in only two columns, enabling cross eliminations

                Respond with a JSON object containing exactly these three fields:
                - feedback: a string with 2-3 sentences giving an honest overall assessment, referencing specific moves and board positions
                - strengths: an array of strings listing only genuinely earned positives, empty array if nothing stands out
                - tips: an array of exactly 3 strings, each naming a specific technique with a concrete example from this game
                """.formatted(
                formatBoard(session.getInitialBoard()),
                formatBoard(session.getSolutionBoard()),
                session.getDifficulty(),
                session.getStatus().name(),
                total, correct, incorrect, accuracy, avgSeconds,
                hardestBox,
                timeline
        );
    }

    private String formatBoard(String boardString) {
        String[] rows = boardString.split("\\|");
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            if (r == 3 || r == 6) sb.append("  ------+-------+------\n");
            sb.append("  ");
            for (int c = 0; c < 9; c++) {
                if (c == 3 || c == 6) sb.append("| ");
                char val = rows[r].charAt(c);
                sb.append(val == '0' ? ". " : val + " ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private double computeAverageSecondsBetweenSnapshots(List<BoardSnapshot> snapshots) {
        if (snapshots.size() < 2) return 0.0;
        long totalSeconds = 0;
        for (int i = 1; i < snapshots.size(); i++) {
            totalSeconds += Duration.between(
                    snapshots.get(i - 1).getCapturedAt(),
                    snapshots.get(i).getCapturedAt()
            ).getSeconds();
        }
        return (double) totalSeconds / (snapshots.size() - 1);
    }

    private String computeHardestBox(List<Move> moves) {
        Map<String, Long> mistakesByBox = moves.stream()
                .filter(m -> !m.isCorrect())
                .collect(Collectors.groupingBy(
                        m -> "Box (" + (m.getRowIndex() / 3) + "," + (m.getColIndex() / 3) + ")",
                        Collectors.counting()
                ));
        return mistakesByBox.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("None");
    }

    private String stripMarkdown(String raw) {
        if (raw == null) return null;
        String stripped = raw.strip();
        if (stripped.startsWith("```")) {
            stripped = stripped.replaceFirst("```[a-zA-Z]*", "").strip();
            if (stripped.endsWith("```")) {
                stripped = stripped.substring(0, stripped.lastIndexOf("```")).strip();
            }
        }
        return stripped;
    }

    private record LlmFeedback(String feedback, List<String> strengths, List<String> tips) {}
}
