export type DrawStatus = 'ACTIVE' | 'COMPLETED';
export type TicketStatus = 'PENDING' | 'WIN' | 'LOSE';

export interface Draw {
  id: number;
  status: DrawStatus;
  winningNumbers: number[] | null;
  createdAt: string;
  completedAt: string | null;
}

export interface Ticket {
  id: number;
  drawId: number;
  numbers: number[];
  status: TicketStatus;
  createdAt: string;
}

export interface ApiError {
  errorCode: string;
  message: string;
}
