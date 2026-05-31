import type { ApiError, Draw, Ticket } from '../types';

const API_BASE = import.meta.env.VITE_API_URL ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });

  if (!response.ok) {
    let error: ApiError = { errorCode: 'UNKNOWN', message: response.statusText };
    try {
      error = (await response.json()) as ApiError;
    } catch {
      /* empty body */
    }
    throw error;
  }

  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

export const api = {
  createDraw: () => request<Draw>('/draws', { method: 'POST' }),
  listActiveDraws: () => request<Draw[]>('/draws'),
  buyTicket: (drawId: number, numbers: number[]) =>
    request<Ticket>(`/draws/${drawId}/tickets`, {
      method: 'POST',
      body: JSON.stringify({ numbers }),
    }),
  completeDraw: (drawId: number) =>
    request<Draw>(`/draws/${drawId}/complete`, { method: 'POST' }),
  getTicket: (ticketId: number) => request<Ticket>(`/tickets/${ticketId}`),
};
