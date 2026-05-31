package hackathon.lottery.repository;

import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.DrawStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DrawRepository {
    private final DataSource dataSource;

    public DrawRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Draw create(DrawStatus status) {
        String sql = "INSERT INTO draws (status) VALUES (?) RETURNING id, status, winning_numbers, created_at, completed_at";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            throw new SQLException("Insert draw returned no row");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create draw", e);
        }
    }

    public Optional<Draw> findById(long id) {
        String sql = "SELECT id, status, winning_numbers, created_at, completed_at FROM draws WHERE id = ?";
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
            throw new IllegalStateException("Failed to find draw", e);
        }
    }

    public List<Draw> findActive() {
        String sql = "SELECT id, status, winning_numbers, created_at, completed_at FROM draws WHERE status = ? ORDER BY id";
        List<Draw> draws = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, DrawStatus.ACTIVE.name());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    draws.add(mapRow(rs));
                }
            }
            return draws;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list active draws", e);
        }
    }

    public void complete(Connection connection, long drawId, int[] winningNumbers, Instant completedAt)
            throws SQLException {
        String sql = """
                UPDATE draws
                SET status = ?, winning_numbers = ?, completed_at = ?
                WHERE id = ? AND status = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, DrawStatus.COMPLETED.name());
            statement.setArray(2, JdbcUtils.createSqlArray(connection, winningNumbers));
            statement.setTimestamp(3, Timestamp.from(completedAt));
            statement.setLong(4, drawId);
            statement.setString(5, DrawStatus.ACTIVE.name());
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new SQLException("Draw not found or not active");
            }
        }
    }

    public Draw findByIdForUpdate(Connection connection, long id) throws SQLException {
        String sql = "SELECT id, status, winning_numbers, created_at, completed_at FROM draws WHERE id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    private Draw mapRow(ResultSet rs) throws SQLException {
        Draw draw = new Draw();
        draw.setId(rs.getLong("id"));
        draw.setStatus(DrawStatus.valueOf(rs.getString("status")));
        draw.setWinningNumbers(JdbcUtils.toIntArray(rs.getArray("winning_numbers")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        draw.setCreatedAt(createdAt.toInstant());
        Timestamp completedAt = rs.getTimestamp("completed_at");
        if (completedAt != null) {
            draw.setCompletedAt(completedAt.toInstant());
        }
        return draw;
    }
}
