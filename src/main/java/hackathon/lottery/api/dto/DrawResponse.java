package hackathon.lottery.api.dto;

import hackathon.lottery.domain.Draw;

import java.time.Instant;
import java.util.List;

public record DrawResponse(
        Long id,
        String status,
        List<Integer> winningNumbers,
        Instant createdAt,
        Instant completedAt
) {
    public static DrawResponse from(Draw draw) {
        return new DrawResponse(
                draw.getId(),
                draw.getStatus().name(),
                draw.getWinningNumbersList(),
                draw.getCreatedAt(),
                draw.getCompletedAt()
        );
    }
}
