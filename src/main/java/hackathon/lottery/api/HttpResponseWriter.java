package hackathon.lottery.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import hackathon.lottery.api.dto.ErrorResponse;
import hackathon.lottery.exception.ApiException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class HttpResponseWriter {
    private final ObjectMapper objectMapper;

    public HttpResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    public void sendError(HttpExchange exchange, ApiException exception) throws IOException {
        sendJson(exchange, exception.getHttpStatus(),
                new ErrorResponse(exception.getErrorCode(), exception.getMessage()));
    }

    public void sendInternalError(HttpExchange exchange, Exception exception) throws IOException {
        byte[] payload = objectMapper.writeValueAsBytes(
                new ErrorResponse("INTERNAL_ERROR", exception.getMessage()));
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(500, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    public String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
}
