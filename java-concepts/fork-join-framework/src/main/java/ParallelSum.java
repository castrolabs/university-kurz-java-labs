import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Sums a range of a {@code long[]} using a divide-and-conquer Fork/Join
 * task. {@code threadNamesUsed} is instrumentation for the tests: every leaf
 * computation records which thread it ran on, which is how the tests can
 * tell a genuinely parallel implementation from one that just calls
 * compute() directly on both halves and never actually forks anything.
 */
public class ParallelSum extends RecursiveTask<Long> {

    static final int SEQUENTIAL_THRESHOLD = 500;

    private final long[] data;
    private final int start;
    private final int end;
    private final Set<String> threadNamesUsed;

    public ParallelSum(long[] data, int start, int end, Set<String> threadNamesUsed) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.threadNamesUsed = threadNamesUsed;
    }

    @Override
    protected Long compute() {
        if ((end - start) <= SEQUENTIAL_THRESHOLD) {
            // TODO-00: Record Thread.currentThread().getName() into
            // threadNamesUsed, then sum data[start] through data[end - 1]
            // (exclusive of end) sequentially and return the total.
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        int middle = (start + end) / 2;
        ParallelSum left = new ParallelSum(data, start, middle, threadNamesUsed);
        ParallelSum right = new ParallelSum(data, middle, end, threadNamesUsed);

        // TODO-01: Run `left` and `right` so they can execute concurrently -
        // fork one of them (ForkJoinTask.fork()), compute or fork the other,
        // then join() each and add the two results together. Calling
        // compute() directly on both subtasks instead of fork()/join() would
        // still return the right sum, but it runs entirely on this one
        // thread - the parallelism test below exists specifically to catch
        // that shortcut.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Sums the whole array without the caller having to construct a
     * ForkJoinPool - the top-level fork()/invoke() call routes through
     * ForkJoinPool.commonPool() automatically.
     */
    public static long sumOnCommonPool(long[] data) {
        // TODO-04 (optional): Build a ParallelSum covering the whole array
        // (with a throwaway Set for threadNamesUsed, e.g.
        // ConcurrentHashMap.newKeySet()) and return task.invoke() - no
        // `new ForkJoinPool()` needed.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
