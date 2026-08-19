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
        queue.put(item);
    }

    public T fetch() throws InterruptedException {
        return queue.take();
    }

    public boolean trySubmitWithin(T item, long timeoutMillis) throws InterruptedException {
        return queue.offer(item, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public T fetchWithin(long timeoutMillis) throws InterruptedException {
        return queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    public void submitAll(List<T> items) throws InterruptedException {
        for (T item : items) {
            submit(item);
        }
    }

    public int size() {
        return queue.size();
    }
}
