# Spring Boot Testing

## Goal

`OrderService`, `InventoryService`, `InventoryListener`, and `GreetingController` are
already fully implemented — your job is to write two different kinds of Spring Boot
test against the same application: a full-context `@SpringBootTest` and a narrow
`@WebMvcTest` slice, and see for yourself when each one is the right tool.

## Prerequisites

- Spring REST API Testing with MockMvc (`@WebMvcTest`, `MockMvc`)
- Reading `OrderService`, `InventoryService`, `InventoryListener`, `OrderPlacedEvent`,
  and `GreetingController` in `src/main/java` before writing any test

## Task

`OrderService` never calls `InventoryService` directly — it only publishes an
`OrderPlacedEvent`. The stock update happens because `InventoryListener`, a
*separate* bean, reacts to that event. Proving those three beans actually
collaborate correctly requires the real, full application context — that's
`OrderPlacingIntegrationTest`.

`GreetingController`, by contrast, has no collaborators at all. Testing it doesn't
need `InventoryService`, `InventoryListener`, or anything else in the business
layer — that's `GreetingControllerSliceTest`, using the same `@WebMvcTest` slice
from the previous lab.

## Instructions

Complete the following TODOs:

- TODO-00 (in `OrderPlacingIntegrationTest`): place an order and assert the stock
  dropped by the right amount — proving the event → listener → service chain works
  end to end.
- TODO-01: place two orders for the same product and assert the stock reflects both
  reservations cumulatively.
- TODO-03 (in `GreetingControllerSliceTest`): assert `GET /greeting` returns the
  expected body through `MockMvc`.
- TODO-04: prove the slice never loaded `InventoryService` — a bean the controller
  under test doesn't need — via `applicationContext.getBeanNamesForType(...)`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/spring-boot-testing test
```

Or from the lab directory:

```bash
cd testing-concepts/spring-boot-testing
mvn test
```

## Bonus (Optional)

- TODO-02 (optional, in `OrderPlacingIntegrationTest`): assert that placing an order
  for an unknown product throws `IllegalArgumentException`, propagated synchronously
  from `InventoryListener` back through `placeOrder(...)`.
