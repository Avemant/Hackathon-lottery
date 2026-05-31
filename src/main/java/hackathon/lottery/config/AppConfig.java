package hackathon.lottery.config;

public record AppConfig(int serverPort, DatabaseConfig database) {
    public static AppConfig fromEnvironment() {
        int port = Integer.parseInt(getenv("SERVER_PORT", "8080"));
        return new AppConfig(port, DatabaseConfig.fromEnvironment());
    }

    private static String getenv(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
