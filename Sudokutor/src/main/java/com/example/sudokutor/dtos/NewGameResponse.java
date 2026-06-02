package com.example.sudokutor.dtos;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NewGameResponse {

    private Long gameId;
    private String difficulty;
    private String board;
    private int maxMistakes;
    private String status;
}
