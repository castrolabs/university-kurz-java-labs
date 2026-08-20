# Spring Boot Actuator Endpoints - Solution

## Overview

This is the official solution for the Spring Boot Actuator Endpoints lab. It configures
Actuator's exposure list, health detail level, `/info` contributor, and a custom health group —
then a test suite makes real HTTP calls against a running instance to verify each one.

## Key Concepts

### Access vs. exposure

```properties
management.endpoints.web.exposure.include=health,info,beans,prometheus
```

Only `health` is exposed over HTTP by default; everything else exists in the application but is
unmapped. `include` is an explicit allowlist — `/actuator/loggers`, deliberately left off,
returns 404 even though the endpoint itself is fully functional and reachable over JMX or if
added to the list later. This is a different switch from *access* (`management.endpoints
.access.default`, `management.endpoint.<id>.access`), which controls whether an endpoint can
operate at all, independent of whether it's exposed anywhere.

### `/health` detail and aggregation

```properties
management.endpoint.health.show-details=always
```

Unauthenticated, `/actuator/health` says as little as possible: `{"status":"UP"}`. With
`show-details=always`, the response breaks down every contributing component:

```json
{"status":"UP","components":{"diskSpace":{"status":"UP","details":{...}},"ping":{"status":"UP"}}}
```

The top-level `status` is the *worst* status among all components — any single `DOWN` makes the
whole aggregate `DOWN`, and the HTTP status code follows: `200` for `UP`, `503` for `DOWN` or
`OUT_OF_SERVICE`. `HealthAggregationDownTest` proves this without writing a custom
`HealthIndicator` at all — it just points the built-in `diskSpace` indicator's threshold above
any real disk's free space:

```java
@SpringBootTest(properties = "management.health.diskspace.threshold=1000000GB")
```

### `/info` and the opt-in `env` contributor

```properties
management.info.env.enabled=true
info.app.name=Kurz Actuator Lab
```

`build` and `git` info contributors are on by default and cost nothing, but the `env`
contributor — the one that echoes arbitrary `info.*` properties like `info.app.name` — is
disabled by default on modern Spring Boot. Without `management.info.env.enabled=true`,
`info.app.name` is silently ignored and `/actuator/info` returns `{}`.

### Health groups

```properties
management.endpoint.health.probes.enabled=true
management.endpoint.health.group.readiness.include=readinessState,diskSpace
```

A health group is a named subset of components exposed at its own path —
`/actuator/health/readiness` here. Kubernetes needs this distinction because a slow database
should fail readiness (stop routing traffic) without failing liveness (restarting the pod won't
fix a database). This lab folds `diskSpace` into the built-in `readiness` group so the same
threshold trick that turns the aggregate `DOWN` also shows up under `/health/readiness`.

## Trade-offs and Best Practices

1. **Restrictive exposure defaults are safe but look broken**: a correctly configured Actuator
   setup still 404s on `/actuator/metrics` until `exposure.include` explicitly lists it — that's
   by design, not a bug.
2. **`include: '*'` is real information disclosure risk in production** — `/env`, `/beans`, and
   `/heapdump` can leak configuration, wiring, and raw memory contents. Prefer a named allowlist,
   as this lab does, over a wildcard.
3. **Health groups exist because one aggregate status is too blunt for orchestration** — a group
   lets "should I restart this?" and "should I route traffic here?" be answered by different
   subsets of the same indicators.

## Summary

- `management.endpoints.web.exposure.include` is an explicit allowlist; only `health` is
  exposed by default.
- `management.endpoint.health.show-details` controls whether `/health` reveals its component
  breakdown; the top-level status is always the worst status among components regardless.
- `management.info.env.enabled=true` is required before any `info.*` property reaches
  `/actuator/info`.
- A health group is a named, independently addressable subset of health components — useful for
  Kubernetes liveness/readiness probes, and definable for any other purpose via
  `management.endpoint.health.group.<name>.include`.
