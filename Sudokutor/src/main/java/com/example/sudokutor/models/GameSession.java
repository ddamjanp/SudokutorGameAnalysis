package com.example.sudokutor.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.sudokutor.models.enums.GameStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String difficulty;

    @Column(nullable = false,length = 200)
    private String initialBoard;

    @Column(nullable = false,length = 200)
    private String solutionBoard;

    @Column(nullable = false,length = 200)
    private String currentBoard;

    private int mistakeCount;

    private int maxMistakes = 5;

    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.ACTIVE;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}

