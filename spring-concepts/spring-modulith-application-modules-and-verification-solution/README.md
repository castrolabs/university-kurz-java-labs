# Spring Modulith: Application Modules and Verification - Solution

## Overview

This is the official solution for the Application Modules and Verification lab. The two feature
packages became two verified application modules: internals live in a subpackage, `notification`
talks to `publishing` only through its API (`ContentCatalog`) and its events (`ContentPublished`),
and `verify()` passes.

## Key Concepts

### A module is a package

Every direct subpackage of the `@SpringBootApplication` package is an application module. Public
types in the module's base package form its API; anything in a subpackage is internal, even when
it is `public`.

### What verify() reports

Before the refactoring, `verify()` fails with:

```
- Cycle detected: Slice notification ->
                Slice publishing ->
- Module 'notification' depends on non-exposed type
  com.kurz.contentmod.publishing.internal.ContentRepository within module 'publishing'!
```

The second message only appears after `ContentRepository` moves to `internal`. The Java compiler
is happy with that import (the class is public), which is exactly why the check has to run as a
test.

### Breaking the cycle with an event

```java
public Content publish(String title) {
    var content = repository.save(title);
    events.publishEvent(new ContentPublished(content));
    return content;
}
```

`publishing` no longer references `notification`. `notification` still depends on `publishing`
(it listens to `ContentPublished`), and that single direction is fine: the dependency graph is now
acyclic.

### Declaring allowed dependencies

```java
@ApplicationModule(allowedDependencies = "publishing")
package com.kurz.contentmod.notification;
```

Without it, a module may depend on any other module's API. With it, a new dependency on a third
module fails `verify()` until somebody changes this line on purpose, in a reviewable diff.

## Summary

Package structure is only organization until something enforces it. `ApplicationModules.verify()`
turns the structure into a rule that fails the build: cycles and access to internal types are
reported even when the code compiles, and `allowedDependencies` makes the intended graph explicit.
