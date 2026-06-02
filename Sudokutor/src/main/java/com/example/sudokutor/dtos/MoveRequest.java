package com.example.sudokutor.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveRequest {
    private int row;
    private int col;
    private int value;
}

