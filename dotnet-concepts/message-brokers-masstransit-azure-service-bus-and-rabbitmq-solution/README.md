# Message Brokers: MassTransit, Azure Service Bus and RabbitMQ (Solution)

Official solution for the `message-brokers-masstransit-azure-service-bus-and-rabbitmq` lab. MassTransit is pinned to
8.5.12 (Apache-2.0). v9 is commercial and needs a license key.

## What changed from the starter

- **`OrderApi.SubmitAsync`** uses `IPublishEndpoint.Publish` with a callback that sets `ctx.MessageId`. A fixed id
  stands for a client retry or a broker redelivery.
- **`OrderApi.RequestShipmentAsync`** gets the send endpoint for `queue:shipping` and sends the command. The
  `ShippingConsumerDefinition` pins the queue name, so the address does not depend on the naming convention.
- **`BillingConsumer`** checks the inbox first, throws for a negative total, records the charge, publishes, and marks
  the message processed last. Marking last matters: a failure before the mark must leave the message eligible.
- **`BillingConsumerDefinition`** adds `UseMessageRetry(Interval(3, 10 ms))` and then `UseInMemoryOutbox`.
- **`BillingFaultConsumer`** reads `context.Message.Message.OrderId` from `Fault<OrderSubmitted>`.

## Bonus notes

- Without `UseInMemoryOutbox`, the outbox test fails: the consumer publishes before it throws, the retry publishes again,
  and `PaymentCaptured` goes out twice.
- Without the `IsProcessed` check, the duplicate test fails: the same `MessageId` is consumed twice and the effect runs twice.
- The harness lists (`Published`, `Consumed`) collapse messages with the same `MessageId`, so the test counts duplicates in
  the ledger instead. A poison `OrderSubmitted` is delivered 4 times (1 try + 3 retries) before the fault is published.
- The in-memory inbox is not atomic with the effect. With a real database, the inbox row and the effect share a
  transaction, as in the transactional outbox concept.
