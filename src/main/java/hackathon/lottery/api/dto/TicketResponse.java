package hackathon.lottery.api.dto;

import hackathon.lottery.domain.Ticket;

import java.time.Instant;
import java.util.List;

public record TicketResponse(
        Long id,
        Long drawId,
        List<Integer> numbers,
        String status,
        Instant createdAt
) {
    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getDrawId(),
                ticket.getNumbersList(),
                ticket.getStatus().name(),
                ticket.getCreatedAt()
        );
    }
}
