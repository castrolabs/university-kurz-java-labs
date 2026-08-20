package com.kurz.kafkalab;

import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class OrderEventListener {

    private final BlockingQueue<ReceivedOrderEvent> received = new LinkedBlockingQueue<>();

    /**
     * Handles every OrderEvent published to OrderEventPublisher.TOPIC.
     */
    // TODO-01: Annotate this method with
    //   @KafkaListener(topics = OrderEventPublisher.TOPIC, groupId = "order-lab-consumer")
    // Add a `@Header(KafkaHeaders.RECEIVED_PARTITION) int partition` parameter
    // alongside the OrderEvent payload, and offer both into `received` as a
    // new ReceivedOrderEvent(event, partition). KafkaTemplate has no
    // receive() -- a listener is the only way to consume.
    public void handle(OrderEvent event, int partition) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * Blocks up to the given timeout for the next received event, or returns
     * null if none arrives in time.
     */
    public ReceivedOrderEvent poll(long timeout, TimeUnit unit) throws InterruptedException {
        return received.poll(timeout, unit);
    }
}

record ReceivedOrderEvent(OrderEvent event, int partition) {
}
