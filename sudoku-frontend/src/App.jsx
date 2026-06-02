import { useState, useEffect, useCallback } from 'react';
import DifficultySelector from './components/DifficultySelector';
import Board from './components/Board';
import Analysis from './components/Analysis';
import { startGame, makeMove, analyzeGame } from './api/gameApi';
import './App.css';

const parseBoard = (str) => str.split('|').map(row => [...row].map(Number));

export default function App() {
  const [phase, setPhase] = useState('select');
  const [gameId, setGameId] = useState(null);
  const [initialBoard, setInitialBoard] = useState(null);
  const [currentBoard, setCurrentBoard] = useState(null);
  const [mistakeCount, setMistakeCount] = useState(0);
  const [maxMistakes, setMaxMistakes] = useState(5);
  const [status, setStatus] = useState('ACTIVE');
  const [selectedCell, setSelectedCell] = useState(null);
  const [wrongCell, setWrongCell] = useState(null);
  const [message, setMessage] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleStartGame = async (difficulty) => {
    setLoading(true);
    const data = await startGame(difficulty);
    const board = parseBoard(data.board);
    setGameId(data.gameId);
    setInitialBoard(board);
    setCurrentBoard(board.map(row => [...row]));
    setMistakeCount(0);
    setMaxMistakes(data.maxMistakes);
    setStatus('ACTIVE');
    setSelectedCell(null);
    setWrongCell(null);
    setMessage('');
    setAnalysis(null);
    setPhase('playing');
    setLoading(false);
  };

  const handleCellClick = (row, col) => {
    if (!initialBoard || initialBoard[row][col] !== 0) return;
    if (status !== 'ACTIVE') return;
    setSelectedCell([row, col]);
  };

  const handleNumberInput = useCallback(async (num) => {
    if (!selectedCell || status !== 'ACTIVE') return;
    const [row, col] = selectedCell;

    const data = await makeMove(gameId, row, col, num);
    setMistakeCount(data.mistakeCount);
    setStatus(data.status);
    setMessage(data.message);
    setCurrentBoard(parseBoard(data.board));

    if (!data.correct) {
      setWrongCell([row, col]);
      setTimeout(() => setWrongCell(null), 800);
    } else {
      setSelectedCell(null);
    }

    if (data.status === 'WON' || data.status === 'LOST') {
      setPhase('finished');
      const result = await analyzeGame(gameId);
      setAnalysis(result);
    }
  }, [selectedCell, status, gameId]);

  const handleAnalyzeClick = async () => {
    const result = await analyzeGame(gameId);
    setAnalysis(result);
  };

  useEffect(() => {
    const onKeyDown = (e) => {
      const num = parseInt(e.key);
      if (!isNaN(num) && num >= 1 && num <= 9) handleNumberInput(num);
      if (e.key === 'Escape') setSelectedCell(null);
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [handleNumberInput]);

  if (phase === 'select') {
    return <DifficultySelector onSelect={handleStartGame} loading={loading} />;
  }

  const mistakesLeft = maxMistakes - mistakeCount;

  return (
    <div className="game-container">
      <div className="game-header">
        <h1 className="logo-title">Sudokutor</h1>
        <div className="mistake-tracker">
          {Array.from({ length: maxMistakes }).map((_, i) => (
            <span key={i} className={`mistake-dot ${i < mistakeCount ? 'used' : ''}`} />
          ))}
          <span className="mistake-text">{mistakesLeft} left</span>
        </div>
      </div>

      {message && (
        <div className={`message-banner ${status === 'WON' ? 'win' : status === 'LOST' ? 'lose' : !message.includes('Wrong') ? 'correct' : 'wrong'}`}>
          {message}
        </div>
      )}

      <Board
        initialBoard={initialBoard}
        currentBoard={currentBoard}
        selectedCell={selectedCell}
        wrongCell={wrongCell}
        onCellClick={handleCellClick}
      />

      {status === 'ACTIVE' && (
        <div className="numpad">
          {[1, 2, 3, 4, 5, 6, 7, 8, 9].map(n => (
            <button key={n} className="numpad-btn" onClick={() => handleNumberInput(n)}>
              {n}
            </button>
          ))}
        </div>
      )}

      <div className="actions">
        {status === 'ACTIVE' && !analysis && (
          <button className="btn-secondary" onClick={handleAnalyzeClick}>
            Analyze Progress
          </button>
        )}
        <button className="btn-primary" onClick={() => setPhase('select')}>
          New Game
        </button>
      </div>

      {analysis && <Analysis data={analysis} />}
    </div>
  );
}
