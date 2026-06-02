export default function Cell({ value, isGiven, isSelected, isWrong, row, col, onClick }) {
  const classes = [
    'cell',
    isGiven ? 'given' : 'user',
    isSelected ? 'selected' : '',
    isWrong ? 'wrong' : '',
    col % 3 === 0 && col !== 0 ? 'box-border-left' : '',
    row % 3 === 0 && row !== 0 ? 'box-border-top' : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={classes} onClick={onClick}>
      {value !== 0 ? value : ''}
    </div>
  );
}
