import java.util.concurrent.RecursiveAction;

/**
 * Squares every element of a {@code long[]} in place, splitting the range in
 * half at each level until a chunk is small enough to just compute
 * directly - the same divide-and-conquer shape as ParallelSum, but for a
 * task that produces no result to combine.
 */
public class ParallelSquare extends RecursiveAction {

    static final int SEQUENTIAL_THRESHOLD = 500;

    private final long[] data;
    private final int start;
    private final int end;

    public ParallelSquare(long[] data, int start, int end) {
        this.data = data;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if ((end - start) <= SEQUENTIAL_THRESHOLD) {
            // TODO-02: Square every element in data[start] through
            // data[end - 1] (exclusive of end) in place, sequentially.
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        int middle = (start + end) / 2;
        ParallelSquare left = new ParallelSquare(data, start, middle);
        ParallelSquare right = new ParallelSquare(data, middle, end);

        // TODO-03: Run `left` and `right` concurrently and wait for both to
        // finish before this call returns. RecursiveAction has no result to
        // combine, so ForkJoinTask.invokeAll(left, right) - which forks and
        // joins both in one call - fits here better than a separate
        // fork()/join() pair.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
