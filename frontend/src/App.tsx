import { useCallback, useEffect, useState } from 'react';
import { api } from './api/client';
import { DrawCard } from './components/DrawCard';
import { StatusMessage } from './components/StatusMessage';
import { TicketChecker } from './components/TicketChecker';
import type { ApiError, Draw, Ticket } from './types';
import { formatNumbers } from './utils/numbers';

function getErrorMessage(err: unknown): string {
  if (err && typeof err === 'object' && 'message' in err) {
    return String((err as ApiError).message);
  }
  return 'Неизвестная ошибка';
}

export default function App() {
  const [draws, setDraws] = useState<Draw[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [lastTicketId, setLastTicketId] = useState<number | null>(null);
  const [lastCompletedDraw, setLastCompletedDraw] = useState<Draw | null>(null);

  const loadDraws = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setDraws(await api.listActiveDraws());
    } catch (e) {
      setError(getErrorMessage(e));
      setDraws([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDraws();
  }, [loadDraws]);

  async function withBusy<T>(fn: () => Promise<T>, successMsg?: string): Promise<T | undefined> {
    setBusy(true);
    setError(null);
    if (successMsg) setSuccess(null);
    try {
      const result = await fn();
      if (successMsg) setSuccess(successMsg);
      await loadDraws();
      return result;
    } catch (e) {
      setError(getErrorMessage(e));
      return undefined;
    } finally {
      setBusy(false);
    }
  }

  async function handleCreateDraw() {
    const draw = await withBusy(() => api.createDraw());
    if (draw) setSuccess(`Создан тираж #${draw.id}`);
  }

  async function handleBuyTicket(drawId: number, numbers: number[]) {
    const ticket = await withBusy(() => api.buyTicket(drawId, numbers));
    if (ticket) {
      setLastTicketId(ticket.id);
      setSuccess(`Билет #${ticket.id} куплен (статус ${ticket.status})`);
    }
  }

  async function handleCompleteDraw(drawId: number) {
    const completed = await withBusy(() => api.completeDraw(drawId));
    if (completed) {
      setLastCompletedDraw(completed);
      setSuccess(
        `Тираж #${completed.id} завершён. Выигрыш: ${formatNumbers(completed.winningNumbers)}`,
      );
    }
  }

  async function handleCheckTicket(ticketId: number): Promise<Ticket> {
    setBusy(true);
    setError(null);
    try {
      return await api.getTicket(ticketId);
    } catch (e) {
      setError(getErrorMessage(e));
      throw e;
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Лотерея</h1>
        <p className="muted">Базовый сценарий: тираж → билет → завершение → результат</p>
      </header>

      {error && <StatusMessage variant="error" message={error} />}
      {success && <StatusMessage variant="success" message={success} />}

      <section className="panel" aria-labelledby="actions-title">
        <h2 id="actions-title">Тиражи</h2>
        <div className="toolbar">
          <button type="button" disabled={busy} onClick={handleCreateDraw}>
            Создать тираж
          </button>
          <button type="button" className="btn-secondary" disabled={busy || loading} onClick={loadDraws}>
            Обновить список
          </button>
        </div>

        {loading && (
          <p className="muted" aria-live="polite">
            Загрузка тиражей…
          </p>
        )}

        {!loading && draws.length === 0 && (
          <p className="empty" role="status">
            Нет активных тиражей. Создайте новый тираж.
          </p>
        )}

        <ul className="draw-list">
          {draws.map((draw) => (
            <li key={draw.id}>
              <DrawCard
                draw={draw}
                busy={busy}
                onBuyTicket={handleBuyTicket}
                onComplete={handleCompleteDraw}
              />
            </li>
          ))}
        </ul>

        {lastCompletedDraw && (
          <aside className="highlight" aria-live="polite">
            Последний завершённый тираж #{lastCompletedDraw.id}:{' '}
            <code>{formatNumbers(lastCompletedDraw.winningNumbers)}</code>
          </aside>
        )}
      </section>

      <TicketChecker busy={busy} onCheck={handleCheckTicket} />

      {lastTicketId !== null && (
        <p className="muted">
          Последний купленный билет: #{lastTicketId} — проверьте его в блоке выше.
        </p>
      )}
    </div>
  );
}
