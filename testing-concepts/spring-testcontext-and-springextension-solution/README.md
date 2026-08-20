# Spring TestContext Framework and SpringExtension — Solution

## Overview

`MessageServiceContextLifecycleTest` runs four ordered tests against a plain Spring
context (`@ExtendWith(SpringExtension.class)` + `@ContextConfiguration(classes = AppConfig.class)`,
no Spring Boot). Together they show the TestContext framework caching a context
across test methods, and `@DirtiesContext` forcing it to rebuild.

## Key Concepts

- **The context is built once and reused, not once per test.** `AppConfig.creationCount`
  only increments inside the `@Bean messageService()` factory method — a proxy for
  "how many times has Spring actually built a context for this configuration."
  `firstTestCreatesContextOnce` sees it at 1; `secondTestReusesSameCachedContext`,
  running immediately after with the exact same `@ContextConfiguration`, still sees
  1 — no second context was built for the second test method.
- **A cached context means a cached bean, mutable state included.**
  `secondTestReusesSameCachedContext` finds `"first"` already in
  `messageService.getHistory()` — the message the *previous* test method added.
  `@Autowired` injected the same `MessageService` instance both times, because
  the container instance is scoped to the context, not to the test method.
- **`@DirtiesContext` is the escape hatch for that leakage.** `thirdTestMutatesAndDirtiesTheContext`
  mutates state again and is annotated `@DirtiesContext` (default `MethodMode.AFTER_METHOD`),
  which tells the framework to discard the cached context once the method finishes.
- **The next test that needs the same configuration gets a brand-new context.**
  `fourthTestGetsAFreshContext` sees `creationCount.get() == 2` — a *new*
  `MessageService` was built — and `getHistory()` is empty, proving it's not the
  instance the first three tests shared.
- **`@TestMethodOrder` + `@Order` make the story deterministic.** Without it, JUnit 5
  doesn't guarantee method execution order, and this whole before/after narrative
  (leak, then a forced rebuild) would depend on an order the framework never
  promised to keep.

## Summary

Context caching is what makes a large Spring test suite tolerable — rebuilding a
full `ApplicationContext` per test method would be far too slow. But caching means
sharing, and sharing a mutable bean across tests is exactly the kind of leakage
that produces order-dependent, hard-to-reproduce failures. `@DirtiesContext` is the
tool for the one test that legitimately needs to break that sharing on purpose.
