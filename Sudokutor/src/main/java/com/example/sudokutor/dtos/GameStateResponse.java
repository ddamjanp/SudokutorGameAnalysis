package com.example.sudokutor.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class GameStateResponse {

    private Long gameId;
    private String difficulty;
    private String currentBoard;
    private int mistakeCount;
    private int maxMistakes;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
