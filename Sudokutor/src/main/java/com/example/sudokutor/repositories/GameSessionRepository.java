package com.example.sudokutor.repositories;

import com.example.sudokutor.models.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession,Long> {
}
