package com.example.sudokutor.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoveResponse {

    private boolean correct;
    private String board;
    private int mistakeCount;
    private String status;
    private String message;
}
