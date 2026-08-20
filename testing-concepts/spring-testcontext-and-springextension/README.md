# Spring TestContext Framework and SpringExtension

## Goal

`MessageService` and `AppConfig` are already fully implemented — your job is to
write four ordered tests in `MessageServiceContextLifecycleTest` that prove two
things about the Spring TestContext framework: it caches and reuses an
`ApplicationContext` across test methods with identical configuration, and
`@DirtiesContext` is what forces it to build a fresh one.

## Prerequisites

- JUnit 5 fundamentals
- Reading `MessageService` and `AppConfig` in `src/main/java` before writing any test
  — in particular, understand what `AppConfig.creationCount` counts and when it goes up

## Task

This is a plain Spring test — no Spring Boot, no component scanning.
`@ExtendWith(SpringExtension.class)` plus `@ContextConfiguration(classes = AppConfig.class)`
tells JUnit 5 exactly which Java config to load, and `AppConfig.creationCount`
increments every time Spring actually calls the `@Bean messageService()` method —
i.e. every time a *new* context gets built.

The four tests in `MessageServiceContextLifecycleTest` are annotated `@Order(1)`
through `@Order(4)` on purpose: they tell one continuous story, and the class is
annotated `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` so that story runs
in the order it's written, every time.

## Instructions

Complete the following TODOs in `MessageServiceContextLifecycleTest`:

- TODO-00 (test 1): assert `AppConfig.creationCount.get() == 1`, then add a message
  and assert the history has one entry.
- TODO-01 (test 2): assert the count is *still* 1, and that the history already
  contains the message test 1 added — the same cached context, and the same bean
  instance, served both tests.
- TODO-02 (test 3, already annotated `@DirtiesContext`): add another message. No
  count assertion needed — this test's job is just to mutate state and get the
  context marked dirty.
- TODO-03 (test 4): assert the count is now 2 (a fresh context was built because
  test 3 was `@DirtiesContext`), and the history is empty — a brand-new
  `MessageService`, not the one tests 1–3 polluted.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl testing-concepts/spring-testcontext-and-springextension test
```

Or from the lab directory:

```bash
cd testing-concepts/spring-testcontext-and-springextension
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): add a fifth `@Test`, `@Order(5)`, asserting that
  `messageService.getHistory().add("x")` throws `UnsupportedOperationException` —
  `getHistory()` returns an unmodifiable view.
