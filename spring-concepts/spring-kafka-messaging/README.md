# Spring Kafka Messaging

## Goal

Send and receive real messages through a real (in-process) Kafka broker with
`KafkaTemplate` and `@KafkaListener`, and prove -- by asserting on which
partition a message landed in -- why the send key you pass is almost never
optional.

## Prerequisites

- Basic Spring dependency injection (`@Service`, `@Component`, constructor
  injection)
- Basic familiarity with Kafka: topics, partitions, producers, consumers

## Task

`OrderEventPublisher` wraps a `KafkaTemplate<String, OrderEvent>` and
`OrderEventListener` is meant to consume from the same topic with
`@KafkaListener`. Neither is wired up yet: `publish()` doesn't send anything,
and `handle()` isn't even registered as a listener.

The tests run against a real embedded Kafka broker (`@EmbeddedKafka` -- no
Docker, no external service) with 3 partitions, and prove two things:

1. A message sent with `publish()` is actually received by the listener --
   not just that `send()` didn't throw.
2. Kafka routes by `hash(key) % partitions`: three events published with the
   *same* order id all land on the *same* partition and are consumed in the
   order they were sent. Get the key wrong (or leave it out) and this test
   fails, because the events could be spread across different partitions.

## Instructions

Complete the following TODOs:

- TODO-00: Implement `OrderEventPublisher.publish()` -- send the event to
  `TOPIC`, keyed by `event.orderId()`.
- TODO-01: Turn `OrderEventListener.handle()` into a real `@KafkaListener` --
  annotate the method, add a `@Header(KafkaHeaders.RECEIVED_PARTITION)`
  parameter, and record both the event and the partition it arrived on.

Run the tests until they all pass. The embedded broker takes a few seconds
to start, so this lab's test suite is noticeably slower than the others --
that's expected.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-kafka-messaging test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-kafka-messaging
mvn test
```
