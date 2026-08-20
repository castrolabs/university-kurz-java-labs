# Scoped Values

## Goal

Carry per-request context through a call chain with `ScopedValue` instead
of `ThreadLocal` - and confirm the two invariants that make it safe: there
is no `set()` to corrupt a caller's view, and reading outside a binding
fails loudly instead of returning something stale.

## Prerequisites

- Lambdas (`Runnable`, and a `Callable`-shaped functional interface)
- Basic familiarity with `ThreadLocal` helps for contrast, but isn't
  required

> `ScopedValue` is a final, stable API as of Java 25 (JEP 506) - no
> `--enable-preview` needed. This lab's `pom.xml` only raises the compiler
> source/target to 25.

## Task

`RequestContext` wraps two `ScopedValue` keys (`CURRENT_USER`,
`CURRENT_LOCALE`) behind static helper methods. There is no `set()`
anywhere in the class - every TODO is either a *read* through one of
`get()`/`isBound()`/`orElse()`, or a *bind* through `where(...).run(...)`/
`where(...).call(...)`.

## Instructions

Complete the following TODOs in `RequestContext`:

- TODO-00: Return the current user, letting an unbound read throw.
- TODO-01: Return whether a user is currently bound.
- TODO-02: Return the current user, or a fallback if unbound.
- TODO-03: Bind a user for the duration of a `Callable`-style action and
  return its result.
- TODO-04: Bind a user for the duration of a `Runnable` action.

Run the tests until they all pass. Two of them are worth reading closely
even after they're green: `nestedRunAsShadowsThenRestores` (a rebinding
only ever shadows its own extent - it can't leak upward) and
`plainChildThreadDoesNotInheritBinding` (a thread you start yourself is
*outside* the extent - only a `StructuredTaskScope` subtask automatically
sees the parent's binding).

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/scoped-values test
```

Or from the lab directory:

```bash
cd java-concepts/scoped-values
mvn test
```

## Bonus (Optional)

- TODO-05 (optional): Implement `runAs(String user, String locale, Runnable action)`
  so it binds both `CURRENT_USER` and `CURRENT_LOCALE` for the duration of
  `action`, by chaining a second `.where(...)` onto the `Carrier` returned
  by the first.
