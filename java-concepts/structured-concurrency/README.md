# Structured Concurrency

## Goal

Fan out concurrent subtasks with `StructuredTaskScope` and prove that a
sibling failure actually cancels the other in-flight subtask - not just
that the code compiles and eventually returns the right answer.

## Prerequisites

- `ExecutorService` / `Future` (see the companion concurrency-utilities
  concept)
- Lambdas and `Callable`
- Comfortable with `try`-with-resources

> **Preview feature.** `StructuredTaskScope` is still preview in Java 25.
> This lab's `pom.xml` already enables `--enable-preview` for both the
> compiler and test runner - you don't need to pass any extra flags
> yourself, just use a JDK 25 toolchain.

## Task

`ProfileFetcher` fans out a `userFetcher` and an `ordersFetcher` - two
independent `Callable`s supplied by the constructor - and combines their
results into a `Profile`. Your job is to implement the fan-out itself using
`StructuredTaskScope`, not the fetchers (those are just test doubles).

## Instructions

Complete the following TODOs in `ProfileFetcher`:

- TODO-00: Implement `loadProfile()` - fork both fetchers in a scope opened
  with the default policy (wait for all, cancel the rest on the first
  failure), join, and combine the results. Wrap a scope failure in
  `ProfileUnavailableException`.
- TODO-01: Implement `loadProfileWithTimeout(Duration)` - same fan-out, but
  the scope is configured with a single deadline for the whole operation
  instead of a timeout on each individual call.

Run the tests until they all pass. One test in particular
(`loadProfileCancelsSlowSiblingOnFailure`) will only pass if the failing
subtask genuinely cancels its sibling - a version that "waits for both,
then throws" will pass every other test but time out on that one.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/structured-concurrency test
```

Or from the lab directory:

```bash
cd java-concepts/structured-concurrency
mvn test
```

## Bonus (Optional)

- TODO-02 (optional): Implement `fetchFastest(Callable<String>, Callable<String>)`
  using `Joiner.anySuccessfulResultOrThrow()` so it returns whichever of the
  two calls succeeds first, with the loser cancelled automatically - the
  hedged-request pattern `CompletableFuture.anyOf()` cannot express on its
  own, since `anyOf()` only ignores the slower branch while it keeps
  running.
