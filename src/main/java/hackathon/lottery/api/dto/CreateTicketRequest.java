package hackathon.lottery.api.dto;

import java.util.List;

public class CreateTicketRequest {
    private List<Integer> numbers;

    public CreateTicketRequest() {
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }
}
