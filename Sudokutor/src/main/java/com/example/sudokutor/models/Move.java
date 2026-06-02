package com.example.sudokutor.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "move_log")
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;

    private int rowIndex;
    private int colIndex;
    private int value;
    private boolean correct;
    private LocalDateTime timestamp;

}
