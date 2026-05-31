package hackathon.lottery.service;

import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.DrawStatus;
import hackathon.lottery.domain.Ticket;
import hackathon.lottery.domain.TicketStatus;
import hackathon.lottery.exception.ApiException;
import hackathon.lottery.repository.DrawRepository;
import hackathon.lottery.repository.TicketRepository;
import hackathon.lottery.util.CombinationValidator;
import hackathon.lottery.util.RandomCombinationGenerator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class DrawService {
    private final DrawRepository drawRepository;
    private final TicketRepository ticketRepository;
    private final DataSource dataSource;
    private final RandomCombinationGenerator combinationGenerator;

    public DrawService(DrawRepository drawRepository,
                       TicketRepository ticketRepository,
                       DataSource dataSource,
                       RandomCombinationGenerator combinationGenerator) {
        this.drawRepository = drawRepository;
        this.ticketRepository = ticketRepository;
        this.dataSource = dataSource;
        this.combinationGenerator = combinationGenerator;
    }

    public Draw createDraw() {
        return drawRepository.create(DrawStatus.ACTIVE);
    }

    public List<Draw> listActiveDraws() {
        return drawRepository.findActive();
    }

    public Draw getDraw(long drawId) {
        return drawRepository.findById(drawId)
                .orElseThrow(() -> new ApiException(404, "DRAW_NOT_FOUND", "Draw not found: " + drawId));
    }

    public Draw completeDraw(long drawId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Draw draw = drawRepository.findByIdForUpdate(connection, drawId);
                if (draw == null) {
                    throw new ApiException(404, "DRAW_NOT_FOUND", "Draw not found: " + drawId);
                }
                if (draw.getStatus() != DrawStatus.ACTIVE) {
                    throw new ApiException(409, "DRAW_NOT_ACTIVE", "Draw is already completed");
                }

                int[] winningNumbers = combinationGenerator.generate();
                Instant completedAt = Instant.now();
                drawRepository.complete(connection, drawId, winningNumbers, completedAt);

                List<Ticket> tickets = ticketRepository.findByDrawId(connection, drawId);
                for (Ticket ticket : tickets) {
                    TicketStatus newStatus = CombinationValidator.matches(ticket.getNumbers(), winningNumbers)
                            ? TicketStatus.WIN
                            : TicketStatus.LOSE;
                    ticketRepository.updateStatus(connection, ticket.getId(), newStatus);
                }

                connection.commit();

                draw.setStatus(DrawStatus.COMPLETED);
                draw.setWinningNumbers(winningNumbers);
                draw.setCompletedAt(completedAt);
                return draw;
            } catch (ApiException e) {
                connection.rollback();
                throw e;
            } catch (Exception e) {
                connection.rollback();
                throw new IllegalStateException("Failed to complete draw", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database error while completing draw", e);
        }
    }
}
