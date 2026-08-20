# Spring Boot Testing — Solution

## Overview

Two test classes exercise the same small application from opposite ends of the
Spring Boot testing spectrum: `OrderPlacingIntegrationTest` loads the full context
to prove several beans collaborate correctly; `GreetingControllerSliceTest` loads
only the web layer because that's all `GreetingController` needs.

## Key Concepts

- **A full context is the only way to test a collaboration that happens *through*
  the container.** `OrderService` never references `InventoryService` — it publishes
  an event and lets Spring dispatch it to whichever listener is registered.
  `placingOrderUpdatesStockThroughTheEventListener` couldn't be written against a
  mocked `InventoryListener`, because the very thing under test is whether Spring
  actually wires the listener to the publisher — mocking it away would remove the
  behavior being verified.
- **State changes persist across calls within the same cached context.**
  `multipleOrdersAccumulateStockChanges` calls `placeOrder` twice and checks the
  stock after each call, relying on `InventoryService` being the same singleton
  instance (and the same mutable `Map`) both times — exactly the sharing a full
  context provides and a fresh-per-test unit test wouldn't.
- **A slice loads only what the controller under test needs — nothing else.**
  `sliceDoesNotLoadBusinessLayerBeans` asserts
  `applicationContext.getBeanNamesForType(InventoryService.class)` is empty inside
  `@WebMvcTest(GreetingController.class)`, even though `InventoryService` sits in the
  very same package and is loaded without issue by `OrderPlacingIntegrationTest`
  above. Same application, two different context contents — that's the slice's job.
- **The choice isn't just about speed.** `GreetingControllerSliceTest` starts faster
  than `OrderPlacingIntegrationTest` because it wires less, but the real reason to
  prefer a slice is that it's testing one layer in isolation — the fewer beans
  loaded, the more precisely a failure points at the actual layer under test.
- **A synchronous `@EventListener` propagates its exception to the publisher.** The
  bonus test shows `InventoryService.reserve(...)` throwing for an unknown product
  surfaces all the way back through `eventPublisher.publishEvent(...)` to
  `orderService.placeOrder(...)` — because `@EventListener` methods run
  synchronously by default, not on a separate thread.

## Summary

`@SpringBootTest` and a slice like `@WebMvcTest` aren't competing options for the
same job — they answer different questions. Reach for the full context when the
behavior under test only exists *because* several beans are wired together by the
container; reach for a slice when the class under test is self-contained and
everything else would just be dead weight in the context.
