package com.example.sudokutor.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "board_snapshot")
public class BoardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_session_id")
    private GameSession gameSession;

    @Column(nullable = false,length = 100)
    private String boardState;
    private LocalDateTime capturedAt;

}
