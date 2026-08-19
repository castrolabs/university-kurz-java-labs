import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BoundedWorkQueue")
class BoundedWorkQueueTest {

    @Test
    @DisplayName("should block the producer instead of throwing when the queue is full")
    void shouldBlockProducerInsteadOfThrowingWhenQueueIsFull() throws InterruptedException {
        BoundedWorkQueue<Integer> queue = new BoundedWorkQueue<>(2);
        List<Integer> produced = List.of(1, 2, 3, 4, 5);
        AtomicReference<Exception> producerFailure = new AtomicReference<>();
        CountDownLatch producerDone = new CountDownLatch(1);

        Thread producer = new Thread(() -> {
            try {
                for (int item : produced) {
                    queue.submit(item);
                }
            } catch (Exception e) {
                producerFailure.set(e);
            } finally {
                producerDone.countDown();
            }
        });
        producer.start();

        // Give the producer time to fill the queue and start waiting for room.
        Thread.sleep(150);
        assertTrue(producer.isAlive(), "producer should still be blocked waiting for room in the queue");

        List<Integer> consumed = new ArrayList<>();
        for (int i = 0; i < produced.size(); i++) {
            consumed.add(queue.fetch());
        }

        assertTrue(producerDone.await(2, TimeUnit.SECONDS), "producer should finish once the consumer drains the queue");
        assertNull(producerFailure.get(), "submit() must block, not throw, when the queue is full");
        assertEquals(produced, consumed);
    }

    @Test
    @DisplayName("should block the consumer instead of throwing when the queue is empty")
    void shouldBlockConsumerInsteadOfThrowingWhenQueueIsEmpty() throws InterruptedException {
        BoundedWorkQueue<String> queue = new BoundedWorkQueue<>(2);
        AtomicReference<String> received = new AtomicReference<>();
        AtomicReference<Exception> consumerFailure = new AtomicReference<>();
        CountDownLatch consumerDone = new CountDownLatch(1);

        Thread consumer = new Thread(() -> {
            try {
                received.set(queue.fetch());
            } catch (Exception e) {
                consumerFailure.set(e);
            } finally {
                consumerDone.countDown();
            }
        });
        consumer.start();

        Thread.sleep(150);
        assertTrue(consumer.isAlive(), "consumer should still be blocked waiting for an item");

        queue.submit("late-item");

        assertTrue(consumerDone.await(2, TimeUnit.SECONDS), "consumer should finish once an item is submitted");
        assertNull(consumerFailure.get(), "fetch() must block, not throw, when the queue is empty");
        assertEquals("late-item", received.get());
    }

    @Test
    @DisplayName("trySubmitWithin should succeed immediately when there is room")
    void trySubmitWithinShouldSucceedWhenThereIsRoom() throws InterruptedException {
        BoundedWorkQueue<String> queue = new BoundedWorkQueue<>(2);

        assertTrue(queue.trySubmitWithin("a", 200));
        assertEquals(1, queue.size());
    }

    @Test
    @DisplayName("trySubmitWithin should give up and return false once the timeout elapses")
    void trySubmitWithinShouldReturnFalseWhenTimeoutElapses() throws InterruptedException {
        BoundedWorkQueue<String> queue = new BoundedWorkQueue<>(1);
        queue.submit("filler");

        assertFalse(queue.trySubmitWithin("overflow", 100));
        assertEquals(1, queue.size());
    }

    @Test
    @DisplayName("fetchWithin should return an item immediately when one is available")
    void fetchWithinShouldReturnItemWhenAvailable() throws InterruptedException {
        BoundedWorkQueue<String> queue = new BoundedWorkQueue<>(2);
        queue.submit("a");

        assertEquals("a", queue.fetchWithin(200));
    }

    @Test
    @DisplayName("fetchWithin should give up and return null once the timeout elapses")
    void fetchWithinShouldReturnNullWhenTimeoutElapses() throws InterruptedException {
        BoundedWorkQueue<String> queue = new BoundedWorkQueue<>(2);

        assertNull(queue.fetchWithin(100));
    }

    @Test
    @DisplayName("submitAll should deliver every item in order, blocking as needed")
    void submitAllShouldDeliverEveryItemInOrder() throws InterruptedException {
        BoundedWorkQueue<Integer> queue = new BoundedWorkQueue<>(2);
        List<Integer> produced = List.of(1, 2, 3, 4);
        AtomicReference<Exception> producerFailure = new AtomicReference<>();
        CountDownLatch producerDone = new CountDownLatch(1);

        Thread producer = new Thread(() -> {
            try {
                queue.submitAll(produced);
            } catch (Exception e) {
                producerFailure.set(e);
            } finally {
                producerDone.countDown();
            }
        });
        producer.start();

        List<Integer> consumed = new ArrayList<>();
        for (int i = 0; i < produced.size(); i++) {
            consumed.add(queue.fetch());
        }

        assertTrue(producerDone.await(2, TimeUnit.SECONDS));
        assertNull(producerFailure.get());
        assertEquals(produced, consumed);
    }
}
