package com.example.sudokutor.controllers;


import com.example.sudokutor.dtos.AnalysisResponse;
import com.example.sudokutor.dtos.GameStateResponse;
import com.example.sudokutor.dtos.MoveRequest;
import com.example.sudokutor.dtos.MoveResponse;
import com.example.sudokutor.dtos.NewGameRequest;
import com.example.sudokutor.dtos.NewGameResponse;
import com.example.sudokutor.dtos.SnapshotResponse;
import com.example.sudokutor.services.AnalysisService;
import com.example.sudokutor.services.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("play/game")
public class GameController {

    private final GameService gameService;
    private final AnalysisService analysisService;

    public GameController(GameService gameService, AnalysisService analysisService) {
        this.gameService = gameService;
        this.analysisService = analysisService;
    }

    @PostMapping
    public NewGameResponse startGame(@RequestBody NewGameRequest request) {
        return gameService.startNewGame(request.getDifficulty());
    }

    @GetMapping("/{id}")
    public GameStateResponse getGameState(@PathVariable Long id) {
        return gameService.getGameState(id);
    }

    @GetMapping("/{id}/snapshots")
    public List<SnapshotResponse> getSnapshots(@PathVariable Long id) {
        return gameService.getSnapshots(id);
    }

    @PostMapping("/{id}/analyze")
    public AnalysisResponse analyze(@PathVariable Long id) {
        return analysisService.analyze(id);
    }

    @PostMapping("/{id}/move")
    public MoveResponse makeMove(@PathVariable Long id, @RequestBody MoveRequest request) {
        return gameService.makeMove(id, request);
    }
}
