export default function DifficultySelector({ onSelect, loading }) {
  return (
    <div className="selector-screen">
      <h1 className="logo-title">Sudokutor</h1>
      <p className="selector-subtitle">Select a difficulty to begin</p>
      {loading ? (
        <div className="loading-spinner">Generating puzzle...</div>
      ) : (
        <div className="difficulty-buttons">
          <button className="diff-btn easy" onClick={() => onSelect('easy')}>
            <span className="diff-label">Easy</span>
            <span className="diff-desc">35 cells removed</span>
          </button>
          <button className="diff-btn medium" onClick={() => onSelect('medium')}>
            <span className="diff-label">Medium</span>
            <span className="diff-desc">46 cells removed</span>
          </button>
          <button className="diff-btn hard" onClick={() => onSelect('hard')}>
            <span className="diff-label">Hard</span>
            <span className="diff-desc">52 cells removed</span>
          </button>
        </div>
      )}
    </div>
  );
}
