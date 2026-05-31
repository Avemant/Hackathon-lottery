package hackathon.lottery.util;

import hackathon.lottery.exception.ApiException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class CombinationValidator {
    public static final int COMBINATION_SIZE = 6;
    public static final int MIN_NUMBER = 1;
    public static final int MAX_NUMBER = 49;

    private CombinationValidator() {
    }

    public static int[] validateAndNormalize(int[] numbers) {
        if (numbers == null || numbers.length != COMBINATION_SIZE) {
            throw new ApiException(400, "INVALID_COMBINATION",
                    "Combination must contain exactly " + COMBINATION_SIZE + " numbers");
        }
        Set<Integer> unique = new HashSet<>();
        for (int number : numbers) {
            if (number < MIN_NUMBER || number > MAX_NUMBER) {
                throw new ApiException(400, "INVALID_COMBINATION",
                        "Each number must be between " + MIN_NUMBER + " and " + MAX_NUMBER);
            }
            if (!unique.add(number)) {
                throw new ApiException(400, "INVALID_COMBINATION", "Combination numbers must be unique");
            }
        }
        int[] normalized = Arrays.copyOf(numbers, numbers.length);
        Arrays.sort(normalized);
        return normalized;
    }

    public static boolean matches(int[] ticketNumbers, int[] winningNumbers) {
        if (ticketNumbers == null || winningNumbers == null) {
            return false;
        }
        int[] a = Arrays.copyOf(ticketNumbers, ticketNumbers.length);
        int[] b = Arrays.copyOf(winningNumbers, winningNumbers.length);
        Arrays.sort(a);
        Arrays.sort(b);
        return Arrays.equals(a, b);
    }
}
