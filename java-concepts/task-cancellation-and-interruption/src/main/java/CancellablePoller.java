import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CancellablePoller implements Runnable {

    static final long POLL_INTERVAL_MILLIS = 20;

    private final AtomicInteger pollCount = new AtomicInteger();

    @Override
    public void run() {
        // TODO-00: Loop while the current thread has not been interrupted
        // (Thread.currentThread().isInterrupted()). On each iteration, sleep
        // for POLL_INTERVAL_MILLIS (Thread.sleep) to simulate one poll, then
        // increment pollCount. If sleep() throws InterruptedException,
        // restore the interrupt status (Thread.currentThread().interrupt())
        // and stop the loop instead of continuing to poll.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public int getPollCount() {
        return pollCount.get();
    }

    public static boolean awaitTermination(Thread worker, long timeoutMillis) throws InterruptedException {
        // TODO-01: Wait up to timeoutMillis for `worker` to finish
        // (Thread.join(long)). Return whether it actually terminated within
        // that window.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public static boolean interruptAndAwaitTermination(Thread worker, long timeoutMillis) throws InterruptedException {
        // TODO-02: Interrupt `worker` (Thread.interrupt()), then wait up to
        // timeoutMillis for it to terminate, the same way awaitTermination()
        // does. Return whether it actually terminated within that window.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String takeOrPropagate(BlockingQueue<String> queue) throws InterruptedException {
        // TODO-03: Take the next item from `queue` (BlockingQueue.take()),
        // letting InterruptedException propagate unmodified to the caller
        // instead of catching it here.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String takeQuietly(BlockingQueue<String> queue) {
        // TODO-04 (optional): Take the next item from `queue`. If
        // interrupted while waiting, restore the interrupt status and
        // return null instead of throwing.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
