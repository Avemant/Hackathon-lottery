package hackathon.lottery.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class DatabaseInitializer {
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    public void initialize(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : loadStatements()) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
            log.info("Database schema initialized");
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        }
    }

    private String[] loadStatements() throws IOException {
        try (InputStream stream = DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("schema.sql")) {
            if (stream == null) {
                throw new IOException("schema.sql not found on classpath");
            }
            String schema = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.stream(schema.split(";"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }
    }
}
