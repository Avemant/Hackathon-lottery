package hackathon.lottery.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import hackathon.lottery.api.dto.CreateTicketRequest;
import hackathon.lottery.api.dto.DrawResponse;
import hackathon.lottery.api.dto.TicketResponse;
import hackathon.lottery.domain.Draw;
import hackathon.lottery.domain.Ticket;
import hackathon.lottery.exception.ApiException;
import hackathon.lottery.service.DrawService;
import hackathon.lottery.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LotteryHttpHandler implements HttpHandler {
    private static final Logger log = LoggerFactory.getLogger(LotteryHttpHandler.class);

    private static final Pattern TICKET_PATTERN =
            Pattern.compile("^/draws/(\\d+)/tickets$");
    private static final Pattern COMPLETE_PATTERN =
            Pattern.compile("^/draws/(\\d+)/complete$");
    private static final Pattern TICKET_BY_ID_PATTERN =
            Pattern.compile("^/tickets/(\\d+)$");

    private final DrawService drawService;
    private final TicketService ticketService;
    private final HttpResponseWriter responseWriter;
    private final ObjectMapper objectMapper;

    public LotteryHttpHandler(DrawService drawService,
                              TicketService ticketService,
                              ObjectMapper objectMapper) {
        this.drawService = drawService;
        this.ticketService = ticketService;
        this.objectMapper = objectMapper;
        this.responseWriter = new HttpResponseWriter(objectMapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCors(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            addCors(exchange);
            route(exchange);
        } catch (ApiException e) {
            responseWriter.sendError(exchange, e);
        } catch (JsonProcessingException e) {
            responseWriter.sendError(exchange,
                    new ApiException(400, "INVALID_JSON", "Invalid JSON body"));
        } catch (Exception e) {
            log.error("Unhandled error", e);
            responseWriter.sendInternalError(exchange, e);
        } finally {
            exchange.close();
        }
    }

    private void route(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("/draws".equals(path) && "POST".equals(method)) {
            Draw draw = drawService.createDraw();
            responseWriter.sendJson(exchange, 201, DrawResponse.from(draw));
            return;
        }
        if ("/draws".equals(path) && "GET".equals(method)) {
            List<DrawResponse> draws = drawService.listActiveDraws().stream()
                    .map(DrawResponse::from)
                    .toList();
            responseWriter.sendJson(exchange, 200, draws);
            return;
        }

        Matcher ticketMatcher = TICKET_PATTERN.matcher(path);
        if (ticketMatcher.matches() && "POST".equals(method)) {
            long drawId = Long.parseLong(ticketMatcher.group(1));
            CreateTicketRequest request = parseBody(exchange, CreateTicketRequest.class);
            int[] numbers = toIntArray(request);
            Ticket ticket = ticketService.createTicket(drawId, numbers);
            responseWriter.sendJson(exchange, 201, TicketResponse.from(ticket));
            return;
        }

        Matcher completeMatcher = COMPLETE_PATTERN.matcher(path);
        if (completeMatcher.matches() && "POST".equals(method)) {
            long drawId = Long.parseLong(completeMatcher.group(1));
            Draw draw = drawService.completeDraw(drawId);
            responseWriter.sendJson(exchange, 200, DrawResponse.from(draw));
            return;
        }

        Matcher ticketByIdMatcher = TICKET_BY_ID_PATTERN.matcher(path);
        if (ticketByIdMatcher.matches() && "GET".equals(method)) {
            long ticketId = Long.parseLong(ticketByIdMatcher.group(1));
            Ticket ticket = ticketService.getTicket(ticketId);
            responseWriter.sendJson(exchange, 200, TicketResponse.from(ticket));
            return;
        }

        throw new ApiException(404, "NOT_FOUND", "Endpoint not found: " + method + " " + path);
    }

    private <T> T parseBody(HttpExchange exchange, Class<T> type) throws IOException {
        String body = responseWriter.readBody(exchange);
        if (body.isBlank()) {
            throw new ApiException(400, "INVALID_REQUEST", "Request body is required");
        }
        return objectMapper.readValue(body, type);
    }

    private int[] toIntArray(CreateTicketRequest request) {
        if (request.getNumbers() == null) {
            throw new ApiException(400, "INVALID_REQUEST", "Field 'numbers' is required");
        }
        return request.getNumbers().stream().mapToInt(Integer::intValue).toArray();
    }

    private void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
}
