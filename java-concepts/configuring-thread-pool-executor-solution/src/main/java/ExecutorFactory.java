import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorFactory {

    public static ThreadPoolExecutor scalingPool(int coreSize, int maxSize, int queueCapacity) {
        return new ThreadPoolExecutor(
                coreSize, maxSize,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity));
    }

    public static ThreadPoolExecutor fixedCorePool(int coreSize) {
        return new ThreadPoolExecutor(
                coreSize, coreSize,
                0, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    public static ThreadPoolExecutor abortingPool(int coreSize, int maxSize, int queueCapacity) {
        return new ThreadPoolExecutor(
                coreSize, maxSize,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static ThreadPoolExecutor callerRunsPool(int coreSize, int maxSize, int queueCapacity) {
        return new ThreadPoolExecutor(
                coreSize, maxSize,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public static ThreadPoolExecutor namedThreadPool(int coreSize, int maxSize, int queueCapacity, String poolName) {
        AtomicInteger threadNumber = new AtomicInteger(0);
        ThreadFactory threadFactory = runnable -> new Thread(runnable, poolName + "-" + threadNumber.incrementAndGet());

        return new ThreadPoolExecutor(
                coreSize, maxSize,
                30, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }
}
