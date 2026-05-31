package hackathon.lottery.domain;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Ticket {
    private Long id;
    private Long drawId;
    private int[] numbers;
    private TicketStatus status;
    private Instant createdAt;

    public Ticket() {
    }

    public Ticket(Long id, Long drawId, int[] numbers, TicketStatus status, Instant createdAt) {
        this.id = id;
        this.drawId = drawId;
        this.numbers = numbers;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDrawId() {
        return drawId;
    }

    public void setDrawId(Long drawId) {
        this.drawId = drawId;
    }

    public int[] getNumbers() {
        return numbers;
    }

    public void setNumbers(int[] numbers) {
        this.numbers = numbers;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<Integer> getNumbersList() {
        return Arrays.stream(numbers).boxed().toList();
    }
}
