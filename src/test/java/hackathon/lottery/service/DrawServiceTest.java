package hackathon.lottery.service;

import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.DrawStatus;
import hackathon.lottery.domain.Ticket;
import hackathon.lottery.domain.TicketStatus;
import hackathon.lottery.exception.ApiException;
import hackathon.lottery.repository.DrawRepository;
import hackathon.lottery.repository.TicketRepository;
import hackathon.lottery.util.RandomCombinationGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private DrawRepository drawRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private RandomCombinationGenerator combinationGenerator;

    private DrawService drawService;

    @BeforeEach
    void setUp() {
        drawService = new DrawService(drawRepository, ticketRepository, dataSource, combinationGenerator);
    }

    private void stubTransaction() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        doAnswer(invocation -> null).when(connection).setAutoCommit(any(Boolean.class));
    }

    @Test
    void createDraw_setsActiveStatus() {
        Draw created = new Draw(1L, DrawStatus.ACTIVE, null, Instant.now(), null);
        when(drawRepository.create(DrawStatus.ACTIVE)).thenReturn(created);

        Draw result = drawService.createDraw();

        assertEquals(DrawStatus.ACTIVE, result.getStatus());
    }

    @Test
    void completeDraw_rejectsAlreadyCompleted() throws Exception {
        stubTransaction();
        Draw draw = new Draw(1L, DrawStatus.COMPLETED, new int[]{1, 2, 3, 4, 5, 6},
                Instant.now(), Instant.now());
        when(drawRepository.findByIdForUpdate(connection, 1L)).thenReturn(draw);

        ApiException ex = assertThrows(ApiException.class, () -> drawService.completeDraw(1L));
        assertEquals("DRAW_NOT_ACTIVE", ex.getErrorCode());
        verify(connection).rollback();
    }

    @Test
    void completeDraw_updatesTicketsToWinOrLose() throws Exception {
        stubTransaction();
        when(combinationGenerator.generate()).thenReturn(new int[]{1, 2, 3, 4, 5, 6});
        Draw draw = new Draw(1L, DrawStatus.ACTIVE, null, Instant.now(), null);
        when(drawRepository.findByIdForUpdate(connection, 1L)).thenReturn(draw);

        Ticket winner = new Ticket(1L, 1L, new int[]{1, 2, 3, 4, 5, 6}, TicketStatus.PENDING, Instant.now());
        Ticket loser = new Ticket(2L, 1L, new int[]{7, 8, 9, 10, 11, 12}, TicketStatus.PENDING, Instant.now());
        when(ticketRepository.findByDrawId(connection, 1L)).thenReturn(List.of(winner, loser));

        Draw result = drawService.completeDraw(1L);

        assertEquals(DrawStatus.COMPLETED, result.getStatus());
        assertEquals(6, result.getWinningNumbers().length);
        verify(drawRepository).complete(eq(connection), eq(1L), any(), any());
        verify(ticketRepository).updateStatus(connection, 1L, TicketStatus.WIN);
        verify(ticketRepository).updateStatus(connection, 2L, TicketStatus.LOSE);
        verify(connection).commit();
    }

    @Test
    void completeDraw_throwsWhenDrawMissing() throws Exception {
        stubTransaction();
        when(drawRepository.findByIdForUpdate(connection, 99L)).thenReturn(null);

        ApiException ex = assertThrows(ApiException.class, () -> drawService.completeDraw(99L));
        assertEquals("DRAW_NOT_FOUND", ex.getErrorCode());
        verify(connection).rollback();
    }

    @Test
    void getDraw_throwsWhenMissing() {
        when(drawRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> drawService.getDraw(42L));
    }
}
