package hackathon.lottery.service;

import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.DrawStatus;
import hackathon.lottery.domain.Ticket;
import hackathon.lottery.domain.TicketStatus;
import hackathon.lottery.exception.ApiException;
import hackathon.lottery.repository.DrawRepository;
import hackathon.lottery.repository.TicketRepository;
import hackathon.lottery.util.CombinationValidator;

public class TicketService {
    private final DrawRepository drawRepository;
    private final TicketRepository ticketRepository;

    public TicketService(DrawRepository drawRepository, TicketRepository ticketRepository) {
        this.drawRepository = drawRepository;
        this.ticketRepository = ticketRepository;
    }

    public Ticket createTicket(long drawId, int[] numbers) {
        Draw draw = drawRepository.findById(drawId)
                .orElseThrow(() -> new ApiException(404, "DRAW_NOT_FOUND", "Draw not found: " + drawId));
        if (draw.getStatus() != DrawStatus.ACTIVE) {
            throw new ApiException(409, "DRAW_NOT_ACTIVE", "Cannot buy ticket for completed draw");
        }
        int[] normalized = CombinationValidator.validateAndNormalize(numbers);
        return ticketRepository.create(drawId, normalized, TicketStatus.PENDING);
    }

    public Ticket getTicket(long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(404, "TICKET_NOT_FOUND", "Ticket not found: " + ticketId));
        if (ticket.getStatus() == TicketStatus.PENDING) {
            Draw draw = drawRepository.findById(ticket.getDrawId())
                    .orElseThrow(() -> new ApiException(404, "DRAW_NOT_FOUND", "Draw not found for ticket"));
            if (draw.getStatus() == DrawStatus.COMPLETED) {
                throw new ApiException(500, "INCONSISTENT_STATE",
                        "Draw completed but ticket status is still PENDING");
            }
        }
        return ticket;
    }
}
