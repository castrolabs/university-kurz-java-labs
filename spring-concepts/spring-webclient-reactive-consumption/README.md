# Spring WebClient: Reactive HTTP Consumption

## Goal

Build a reactive HTTP client with `WebClient` and prove -- with `StepVerifier`,
not `.block()` -- that timeouts, retries, and error recovery are ordinary
Reactor operators layered onto the response, not special client settings.

## Prerequisites

- Basic Project Reactor (`Mono`, `Flux`, `subscribe`)
- Basic `WebClient` usage (`get().uri(...).retrieve()`)
- Familiarity with `StepVerifier`

## Task

`IngredientClient` wraps a `WebClient` around a fake "ingredients" HTTP API
(started in the test with the JDK's own `com.sun.net.httpserver.HttpServer`
-- no external mock-server dependency, no Docker). None of the four methods
are implemented yet. Getting them right means understanding two things the
article calls out explicitly:

1. Building a `Mono`/`Flux` performs **no I/O** -- the request only fires on
   subscription. `StepVerifier.create(...)` subscribes for you.
2. A non-2xx response does **not** throw at the call site -- it terminates
   the publisher with an error signal. Retries, timeouts, and fallbacks are
   handled with Reactor operators (`retryWhen`, `timeout`, `onErrorResume`)
   downstream of `retrieve()`, exactly like any other `Flux`/`Mono` chain.

## Instructions

Complete the following TODOs in `IngredientClient.java`:

- TODO-00: Implement `getIngredient(id)` -- GET a single ingredient and
  decode it with `bodyToMono(Ingredient.class)`.
- TODO-01: Implement `getIngredients()` -- GET the collection and decode it
  with `bodyToFlux(Ingredient.class)`.
- TODO-02: Implement `getIngredientResilient(id)` -- retry a transient
  failure with backoff, then fall back to an `"UNKNOWN"` ingredient once
  retries are exhausted.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-webclient-reactive-consumption test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-webclient-reactive-consumption
mvn test
```

## Bonus (Optional)

- TODO-03 (optional): Implement `getIngredientWithTimeout(id, timeout)` --
  bound a slow response with `.timeout(...)` and fall back the same way
  `getIngredientResilient()` does.
