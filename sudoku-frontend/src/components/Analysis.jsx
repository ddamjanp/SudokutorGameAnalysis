export default function Analysis({ data }) {
  return (
    <div className="analysis">
      <h2>Game Analysis</h2>

      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-value">{data.totalMoves}</span>
          <span className="stat-label">Total Moves</span>
        </div>
        <div className="stat-card correct">
          <span className="stat-value">{data.correctMoves}</span>
          <span className="stat-label">Correct</span>
        </div>
        <div className="stat-card wrong">
          <span className="stat-value">{data.incorrectMoves}</span>
          <span className="stat-label">Incorrect</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{data.averageSecondsPerMove.toFixed(1)}s</span>
          <span className="stat-label">Avg. Time / Move</span>
        </div>
      </div>

      {data.hardestBox !== 'None' && (
        <p className="hardest-box">Hardest area: <strong>{data.hardestBox}</strong></p>
      )}

      <p className="feedback-text">{data.feedback}</p>

      {data.strengths?.length > 0 && (
        <div className="analysis-section">
          <h3>Strengths</h3>
          <ul>{data.strengths.map((s, i) => <li key={i}>{s}</li>)}</ul>
        </div>
      )}

      {data.tips?.length > 0 && (
        <div className="analysis-section">
          <h3>Tips for Next Time</h3>
          <ul>{data.tips.map((t, i) => <li key={i}>{t}</li>)}</ul>
        </div>
      )}
    </div>
  );
}
