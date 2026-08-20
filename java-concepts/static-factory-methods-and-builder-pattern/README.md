# Static Factory Methods and Builder Pattern

## Goal

Learn why a class with several optional, interdependent fields needs more than a public constructor: static factory methods that name intent (and can cache/share instances), and a fluent `Builder` whose invariants are validated once — at `build()` — instead of scattered across individual setters.

## Prerequisites

- Basic Java syntax
- Java records

## Task

Implement `ServerConfig`, an immutable server startup configuration with two required fields (`host`, `port`) and several optional ones, two of which have real constraints: `tlsCertificatePath` is only required when `useTls` is enabled, and `maxConnections`/`unlimited` are mutually exclusive.

## Instructions

Complete the following TODOs in `ServerConfig`:

- TODO-00: Implement `Builder.build()` — validate every invariant (required host, valid port range, TLS certificate requirement, the maxConnections/unlimited conflict) and construct the `ServerConfig`.
- TODO-01: Implement the five fluent setter methods on `Builder`. Each one just assigns its field and returns `this` — no validation belongs here.
- TODO-02: Implement the static factory `localhost(int port)`.
- TODO-03: Implement the static factory `defaultConfig()`, caching and returning the same shared instance on every call.

Run the tests until they all pass. Pay close attention to `settersShouldNeverValidateOnlyBuildShould` — it specifically checks that calling a setter with a conflicting value does *not* throw, and that only `build()` catches the conflict.

## Running the Lab

From the project root:

```bash
mvn -pl java-concepts/static-factory-methods-and-builder-pattern test
```

Or from the lab directory:

```bash
cd java-concepts/static-factory-methods-and-builder-pattern
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Implement `withPort(ServerConfig base, int newPort)`, a static factory that returns a copy of an existing config with only the port changed.
