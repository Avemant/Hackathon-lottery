package hackathon.lottery.repository;

import hackathon.lottery.domain.Ticket;
import hackathon.lottery.domain.TicketStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketRepository {
    private final DataSource dataSource;

    public TicketRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ticket create(long drawId, int[] numbers, TicketStatus status) {
        String sql = "INSERT INTO tickets (draw_id, numbers, status) VALUES (?, ?, ?) RETURNING id, draw_id, numbers, status, created_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, drawId);
            statement.setArray(2, JdbcUtils.createSqlArray(connection, numbers));
            statement.setString(3, status.name());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            throw new SQLException("Insert ticket returned no row");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create ticket", e);
        }
    }

    public Optional<Ticket> findById(long id) {
        String sql = "SELECT id, draw_id, numbers, status, created_at FROM tickets WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find ticket", e);
        }
    }

    public List<Ticket> findByDrawId(Connection connection, long drawId) throws SQLException {
        String sql = "SELECT id, draw_id, numbers, status, created_at FROM tickets WHERE draw_id = ?";
        List<Ticket> tickets = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, drawId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
        }
        return tickets;
    }

    public void updateStatus(Connection connection, long ticketId, TicketStatus status) throws SQLException {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, ticketId);
            statement.executeUpdate();
        }
    }

    private Ticket mapRow(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setDrawId(rs.getLong("draw_id"));
        ticket.setNumbers(JdbcUtils.toIntArray(rs.getArray("numbers")));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        ticket.setCreatedAt(createdAt.toInstant());
        return ticket;
    }
}
