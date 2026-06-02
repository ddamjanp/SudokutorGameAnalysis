import Cell from './Cell';

export default function Board({ initialBoard, currentBoard, selectedCell, wrongCell, onCellClick }) {
  return (
    <div className="board">
      {currentBoard.map((row, r) =>
        row.map((val, c) => (
          <Cell
            key={`${r}-${c}`}
            value={val}
            isGiven={initialBoard[r][c] !== 0}
            isSelected={selectedCell?.[0] === r && selectedCell?.[1] === c}
            isWrong={wrongCell?.[0] === r && wrongCell?.[1] === c}
            row={r}
            col={c}
            onClick={() => onCellClick(r, c)}
          />
        ))
      )}
    </div>
  );
}
