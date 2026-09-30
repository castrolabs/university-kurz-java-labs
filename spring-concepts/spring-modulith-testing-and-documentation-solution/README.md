# Spring Modulith: Testing and Documentation - Solution

## Overview

This is the official solution for the Testing and Documentation lab. Each module is tested in its
own slice of the application context, cross-module collaboration is tested through events
(`AssertablePublishedEvents`, `Scenario`), and `Documenter` turns the module model into diagrams
and canvases.

## Key Concepts

### @ApplicationModuleTest

```java
@ApplicationModuleTest           // BootstrapMode.STANDALONE by default
class PublishingModuleTests { ... }
```

The test class must live in the module's package. Spring Modulith derives the module from it and
limits component scanning to that module. `DIRECT_DEPENDENCIES` also starts the modules it
depends on, `ALL_DEPENDENCIES` the whole dependency tree.

### A missing bean from another module is a design signal

```
No qualifying bean of type 'com.kurz.moduletests.publishing.ContentCatalog' available
```

`notification` needs a bean from `publishing`. Either mock it (`@MockitoBean ContentCatalog`) and
keep the test to one module, or accept a bigger context with `DIRECT_DEPENDENCIES`. The more of
these a module has, the less independent it really is.

### Asserting events

```java
assertThat(events).contains(ContentPublished.class)
        .matching(ContentPublished::title, "Modular monoliths");

scenario.publish(new ContentPublished(42, "Modular monoliths"))
        .andWaitForEventOfType(NotificationSent.class)
        .matching(sent -> sent.title().equals("Modular monoliths"))
        .toArriveAndVerify(sent -> assertThat(sent.recipients()).isEqualTo(2));
```

`AssertablePublishedEvents` records every event published during the test. `Scenario` publishes
the stimulus in a transaction (so AFTER_COMMIT listeners fire) and waits, via Awaitility, for the
asynchronous outcome.

### Documenter

`new Documenter(modules).writeDocumentation()` writes, under `target/spring-modulith-docs`,
`components.puml` (C4 overview), `module-<name>.puml` and `module-<name>.adoc` (the module canvas:
components, bean references to other modules, events published and listened to) and
`all-docs.adoc`.

## Summary

Module tests are faster and more honest than full `@SpringBootTest` runs: whatever a module needs
from outside shows up as a missing bean you must mock or opt into. The same model that powers the
tests also produces documentation that cannot drift from the code.
