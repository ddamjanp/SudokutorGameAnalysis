package com.example.sudokutor.repositories;

import com.example.sudokutor.models.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoveRepository extends JpaRepository<Move, Long> {

    List<Move> findByGameSessionIdOrderByTimestampAsc(Long gameSessionId);
}
