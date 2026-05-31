package hackathon.lottery.config;

public record DatabaseConfig(String host, int port, String database, String user, String password) {
    public String jdbcUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + database;
    }

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                getenv("DB_HOST", "localhost"),
                Integer.parseInt(getenv("DB_PORT", "5432")),
                getenv("DB_NAME", "postgres"),
                getenv("DB_USER", "postgres"),
                getenv("DB_PASSWORD", "123321")
        );
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
