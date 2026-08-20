import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

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
            threadNamesUsed.add(Thread.currentThread().getName());
            long sum = 0;
            for (int i = start; i < end; i++) {
                sum += data[i];
            }
            return sum;
        }

        int middle = (start + end) / 2;
        ParallelSum left = new ParallelSum(data, start, middle, threadNamesUsed);
        ParallelSum right = new ParallelSum(data, middle, end, threadNamesUsed);

        left.fork();
        long rightResult = right.compute();
        long leftResult = left.join();

        return leftResult + rightResult;
    }

    public static long sumOnCommonPool(long[] data) {
        ParallelSum task = new ParallelSum(data, 0, data.length, ConcurrentHashMap.newKeySet());
        return task.invoke();
    }
}
