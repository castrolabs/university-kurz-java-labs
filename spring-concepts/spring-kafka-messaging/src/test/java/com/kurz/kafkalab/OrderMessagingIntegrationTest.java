package com.kurz.kafkalab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = KafkaLabApplication.class)
@EmbeddedKafka(partitions = 3, topics = OrderEventPublisher.TOPIC)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@DisplayName("Order messaging (KafkaTemplate + @KafkaListener)")
class OrderMessagingIntegrationTest {

    @Autowired
    private OrderEventPublisher publisher;

    @Autowired
    private OrderEventListener listener;

    @Test
    @DisplayName("publish() sends an OrderEvent that the @KafkaListener consumes")
    void shouldPublishAndConsumeOrderEvent() throws Exception {
        OrderEvent event = new OrderEvent("order-1", "CREATED");

        publisher.publish(event).get(10, TimeUnit.SECONDS);

        ReceivedOrderEvent received = listener.poll(10, TimeUnit.SECONDS);

        assertNotNull(received, "expected the listener to receive the event -- did you add @KafkaListener?");
        assertEquals(event, received.event());
    }

    @Test
    @DisplayName("events published with the same key are consumed from the same partition, in order")
    void shouldRouteSameKeyToSamePartitionInOrder() throws Exception {
        publisher.publish(new OrderEvent("order-2", "CREATED")).get(10, TimeUnit.SECONDS);
        publisher.publish(new OrderEvent("order-2", "PAID")).get(10, TimeUnit.SECONDS);
        publisher.publish(new OrderEvent("order-2", "SHIPPED")).get(10, TimeUnit.SECONDS);

        ReceivedOrderEvent first = listener.poll(10, TimeUnit.SECONDS);
        ReceivedOrderEvent second = listener.poll(10, TimeUnit.SECONDS);
        ReceivedOrderEvent third = listener.poll(10, TimeUnit.SECONDS);

        assertNotNull(first, "expected the first event to arrive -- did you add @KafkaListener?");
        assertNotNull(second);
        assertNotNull(third);

        assertEquals("CREATED", first.event().status());
        assertEquals("PAID", second.event().status());
        assertEquals("SHIPPED", third.event().status());

        assertEquals(first.partition(), second.partition(),
                "same key should route to the same partition (hash(key) % partitions)");
        assertEquals(second.partition(), third.partition(),
                "same key should route to the same partition (hash(key) % partitions)");
    }
}
