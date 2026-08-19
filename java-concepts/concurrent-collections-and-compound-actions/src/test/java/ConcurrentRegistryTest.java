import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConcurrentRegistry")
class ConcurrentRegistryTest {

    // Daemon threads so a bug that never releases a thread (e.g. it blocks
    // forever) can't hang the test JVM at shutdown.
    private static ExecutorService newDaemonPool(int threads) {
        return Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    @Test
    @DisplayName("should return a different account for different ids")
    void shouldReturnADifferentAccountForDifferentIds() {
        ConcurrentRegistry registry = new ConcurrentRegistry();

        ConcurrentRegistry.Account a = registry.getOrCreate("alice");
        ConcurrentRegistry.Account b = registry.getOrCreate("bob");

        assertNotEquals(a, b);
        assertEquals(2, registry.size());
    }

    @Test
    @DisplayName("should return the same instance on repeated sequential calls")
    void shouldReturnTheSameInstanceOnRepeatedSequentialCalls() {
        ConcurrentRegistry registry = new ConcurrentRegistry();

        ConcurrentRegistry.Account first = registry.getOrCreate("alice");
        ConcurrentRegistry.Account second = registry.getOrCreate("alice");

        assertSame(first, second);
        assertEquals(1, registry.constructions());
    }

    @Test
    @DisplayName("should create exactly one account per id under concurrent access")
    void shouldCreateExactlyOneAccountPerIdUnderConcurrentAccess() throws InterruptedException {
        ConcurrentRegistry registry = new ConcurrentRegistry();
        int threadCount = 200;
        ExecutorService pool = newDaemonPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        Set<ConcurrentRegistry.Account> seen = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    seen.add(registry.getOrCreate("shared-id"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        boolean finished = done.await(15, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertTrue(finished, "all threads should finish within the timeout");
        assertEquals(1, registry.constructions(),
                "the account must be constructed exactly once, even with " + threadCount + " racing threads");
        assertEquals(1, seen.size(), "every thread must observe the same account instance");
        assertEquals(1, registry.size());
    }

    @Test
    @DisplayName("should register a nickname only the first time")
    void shouldRegisterANicknameOnlyTheFirstTime() {
        ConcurrentRegistry registry = new ConcurrentRegistry();

        boolean first = registry.registerNickname("alice", "ace");
        boolean second = registry.registerNickname("alice", "someone-else");

        assertTrue(first);
        assertFalse(second);
        assertEquals("ace", registry.nicknameOf("alice"));
    }

    @Test
    @DisplayName("should let exactly one thread win the nickname registration race")
    void shouldLetExactlyOneThreadWinTheNicknameRegistrationRace() throws InterruptedException {
        // A single "check, then act" race only lands inside its narrow unsafe
        // window with some probability, not certainty. Repeating the race,
        // with a fresh barrier-synchronized start each time, across many
        // independent rounds makes an unsafe implementation reliably fail
        // instead of depending on one lucky (or unlucky) scheduling outcome.
        ConcurrentRegistry registry = new ConcurrentRegistry();
        int rounds = 10;
        int threadsPerRound = 200;
        ExecutorService pool = newDaemonPool(threadsPerRound);
        int roundsWithMoreThanOneWinner = 0;

        for (int round = 0; round < rounds; round++) {
            String id = "round-" + round;
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(threadsPerRound);
            AtomicInteger winners = new AtomicInteger();

            for (int i = 0; i < threadsPerRound; i++) {
                int threadIndex = i;
                pool.submit(() -> {
                    try {
                        start.await();
                        if (registry.registerNickname(id, "nickname-" + threadIndex)) {
                            winners.incrementAndGet();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            start.countDown();
            boolean finished = done.await(15, TimeUnit.SECONDS);
            assertTrue(finished, "all threads should finish within the timeout");
            if (winners.get() > 1) {
                roundsWithMoreThanOneWinner++;
            }
            assertNotNull(registry.nicknameOf(id));
        }

        pool.shutdownNow();
        assertEquals(0, roundsWithMoreThanOneWinner,
                "no round should have more than one winning registration, even under concurrent access");
    }

    @Test
    @DisplayName("should accumulate every hit exactly once under concurrent access")
    void shouldAccumulateEveryHitExactlyOnceUnderConcurrentAccess() throws InterruptedException {
        ConcurrentRegistry registry = new ConcurrentRegistry();
        int threadCount = 50;
        int hitsPerThread = 200;
        ExecutorService pool = newDaemonPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < hitsPerThread; j++) {
                        registry.recordHit("shared-id");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        boolean finished = done.await(15, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertTrue(finished, "all threads should finish within the timeout");
        assertEquals(threadCount * hitsPerThread, registry.hitsFor("shared-id"),
                "no hit should be lost, no matter how many threads increment concurrently");
    }

    @Test
    @DisplayName("should remove the nickname when the current value matches (optional)")
    void shouldRemoveTheNicknameWhenTheCurrentValueMatchesOptional() {
        ConcurrentRegistry registry = new ConcurrentRegistry();
        registry.registerNickname("alice", "ace");

        boolean removed = registry.clearNicknameIfMatches("alice", "ace");

        assertTrue(removed);
        assertNull(registry.nicknameOf("alice"));
    }

    @Test
    @DisplayName("should not remove the nickname when the current value doesn't match (optional)")
    void shouldNotRemoveTheNicknameWhenTheCurrentValueDoesNotMatchOptional() {
        ConcurrentRegistry registry = new ConcurrentRegistry();
        registry.registerNickname("alice", "ace");

        boolean removed = registry.clearNicknameIfMatches("alice", "someone-else");

        assertFalse(removed);
        assertEquals("ace", registry.nicknameOf("alice"));
    }
}
