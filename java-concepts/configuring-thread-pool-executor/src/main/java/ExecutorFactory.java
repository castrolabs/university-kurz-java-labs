import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorFactory {

    public static ThreadPoolExecutor scalingPool(int coreSize, int maxSize, int queueCapacity) {
        // TODO-00: Build a ThreadPoolExecutor with `coreSize` core threads,
        // `maxSize` maximum threads, a 30-second keep-alive, and a BOUNDED
        // work queue (a new ArrayBlockingQueue<Runnable>(queueCapacity)).
        // With a bounded queue, once the queue is full the pool grows past
        // corePoolSize (up to maxSize) instead of only ever queuing tasks.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static ThreadPoolExecutor fixedCorePool(int coreSize) {
        // TODO-01: Build a ThreadPoolExecutor with `coreSize` as BOTH the
        // core and maximum pool size, a 0ms keep-alive (irrelevant here,
        // since core threads are never reaped by default), and an UNBOUNDED
        // work queue (new LinkedBlockingQueue<Runnable>(), no capacity
        // argument). Tasks beyond `coreSize` queue up instead of ever
        // spawning another thread.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static ThreadPoolExecutor abortingPool(int coreSize, int maxSize, int queueCapacity) {
        // TODO-02: Same shape as scalingPool() (core, max, 30s keep-alive,
        // bounded ArrayBlockingQueue), but pass an explicit
        // ThreadPoolExecutor.AbortPolicy as the RejectedExecutionHandler
        // using the 6-argument constructor, so submitting once the pool AND
        // the queue are both full throws RejectedExecutionException.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static ThreadPoolExecutor callerRunsPool(int coreSize, int maxSize, int queueCapacity) {
        // TODO-03: Same shape as abortingPool(), but pass a
        // ThreadPoolExecutor.CallerRunsPolicy instead, so a task submitted
        // while the pool AND queue are both full runs on the calling thread
        // instead of throwing.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static ThreadPoolExecutor namedThreadPool(int coreSize, int maxSize, int queueCapacity, String poolName) {
        // TODO-04 (optional): Same shape as abortingPool(), but also supply
        // a custom ThreadFactory (via the 7-argument constructor) that names
        // every thread it creates "`poolName`-<n>", with <n> starting at 1
        // and increasing for each thread the factory creates.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
