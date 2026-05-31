import { useState } from 'react';
import type { Draw } from '../types';
import { parseCombination } from '../utils/numbers';
import { StatusMessage } from './StatusMessage';

type DrawCardProps = {
  draw: Draw;
  onBuyTicket: (drawId: number, numbers: number[]) => Promise<void>;
  onComplete: (drawId: number) => Promise<void>;
  busy: boolean;
};

export function DrawCard({ draw, onBuyTicket, onComplete, busy }: DrawCardProps) {
  const [numbersInput, setNumbersInput] = useState('3, 11, 22, 33, 41, 49');
  const [localError, setLocalError] = useState<string | null>(null);

  async function handleBuy(e: React.FormEvent) {
    e.preventDefault();
    setLocalError(null);
    const parsed = parseCombination(numbersInput);
    if (typeof parsed === 'string') {
      setLocalError(parsed);
      return;
    }
    await onBuyTicket(draw.id, parsed);
  }

  return (
    <article className="draw-card" aria-labelledby={`draw-title-${draw.id}`}>
      <header className="draw-card__header">
        <h3 id={`draw-title-${draw.id}`}>Тираж #{draw.id}</h3>
        <span className="badge badge--active">{draw.status}</span>
      </header>
      <p className="muted">Создан: {new Date(draw.createdAt).toLocaleString('ru-RU')}</p>

      <form className="inline-form" onSubmit={handleBuy}>
        <label htmlFor={`numbers-${draw.id}`}>Комбинация билета</label>
        <input
          id={`numbers-${draw.id}`}
          type="text"
          value={numbersInput}
          onChange={(e) => setNumbersInput(e.target.value)}
          placeholder="1, 2, 3, 4, 5, 6"
          disabled={busy}
          aria-describedby={localError ? `draw-error-${draw.id}` : undefined}
        />
        <button type="submit" disabled={busy}>
          Купить билет
        </button>
      </form>
      {localError && (
        <StatusMessage variant="error" message={localError} id={`draw-error-${draw.id}`} />
      )}

      <button
        type="button"
        className="btn-secondary"
        disabled={busy}
        onClick={() => onComplete(draw.id)}
      >
        Завершить тираж
      </button>
    </article>
  );
}
