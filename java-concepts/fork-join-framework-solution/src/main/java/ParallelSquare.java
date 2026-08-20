import java.util.concurrent.RecursiveAction;

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
            for (int i = start; i < end; i++) {
                data[i] = data[i] * data[i];
            }
            return;
        }

        int middle = (start + end) / 2;
        invokeAll(
                new ParallelSquare(data, start, middle),
                new ParallelSquare(data, middle, end));
    }
}
