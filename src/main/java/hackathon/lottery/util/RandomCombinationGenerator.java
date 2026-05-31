package hackathon.lottery.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomCombinationGenerator {
    private final Random random;

    public RandomCombinationGenerator() {
        this(new Random());
    }

    public RandomCombinationGenerator(Random random) {
        this.random = random;
    }

    public int[] generate() {
        List<Integer> pool = new ArrayList<>();
        for (int i = CombinationValidator.MIN_NUMBER; i <= CombinationValidator.MAX_NUMBER; i++) {
            pool.add(i);
        }
        Collections.shuffle(pool, random);
        int[] result = new int[CombinationValidator.COMBINATION_SIZE];
        for (int i = 0; i < CombinationValidator.COMBINATION_SIZE; i++) {
            result[i] = pool.get(i);
        }
        Arrays.sort(result);
        return result;
    }
}
