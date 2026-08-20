# Spring Kafka Messaging - Solution

## Overview

This is the official solution for the Spring Kafka Messaging lab. It
implements a `KafkaTemplate`-based publisher and an `@KafkaListener`-based
consumer, and demonstrates why the key passed to `send()` is the one
"optional" parameter that almost never actually is.

## Key Concepts

### Sending with `KafkaTemplate`

```java
public CompletableFuture<SendResult<String, OrderEvent>> publish(OrderEvent event) {
    return kafkaTemplate.send(TOPIC, event.orderId(), event);
}
```

`send(topic, key, value)` is asynchronous -- it returns immediately with a
`CompletableFuture`. The tests call `.get(10, TimeUnit.SECONDS)` on it, which
is legitimate at a test boundary; in a real service, a fire-and-forget
`send()` that ignores the returned future silently swallows serialization
errors or an unreachable broker.

### Receiving with `@KafkaListener`

```java
@KafkaListener(topics = OrderEventPublisher.TOPIC, groupId = "order-lab-consumer")
public void handle(OrderEvent event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
    received.offer(new ReceivedOrderEvent(event, partition));
}
```

`KafkaTemplate` has no `receive()` -- unlike `JmsTemplate`, there is no
pull-based API, because a Kafka consumer is a long-lived, group-coordinated,
offset-committing object. `@KafkaListener` is the only way to consume.
`@Header(KafkaHeaders.RECEIVED_PARTITION)` pulls the partition a record was
consumed from directly into the method signature, alongside the deserialized
payload.

### Proving the key routes to a partition, not just "doesn't throw"

`shouldRouteSameKeyToSamePartitionInOrder()` publishes three events for the
same `orderId` against a 3-partition topic, then asserts all three arrive on
the *same* partition, in the order they were sent:

```java
assertEquals(first.partition(), second.partition());
assertEquals(second.partition(), third.partition());
```

Kafka routes by `hash(key) % partitions`. Passing `event.orderId()` as the
key guarantees every event for one order lands on -- and is consumed from --
the same partition, which is a strict FIFO sequence. Publishing with no key
(or a different key per call) spreads events round-robin across partitions,
and with them, destroys any per-order ordering guarantee -- a bug that only
shows up under concurrency, exactly as the article warns.

## Trade-offs

- Ordering is per-partition, not per-topic: a single-queue JMS broker gives
  FIFO over everything, but Kafka only guarantees order within one partition.
  Getting the guarantee you actually want is a matter of picking the right
  key, not a Kafka setting.
- Parallelism is capped by partition count: within one consumer group, each
  partition is owned by exactly one consumer. This lab's 3-partition topic
  caps useful concurrency at 3 listener instances -- a 4th would sit idle.
- `send()` being asynchronous means a discarded return value reports success
  it can't actually know about; the `CompletableFuture` (or a
  `.whenComplete()` callback in production code) is the only place a failure
  surfaces.

## Summary

- `KafkaTemplate.send(topic, key, value)` is the only way to publish;
  `@KafkaListener` is the only way to consume -- there is no
  `KafkaTemplate.receive()`.
- The send key determines the partition (`hash(key) % partitions`), and the
  partition is what guarantees order -- get the key wrong and ordering
  silently breaks under load, not on the happy path.
- `@EmbeddedKafka` starts a real, in-process broker for integration tests --
  no Docker, no external service -- at the cost of a slower test startup than
  any other messaging technology's Spring integration.
