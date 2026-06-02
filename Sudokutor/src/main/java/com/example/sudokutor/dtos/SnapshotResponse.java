package com.example.sudokutor.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SnapshotResponse {

    private Long snapshotId;
    private String boardState;
    private LocalDateTime capturedAt;
}
