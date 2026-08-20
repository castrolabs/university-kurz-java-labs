# Spring Boot Actuator Endpoints

## Goal

Turn on Spring Boot Actuator's built-in, production-readiness endpoints and understand its two
separate configuration axes: which endpoints are *exposed* over HTTP, and how the `/health`
endpoint *aggregates* several components into one overall status.

## Prerequisites

- The `spring-boot-starter-actuator` dependency and what it auto-configures
- Basic `application.properties` configuration
- Comfort making an HTTP request and reading a JSON response

## Task

`ActuatorLabApplication` is a minimal Spring Boot web application with Actuator on the
classpath. Right now almost everything Actuator can do is invisible: only `/actuator/health`
responds, and it says as little as possible.

The test suite boots the real application on a random port and makes real HTTP requests against
it, checking:

- That `/actuator/info` and `/actuator/beans` become reachable once explicitly opted into
  exposure — and that `/actuator/loggers`, deliberately left off the include list, still 404s.
- That `/actuator/health` reports its component breakdown once `show-details` is turned on.
- That `/actuator/health/readiness` — a health group — reflects a custom `include` list, not
  just the group's default membership.
- That one component reporting `DOWN` (the built-in `diskSpace` indicator, forced below its
  threshold — no custom health indicator required) drags the whole aggregate `/actuator/health`
  status to `DOWN`, with the response's HTTP status code following it to `503`.

## Instructions

Complete the following TODOs in `application.properties`:

- TODO-00: Add `management.endpoints.web.exposure.include=health,info,beans` so `/actuator/info`
  and `/actuator/beans` are reachable (`/actuator/loggers` stays out on purpose).
- TODO-01: Add `management.endpoint.health.show-details=always` so `/actuator/health` includes
  its `components` breakdown, not just the bare aggregate status.
- TODO-02: Add `management.info.env.enabled=true` so the already-present `info.app.name`
  property actually reaches `/actuator/info` (the `env` contributor is opt-in on modern Boot).
- TODO-03: Add `management.endpoint.health.probes.enabled=true` and
  `management.endpoint.health.group.readiness.include=readinessState,diskSpace` so the
  `readiness` health group includes `diskSpace` alongside the default `readinessState`.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl spring-concepts/spring-boot-actuator-endpoints test
```

Or from the lab directory:

```bash
cd spring-concepts/spring-boot-actuator-endpoints
mvn test
```

## Bonus (Optional)

- TODO-04 (optional): Uncomment the `micrometer-registry-prometheus` dependency in `pom.xml`
  and add `prometheus` to `management.endpoints.web.exposure.include`, then run the app and
  `curl http://localhost:8080/actuator/prometheus` to see the Prometheus text-exposition format.
