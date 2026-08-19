import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class CancellablePoller implements Runnable {

    static final long POLL_INTERVAL_MILLIS = 20;

    private final AtomicInteger pollCount = new AtomicInteger();

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(POLL_INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            pollCount.incrementAndGet();
        }
    }

    public int getPollCount() {
        return pollCount.get();
    }

    public static boolean awaitTermination(Thread worker, long timeoutMillis) throws InterruptedException {
        worker.join(timeoutMillis);
        return !worker.isAlive();
    }

    public static boolean interruptAndAwaitTermination(Thread worker, long timeoutMillis) throws InterruptedException {
        worker.interrupt();
        return awaitTermination(worker, timeoutMillis);
    }

    public String takeOrPropagate(BlockingQueue<String> queue) throws InterruptedException {
        return queue.take();
    }

    public String takeQuietly(BlockingQueue<String> queue) {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
