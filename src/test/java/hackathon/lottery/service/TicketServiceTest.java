package hackathon.lottery.service;

import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.DrawStatus;
import hackathon.lottery.domain.Ticket;
import hackathon.lottery.domain.TicketStatus;
import hackathon.lottery.exception.ApiException;
import hackathon.lottery.repository.DrawRepository;
import hackathon.lottery.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicket_succeedsForActiveDraw() {
        Draw draw = activeDraw(1L);
        when(drawRepository.findById(1L)).thenReturn(Optional.of(draw));
        Ticket saved = new Ticket(10L, 1L, new int[]{1, 2, 3, 4, 5, 6}, TicketStatus.PENDING, Instant.now());
        when(ticketRepository.create(eq(1L), any(), eq(TicketStatus.PENDING))).thenReturn(saved);

        Ticket result = ticketService.createTicket(1L, new int[]{6, 5, 4, 3, 2, 1});

        assertEquals(10L, result.getId());
        assertEquals(TicketStatus.PENDING, result.getStatus());
        verify(ticketRepository).create(eq(1L), any(), eq(TicketStatus.PENDING));
    }

    @Test
    void createTicket_rejectsCompletedDraw() {
        Draw draw = new Draw(1L, DrawStatus.COMPLETED, new int[]{1, 2, 3, 4, 5, 6},
                Instant.now(), Instant.now());
        when(drawRepository.findById(1L)).thenReturn(Optional.of(draw));

        ApiException ex = assertThrows(ApiException.class,
                () -> ticketService.createTicket(1L, new int[]{1, 2, 3, 4, 5, 6}));
        assertEquals("DRAW_NOT_ACTIVE", ex.getErrorCode());
    }

    @Test
    void createTicket_rejectsMissingDraw() {
        when(drawRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class,
                () -> ticketService.createTicket(99L, new int[]{1, 2, 3, 4, 5, 6}));
        assertEquals("DRAW_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void getTicket_returnsWinTicket() {
        Ticket ticket = new Ticket(5L, 1L, new int[]{1, 2, 3, 4, 5, 6}, TicketStatus.WIN, Instant.now());
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicket(5L);

        assertEquals(TicketStatus.WIN, result.getStatus());
    }

    private Draw activeDraw(long id) {
        return new Draw(id, DrawStatus.ACTIVE, null, Instant.now(), null);
    }
}
