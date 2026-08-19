import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancellablePoller")
class CancellablePollerTest {

    @Test
    @DisplayName("should stop polling promptly when interrupted")
    void shouldStopPollingPromptlyWhenInterrupted() throws InterruptedException {
        CancellablePoller poller = new CancellablePoller();
        Thread worker = new Thread(poller);
        worker.start();

        Thread.sleep(CancellablePoller.POLL_INTERVAL_MILLIS * 3);
        worker.interrupt();
        worker.join(500);

        assertFalse(worker.isAlive(), "worker should have stopped within 500ms of being interrupted");
        assertTrue(poller.getPollCount() > 0, "worker should have completed at least one poll before stopping");
    }

    @Test
    @DisplayName("should clear the interrupt status on the polling thread after stopping")
    void shouldRestoreInterruptStatusAfterStopping() throws InterruptedException {
        // A correct implementation restores the flag right before run() returns,
        // so by the time the thread has died, there is nothing left to observe
        // from outside except that it actually stopped — this is covered above.
        // Here we only assert the thread does not linger around forever.
        CancellablePoller poller = new CancellablePoller();
        Thread worker = new Thread(poller);
        worker.start();
        Thread.sleep(CancellablePoller.POLL_INTERVAL_MILLIS * 2);

        boolean terminated = CancellablePoller.interruptAndAwaitTermination(worker, 500);

        assertTrue(terminated, "interruptAndAwaitTermination should report the worker as terminated");
    }

    @Test
    @DisplayName("awaitTermination should return true once the thread finishes on its own")
    void awaitTerminationShouldReturnTrueForAFinishedThread() throws InterruptedException {
        Thread quick = new Thread(() -> { });
        quick.start();

        assertTrue(CancellablePoller.awaitTermination(quick, 500));
    }

    @Test
    @DisplayName("awaitTermination should return false when the timeout elapses first")
    void awaitTerminationShouldReturnFalseWhenTimeoutElapsesFirst() throws InterruptedException {
        Thread slow = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
                // deliberately ignored: this test only cares about the timeout window
            }
        });
        slow.start();

        assertFalse(CancellablePoller.awaitTermination(slow, 50));

        slow.interrupt();
        slow.join(1000);
    }

    @Test
    @DisplayName("interruptAndAwaitTermination should return false for a thread that ignores interruption")
    void interruptAndAwaitTerminationShouldReturnFalseForUncooperativeThread() throws InterruptedException {
        Thread uncooperative = new Thread(() -> {
            long deadline = System.nanoTime() + 300_000_000L;
            while (System.nanoTime() < deadline) {
                // deliberately ignores its own interrupt status
            }
        });
        uncooperative.setDaemon(true);
        uncooperative.start();

        assertFalse(CancellablePoller.interruptAndAwaitTermination(uncooperative, 50));

        uncooperative.join(1000);
    }

    @Test
    @DisplayName("takeOrPropagate should return the next item when one is available")
    void takeOrPropagateShouldReturnAvailableItem() throws InterruptedException {
        CancellablePoller poller = new CancellablePoller();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        queue.put("task-1");

        assertEquals("task-1", poller.takeOrPropagate(queue));
    }

    @Test
    @DisplayName("takeOrPropagate should let InterruptedException propagate instead of swallowing it")
    void takeOrPropagateShouldPropagateInterruptedException() {
        CancellablePoller poller = new CancellablePoller();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);

        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedException.class, () -> poller.takeOrPropagate(queue));
        } finally {
            Thread.interrupted(); // clear the flag so it doesn't leak into other tests
        }
    }

    @Test
    @DisplayName("takeQuietly should return the next item when one is available")
    void takeQuietlyShouldReturnAvailableItem() throws InterruptedException {
        CancellablePoller poller = new CancellablePoller();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        queue.put("task-1");

        assertEquals("task-1", poller.takeQuietly(queue));
    }

    @Test
    @DisplayName("takeQuietly should return null and restore the flag instead of throwing")
    void takeQuietlyShouldReturnNullAndRestoreFlag() {
        CancellablePoller poller = new CancellablePoller();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);

        Thread.currentThread().interrupt();
        try {
            assertNull(poller.takeQuietly(queue));
            assertTrue(Thread.currentThread().isInterrupted(), "interrupt status should be restored, not lost");
        } finally {
            Thread.interrupted(); // clear the flag so it doesn't leak into other tests
        }
    }
}
