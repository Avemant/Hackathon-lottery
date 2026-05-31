package hackathon.lottery.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hackathon.lottery.config.DatabaseConfig;

import javax.sql.DataSource;

public final class DataSourceFactory {
    private DataSourceFactory() {
    }

    public static DataSource create(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.jdbcUrl());
        hikariConfig.setUsername(config.user());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(5_000);
        return new HikariDataSource(hikariConfig);
    }
}
