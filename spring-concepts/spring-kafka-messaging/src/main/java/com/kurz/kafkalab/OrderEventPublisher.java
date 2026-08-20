package com.kurz.kafkalab;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventPublisher {

    public static final String TOPIC = "orders.topic";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes an OrderEvent to TOPIC, keyed by its order id.
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publish(OrderEvent event) {
        // TODO-00: Send `event` to TOPIC using kafkaTemplate.send(topic, key, value),
        // keyed by event.orderId(). Kafka routes by hash(key) % partitions, so
        // using the order id as the key guarantees every event for the same
        // order lands on -- and is consumed from -- the same partition, in
        // the order it was sent.

        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
