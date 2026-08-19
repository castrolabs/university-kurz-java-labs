import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExecutorFactory")
class ExecutorFactoryTest {

    private ThreadPoolExecutor executor;

    @AfterEach
    void shutdown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("scalingPool should grow past core size once its bounded queue fills up under a burst")
    void scalingPoolShouldGrowPastCoreSizeUnderBurst() throws InterruptedException {
        executor = ExecutorFactory.scalingPool(2, 6, 1);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        CountDownLatch done = new CountDownLatch(6);

        for (int i = 0; i < 6; i++) {
            executor.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                sleep(150);
                done.countDown();
            });
        }

        assertTrue(done.await(3, TimeUnit.SECONDS), "all 6 tasks should have completed");
        assertTrue(threadNames.size() > 2,
                "pool should have grown past corePoolSize(2) to absorb the burst, but only used " + threadNames.size() + " thread(s)");
        assertTrue(threadNames.size() <= 6, "pool should never exceed maximumPoolSize(6)");
    }

    @Test
    @DisplayName("fixedCorePool should never use more threads than its core size, no matter the burst size")
    void fixedCorePoolShouldNeverExceedCoreSize() throws InterruptedException {
        executor = ExecutorFactory.fixedCorePool(3);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        CountDownLatch done = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                sleep(50);
                done.countDown();
            });
        }

        assertTrue(done.await(3, TimeUnit.SECONDS), "all 10 tasks should have completed");
        assertEquals(3, threadNames.size(),
                "an unbounded queue should let a fixed-size pool absorb every task with exactly corePoolSize(3) threads, but used " + threadNames.size());
    }

    @Test
    @DisplayName("abortingPool should reject tasks once both the pool and queue are full")
    void abortingPoolShouldRejectWhenSaturated() {
        executor = ExecutorFactory.abortingPool(1, 1, 1);

        assertThrows(RejectedExecutionException.class, () -> {
            for (int i = 0; i < 5; i++) {
                executor.execute(() -> sleep(200));
            }
        });
    }

    @Test
    @DisplayName("callerRunsPool should run overflow tasks on the calling thread instead of throwing")
    void callerRunsPoolShouldRunOverflowOnCallingThread() throws InterruptedException {
        executor = ExecutorFactory.callerRunsPool(1, 1, 1);
        String callingThreadName = Thread.currentThread().getName();
        List<String> threadNames = new CopyOnWriteArrayList<>();

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 4; i++) {
                executor.execute(() -> {
                    threadNames.add(Thread.currentThread().getName());
                    sleep(120);
                });
            }
        });

        executor.shutdown();
        assertTrue(executor.awaitTermination(3, TimeUnit.SECONDS));

        assertEquals(4, threadNames.size(), "every submitted task should have run exactly once");
        assertTrue(threadNames.contains(callingThreadName),
                "at least one overflow task should have run on the caller's own thread under CallerRunsPolicy, but saw " + threadNames);
    }

    @Test
    @DisplayName("namedThreadPool should name every thread it creates using the given pool name")
    void namedThreadPoolShouldNameThreadsUsingPoolName() throws InterruptedException {
        executor = ExecutorFactory.namedThreadPool(2, 2, 4, "worker");
        Set<String> threadNames = ConcurrentHashMap.newKeySet();
        CountDownLatch done = new CountDownLatch(3);

        for (int i = 0; i < 3; i++) {
            executor.execute(() -> {
                threadNames.add(Thread.currentThread().getName());
                sleep(30);
                done.countDown();
            });
        }

        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertTrue(threadNames.stream().allMatch(name -> name.startsWith("worker-")),
                "every pool thread name should start with \"worker-\", but got " + threadNames);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
