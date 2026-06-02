package com.example.sudokutor.repositories;

import com.example.sudokutor.models.BoardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardSnapshotRepository extends JpaRepository<BoardSnapshot,Long> {

    List<BoardSnapshot> findByGameSessionIdOrderByCapturedAtAsc(Long gameSessionId);
}
