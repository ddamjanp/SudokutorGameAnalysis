const BASE = 'http://localhost:8080/play/game';

export async function startGame(difficulty) {
  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ difficulty }),
  });
  return res.json();
}

export async function makeMove(gameId, row, col, value) {
  const res = await fetch(`${BASE}/${gameId}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ row, col, value }),
  });
  return res.json();
}

export async function analyzeGame(gameId) {
  const res = await fetch(`${BASE}/${gameId}/analyze`, { method: 'POST' });
  return res.json();
}
