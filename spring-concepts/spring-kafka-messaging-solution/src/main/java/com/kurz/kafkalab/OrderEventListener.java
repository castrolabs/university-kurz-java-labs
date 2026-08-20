package com.kurz.kafkalab;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class OrderEventListener {

    private final BlockingQueue<ReceivedOrderEvent> received = new LinkedBlockingQueue<>();

    @KafkaListener(topics = OrderEventPublisher.TOPIC, groupId = "order-lab-consumer")
    public void handle(OrderEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        received.offer(new ReceivedOrderEvent(event, partition));
    }

    public ReceivedOrderEvent poll(long timeout, TimeUnit unit) throws InterruptedException {
        return received.poll(timeout, unit);
    }
}

record ReceivedOrderEvent(OrderEvent event, int partition) {
}
