package hackathon.lottery;

import com.sun.net.httpserver.HttpServer;
import hackathon.lottery.api.JsonMapper;
import hackathon.lottery.api.LotteryHttpHandler;
import hackathon.lottery.config.AppConfig;
import hackathon.lottery.db.DataSourceFactory;
import hackathon.lottery.db.DatabaseInitializer;
import hackathon.lottery.repository.DrawRepository;
import hackathon.lottery.repository.TicketRepository;
import hackathon.lottery.service.DrawService;
import hackathon.lottery.service.TicketService;
import hackathon.lottery.util.RandomCombinationGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private final HttpServer httpServer;

    public Application(AppConfig config) {
        DataSource dataSource = DataSourceFactory.create(config.database());
        new DatabaseInitializer().initialize(dataSource);

        DrawRepository drawRepository = new DrawRepository(dataSource);
        TicketRepository ticketRepository = new TicketRepository(dataSource);
        DrawService drawService = new DrawService(
                drawRepository, ticketRepository, dataSource, new RandomCombinationGenerator());
        TicketService ticketService = new TicketService(drawRepository, ticketRepository);

        LotteryHttpHandler handler = new LotteryHttpHandler(
                drawService, ticketService, JsonMapper.instance());

        try {
            httpServer = HttpServer.create(new InetSocketAddress(config.serverPort()), 0);
            httpServer.createContext("/", handler);
            httpServer.setExecutor(Executors.newFixedThreadPool(8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start HTTP server", e);
        }
    }

    public void start() {
        httpServer.start();
        log.info("Lottery API started on port {}", httpServer.getAddress().getPort());
    }

    public void stop() {
        httpServer.stop(0);
    }

    public int getPort() {
        return httpServer.getAddress().getPort();
    }
}
