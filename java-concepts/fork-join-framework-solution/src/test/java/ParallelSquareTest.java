import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@DisplayName("ParallelSquare")
class ParallelSquareTest {

    @Test
    @DisplayName("should square every element of a small array in place")
    void computeSquaresElementsInPlace() {
        long[] data = {1, 2, 3, 4, 5};

        new ForkJoinPool(2).invoke(new ParallelSquare(data, 0, data.length));

        assertArrayEquals(new long[]{1, 4, 9, 16, 25}, data);
    }

    @Test
    @DisplayName("should leave the array untouched for an empty range")
    void computeHandlesEmptyRange() {
        long[] data = {1, 2, 3};
        long[] expected = data.clone();

        new ForkJoinPool(2).invoke(new ParallelSquare(data, 1, 1));

        assertArrayEquals(expected, data);
    }

    @Test
    @DisplayName("should square negative values into positive squares")
    void computeHandlesNegativeValues() {
        long[] data = {-3, -2, 0, 4};

        new ForkJoinPool(2).invoke(new ParallelSquare(data, 0, data.length));

        assertArrayEquals(new long[]{9, 4, 0, 16}, data);
    }

    @Test
    @DisplayName("should square only the requested sub-range, leaving elements outside it untouched")
    void computeSquaresOnlyTheGivenSubRange() {
        long[] data = {100, 2, 3, 4, 100};

        new ForkJoinPool(2).invoke(new ParallelSquare(data, 1, 4));

        assertArrayEquals(new long[]{100, 4, 9, 16, 100}, data);
    }

    @Test
    @DisplayName("should produce correct results on an array large enough to cross the sequential threshold many times")
    void computeHandlesLargeArrayAcrossTheSplitThreshold() {
        int size = 10_000;
        long[] data = new long[size];
        long[] expected = new long[size];
        for (int i = 0; i < size; i++) {
            data[i] = i;
            expected[i] = (long) i * i;
        }

        new ForkJoinPool(4).invoke(new ParallelSquare(data, 0, data.length));

        assertArrayEquals(expected, data);
    }
}
