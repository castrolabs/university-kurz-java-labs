import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class BoundedWorkQueue<T> {

    private final BlockingQueue<T> queue;

    public BoundedWorkQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void submit(T item) throws InterruptedException {
        // TODO-00: Add `item` to the queue, blocking the caller while the
        // queue is full instead of throwing (BlockingQueue.put()).
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public T fetch() throws InterruptedException {
        // TODO-01: Remove and return the next item, blocking the caller
        // while the queue is empty instead of throwing (BlockingQueue.take()).
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean trySubmitWithin(T item, long timeoutMillis) throws InterruptedException {
        // TODO-02: Try to add `item`, waiting up to timeoutMillis for room to
        // free up (BlockingQueue.offer(item, timeout, unit)). Return whether
        // it was actually added.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public T fetchWithin(long timeoutMillis) throws InterruptedException {
        // TODO-03: Try to remove the next item, waiting up to timeoutMillis
        // for one to become available (BlockingQueue.poll(timeout, unit)).
        // Return null if nothing arrived in time.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void submitAll(List<T> items) throws InterruptedException {
        // TODO-04 (optional): Submit every item in `items`, in order, reusing
        // submit() for each one so the caller blocks exactly like a single
        // submit() would whenever the queue is full.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public int size() {
        return queue.size();
    }
}
