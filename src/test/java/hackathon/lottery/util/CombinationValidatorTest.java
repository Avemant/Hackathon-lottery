package hackathon.lottery.util;

import hackathon.lottery.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinationValidatorTest {

    @Test
    void validateAndNormalize_sortsNumbers() {
        int[] result = CombinationValidator.validateAndNormalize(new int[]{49, 1, 10, 20, 30, 40});
        assertArrayEquals(new int[]{1, 10, 20, 30, 40, 49}, result);
    }

    @Test
    void validateAndNormalize_rejectsWrongSize() {
        ApiException ex = assertThrows(ApiException.class,
                () -> CombinationValidator.validateAndNormalize(new int[]{1, 2, 3}));
        assertTrue(ex.getMessage().contains("exactly"));
    }

    @Test
    void validateAndNormalize_rejectsDuplicates() {
        assertThrows(ApiException.class,
                () -> CombinationValidator.validateAndNormalize(new int[]{1, 1, 2, 3, 4, 5}));
    }

    @Test
    void validateAndNormalize_rejectsOutOfRange() {
        assertThrows(ApiException.class,
                () -> CombinationValidator.validateAndNormalize(new int[]{0, 2, 3, 4, 5, 6}));
        assertThrows(ApiException.class,
                () -> CombinationValidator.validateAndNormalize(new int[]{1, 2, 3, 4, 5, 50}));
    }

    @Test
    void matches_ignoresOrder() {
        assertTrue(CombinationValidator.matches(
                new int[]{6, 5, 4, 3, 2, 1},
                new int[]{1, 2, 3, 4, 5, 6}));
    }

    @Test
    void matches_returnsFalseWhenDifferent() {
        assertFalse(CombinationValidator.matches(
                new int[]{1, 2, 3, 4, 5, 6},
                new int[]{1, 2, 3, 4, 5, 7}));
    }
}
