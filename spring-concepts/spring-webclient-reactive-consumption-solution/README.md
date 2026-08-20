# Spring WebClient: Reactive HTTP Consumption - Solution

## Overview

This is the official solution for the Spring WebClient Reactive Consumption
lab. It implements a fully reactive `IngredientClient` and demonstrates that
retries, timeouts, and error fallback are ordinary Reactor operators chained
onto the response `Mono`/`Flux` -- not features of `WebClient` itself.

## Key Concepts

### `retrieve()` builds a publisher, it does not perform I/O

```java
public Mono<Ingredient> getIngredient(String id) {
    return webClient.get()
            .uri("/ingredients/{id}", id)
            .retrieve()
            .bodyToMono(Ingredient.class);
}
```

Calling `getIngredient("FLTO")` sends nothing over the network. The GET only
fires once something subscribes to the returned `Mono` -- in the tests,
`StepVerifier.create(...)` is that subscriber. `getIngredients()` is the same
shape with `bodyToFlux(Ingredient.class)` in place of `bodyToMono`.

### Errors are signals, not exceptions

A 404 from `/ingredients/MISSING` never throws at the `getIngredient()` call
site. `retrieve()` turns any non-2xx response into a
`WebClientResponseException` that terminates the publisher with an error
signal instead:

```java
StepVerifier.create(client.getIngredient("MISSING"))
        .expectErrorMatches(e -> e instanceof WebClientResponseException wcre
                && wcre.getStatusCode().value() == 404)
        .verify();
```

### Resilience is composed, not configured

```java
public Mono<Ingredient> getIngredientResilient(String id) {
    return getIngredient(id)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(50)))
            .onErrorResume(e -> Mono.just(unknown(id)));
}
```

`retryWhen(Retry.backoff(3, ...))` re-subscribes to the upstream `Mono` up to
three more times with exponential backoff whenever it errors -- exactly what
lets `getIngredientResilient("FLAKY")` succeed against a fake endpoint that
fails its first two requests and only succeeds on the third.
`onErrorResume()` is the safety net once every attempt is exhausted:
`getIngredientResilient("BROKEN")` (an endpoint that always returns 503)
still completes successfully, with a fallback `Ingredient` instead of an
error signal.

### The same idea, with `timeout()` (`TODO-03`, optional)

```java
public Mono<Ingredient> getIngredientWithTimeout(String id, Duration timeout) {
    return getIngredient(id)
            .timeout(timeout)
            .onErrorResume(e -> Mono.just(unknown(id)));
}
```

`.timeout(Duration.ofMillis(100))` against an endpoint that sleeps 400ms
before responding raises a `TimeoutException` well before the slow response
ever arrives; `onErrorResume()` catches it the same way it catches an
exhausted retry. Nothing here is specific to HTTP or `WebClient` -- `timeout`,
`retryWhen`, and `onErrorResume` are generic Reactor operators that apply to
any `Mono`/`Flux`.

## Trade-offs

- `.retryWhen()` and `.timeout()` are composable precisely because nothing
  fires until subscription -- but that same property means a `Mono` built and
  never subscribed (a bug, not a feature) silently sends no request at all,
  with no exception to notice.
- Retrying blindly on every error is rarely correct: retrying a 503 (likely
  transient) makes sense, retrying a 404 (never transient) just adds latency
  before the same failure. A production client typically narrows
  `retryWhen`'s trigger to specific exception types or status codes rather
  than retrying unconditionally, as this lab does for simplicity.
- `onErrorResume()` swallows the original error in exchange for a fallback
  value -- useful for a UI that would rather show "unknown" than a broken
  page, wrong for a caller that needs to distinguish "ingredient not found"
  from "service down" and react differently to each.

## Summary

- Building a `Mono`/`Flux` with `WebClient` performs no I/O; the request
  fires on subscription.
- A non-2xx response terminates the publisher with a
  `WebClientResponseException` error signal, not a thrown exception at the
  call site.
- `retryWhen`, `timeout`, and `onErrorResume` are ordinary Reactor operators
  layered onto the response -- the same vocabulary as any other reactive
  pipeline, with no separate client-level API to learn.
