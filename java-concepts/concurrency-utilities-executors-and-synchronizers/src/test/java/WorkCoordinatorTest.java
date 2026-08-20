import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkCoordinator")
class WorkCoordinatorTest {

    @Test
    @DisplayName("awaitStartupGate should block until every participant has arrived")
    void awaitStartupGateBlocksUntilAllParticipantsArrive() throws InterruptedException {
        WorkCoordinator coordinator = new WorkCoordinator(3, 1);
        AtomicInteger arrivedCount = new AtomicInteger();
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        for (int i = 1; i <= 3; i++) {
            long delay = i * 50L;
            new Thread(() -> {
                try {
                    sleep(delay);
                    arrivedCount.incrementAndGet();
                    coordinator.arriveAtStartupGate();
                } catch (Throwable t) {
                    failures.add(t);
                }
            }).start();
        }

        coordinator.awaitStartupGate();

        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
        assertEquals(3, arrivedCount.get(),
                "the gate should only open once all 3 participants had already arrived");
    }

    @Test
    @DisplayName("awaitStartupGate should return immediately once the gate is already open")
    void awaitStartupGateReturnsImmediatelyOnceAlreadyOpen() throws InterruptedException {
        WorkCoordinator coordinator = new WorkCoordinator(1, 1);
        coordinator.arriveAtStartupGate();

        coordinator.awaitStartupGate();
        coordinator.awaitStartupGate(); // second call must not block - the gate is one-shot, not a fresh wait
    }

    @Test
    @DisplayName("awaitRoundBarrier should be reusable across multiple rounds, releasing all participants together each time")
    void roundBarrierIsReusableAcrossMultipleRounds() throws InterruptedException {
        int participants = 4;
        int rounds = 3;
        WorkCoordinator coordinator = new WorkCoordinator(participants, 1);
        AtomicInteger[] roundArrivals = new AtomicInteger[rounds];
        for (int r = 0; r < rounds; r++) {
            roundArrivals[r] = new AtomicInteger();
        }
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        CountDownLatch allDone = new CountDownLatch(participants);

        for (int p = 0; p < participants; p++) {
            new Thread(() -> {
                try {
                    for (int r = 0; r < rounds; r++) {
                        roundArrivals[r].incrementAndGet();
                        coordinator.awaitRoundBarrier();
                        assertEquals(participants, roundArrivals[r].get(),
                                "round " + r + " barrier released a participant before everyone had arrived");
                    }
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    allDone.countDown();
                }
            }).start();
        }

        allDone.await(5, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
    }

    @Test
    @DisplayName("runWithPermit should never let more concurrent tasks run than there are permits")
    void runWithPermitNeverExceedsAvailablePermits() throws InterruptedException {
        WorkCoordinator coordinator = new WorkCoordinator(1, 2);
        int taskCount = 6;
        AtomicInteger current = new AtomicInteger();
        AtomicInteger maxObserved = new AtomicInteger();
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        CountDownLatch done = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            new Thread(() -> {
                try {
                    coordinator.runWithPermit(() -> {
                        int now = current.incrementAndGet();
                        maxObserved.updateAndGet(max -> Math.max(max, now));
                        sleep(100);
                        current.decrementAndGet();
                    });
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            }).start();
        }

        done.await(3, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
        assertTrue(maxObserved.get() <= 2,
                "no more than 2 tasks should ever run concurrently, but saw " + maxObserved.get());
        assertTrue(maxObserved.get() >= 2,
                "with 6 tasks and 2 permits, at least 2 should have overlapped, but saw " + maxObserved.get());
    }

    @Test
    @DisplayName("runWithPermit should release the permit even when the task throws")
    void runWithPermitReleasesPermitEvenWhenTaskThrows() throws InterruptedException {
        WorkCoordinator coordinator = new WorkCoordinator(1, 1);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> coordinator.runWithPermit(() -> {
            throw new RuntimeException("boom");
        }));
        assertEquals("boom", thrown.getMessage());

        List<Throwable> failures = new CopyOnWriteArrayList<>();
        AtomicBoolean completed = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                coordinator.runWithPermit(() -> completed.set(true));
            } catch (Throwable e) {
                failures.add(e);
            }
        });
        t.start();
        t.join(1000);

        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
        assertTrue(completed.get(),
                "the permit should have been released after the task threw - a leaked permit would block this call forever");
    }

    @Test
    @DisplayName("incrementUnderLock should be mutually exclusive across concurrent callers")
    void incrementUnderLockIsMutuallyExclusive() throws InterruptedException {
        WorkCoordinator coordinator = new WorkCoordinator(1, 1);
        int threadCount = 8;
        int incrementsPerThread = 500;
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        coordinator.incrementUnderLock();
                    }
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            }).start();
        }

        done.await(5, TimeUnit.SECONDS);
        if (!failures.isEmpty()) {
            fail(failures.get(0));
        }
        int finalValue = coordinator.incrementUnderLock();
        assertEquals(threadCount * incrementsPerThread + 1, finalValue,
                "no increment should have been lost to a race");
    }

    @Test
    @DisplayName("reentrantDoubleIncrement should not deadlock when re-acquiring the same lock on the same thread")
    void reentrantDoubleIncrementDoesNotDeadlockAndIncrementsTwice() {
        WorkCoordinator coordinator = new WorkCoordinator(1, 1);

        int result = assertTimeoutPreemptively(Duration.ofSeconds(2), coordinator::reentrantDoubleIncrement);

        assertEquals(2, result, "reentrantDoubleIncrement should leave the counter at 2");
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
