import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ParallelSum")
class ParallelSumTest {

    @Test
    @DisplayName("should sum a small range correctly")
    void computeSumsRangeCorrectly() {
        long[] data = {1, 2, 3, 4, 5};
        ParallelSum task = new ParallelSum(data, 0, data.length, ConcurrentHashMap.newKeySet());

        assertEquals(15L, new ForkJoinPool(2).invoke(task));
    }

    @Test
    @DisplayName("should return 0 for an empty range")
    void computeHandlesEmptyRange() {
        long[] data = {1, 2, 3};
        ParallelSum task = new ParallelSum(data, 1, 1, ConcurrentHashMap.newKeySet());

        assertEquals(0L, new ForkJoinPool(2).invoke(task));
    }

    @Test
    @DisplayName("should sum negative and positive values correctly")
    void computeHandlesNegativeValues() {
        long[] data = {-5, 10, -3, 8};
        ParallelSum task = new ParallelSum(data, 0, data.length, ConcurrentHashMap.newKeySet());

        assertEquals(10L, new ForkJoinPool(2).invoke(task));
    }

    @Test
    @DisplayName("should sum only the requested sub-range, ignoring elements outside it")
    void computeSumsOnlyTheGivenSubRange() {
        long[] data = {100, 1, 2, 3, 100};
        ParallelSum task = new ParallelSum(data, 1, 4, ConcurrentHashMap.newKeySet());

        assertEquals(6L, new ForkJoinPool(2).invoke(task));
    }

    @Test
    @DisplayName("should compute the correct total on a large array AND spread leaf work across more than one thread")
    void computeSumsLargeArrayAcrossMultipleThreads() {
        int size = 50_000;
        long[] data = new long[size];
        long expected = 0;
        for (int i = 0; i < size; i++) {
            data[i] = i;
            expected += i;
        }

        Set<String> threadNamesUsed = ConcurrentHashMap.newKeySet();
        ParallelSum task = new ParallelSum(data, 0, data.length, threadNamesUsed);

        long result = new ForkJoinPool(4).invoke(task);

        assertEquals(expected, result);
        assertTrue(threadNamesUsed.size() > 1,
                "expected leaf computations to be spread across more than one thread (real fork()/join() "
                        + "parallelism), but every leaf ran on: " + threadNamesUsed
                        + " - calling compute() directly on both subtasks instead of fork()/join() would still "
                        + "produce the right sum, but only ever uses the one thread that started the task");
    }

    @Test
    @DisplayName("sumOnCommonPool should sum correctly without the caller constructing a ForkJoinPool")
    void sumOnCommonPoolSumsWithoutAnExplicitPool() {
        long[] data = {10, 20, 30, 40};

        assertEquals(100L, ParallelSum.sumOnCommonPool(data));
    }
}
