package com.example.sudokutor.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AnalysisResponse {

    private long totalMoves;
    private long correctMoves;
    private long incorrectMoves;
    private double averageSecondsPerMove;
    private String hardestBox;
    private String feedback;
    private List<String> strengths;
    private List<String> tips;
}
