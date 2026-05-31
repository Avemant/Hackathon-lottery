package hackathon.lottery.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Draw {
    private Long id;
    private DrawStatus status;
    private int[] winningNumbers;
    private Instant createdAt;
    private Instant completedAt;

    public Draw() {
    }

    public Draw(Long id, DrawStatus status, int[] winningNumbers, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.status = status;
        this.winningNumbers = winningNumbers;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DrawStatus getStatus() {
        return status;
    }

    public void setStatus(DrawStatus status) {
        this.status = status;
    }

    public int[] getWinningNumbers() {
        return winningNumbers;
    }

    public void setWinningNumbers(int[] winningNumbers) {
        this.winningNumbers = winningNumbers;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<Integer> getWinningNumbersList() {
        if (winningNumbers == null) {
            return null;
        }
        return Arrays.stream(winningNumbers).boxed().toList();
    }
}
