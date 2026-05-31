import { useState } from 'react';
import type { Ticket } from '../types';
import { formatNumbers } from '../utils/numbers';
type TicketCheckerProps = {
  onCheck: (ticketId: number) => Promise<Ticket>;
  busy: boolean;
};

const statusLabels: Record<Ticket['status'], string> = {
  PENDING: 'Ожидает тиража',
  WIN: 'Выигрыш',
  LOSE: 'Проигрыш',
};

export function TicketChecker({ onCheck, busy }: TicketCheckerProps) {
  const [ticketId, setTicketId] = useState('');
  const [ticket, setTicket] = useState<Ticket | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setTicket(null);
    const id = Number(ticketId);
    if (!Number.isInteger(id) || id < 1) return;
    const result = await onCheck(id);
    setTicket(result);
  }

  return (
    <section className="panel" aria-labelledby="ticket-check-title">
      <h2 id="ticket-check-title">Проверка билета</h2>
      <form className="inline-form" onSubmit={handleSubmit}>
        <label htmlFor="ticket-id">ID билета</label>
        <input
          id="ticket-id"
          type="number"
          min={1}
          value={ticketId}
          onChange={(e) => setTicketId(e.target.value)}
          placeholder="1"
          disabled={busy}
          required
        />
        <button type="submit" disabled={busy || !ticketId}>
          Проверить
        </button>
      </form>

      {ticket && (
        <div className="ticket-result" role="region" aria-live="polite">
          <p>
            <strong>Билет #{ticket.id}</strong> · тираж #{ticket.drawId}
          </p>
          <p>
            Комбинация: <code>{formatNumbers(ticket.numbers)}</code>
          </p>
          <p>
            Статус:{' '}
            <span className={`badge badge--${ticket.status.toLowerCase()}`}>
              {ticket.status} — {statusLabels[ticket.status]}
            </span>
          </p>
        </div>
      )}
    </section>
  );
}
