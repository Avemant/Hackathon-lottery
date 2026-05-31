package hackathon.lottery.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomCombinationGeneratorTest {

    @Test
    void generate_producesSixUniqueNumbersInRange() {
        RandomCombinationGenerator generator = new RandomCombinationGenerator(new Random(42));
        int[] numbers = generator.generate();

        assertEquals(CombinationValidator.COMBINATION_SIZE, numbers.length);
        HashSet<Integer> unique = new HashSet<>();
        for (int number : numbers) {
            assertTrue(number >= CombinationValidator.MIN_NUMBER);
            assertTrue(number <= CombinationValidator.MAX_NUMBER);
            unique.add(number);
        }
        assertEquals(CombinationValidator.COMBINATION_SIZE, unique.size());
    }
}
