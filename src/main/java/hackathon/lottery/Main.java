package hackathon.lottery;

import hackathon.lottery.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromEnvironment();
        Application application = new Application(config);
        application.start();
        log.info("Press Ctrl+C to stop");

        Runtime.getRuntime().addShutdownHook(new Thread(application::stop));
    }
}
