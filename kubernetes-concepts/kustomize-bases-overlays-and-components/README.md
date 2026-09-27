# Kustomize: Bases, Overlays and Components

## Goal

Build two environments from one base: overlays that change only what differs, and a Component that only one environment opts into. The tests render your overlays with the Kustomize built into `kubectl` (from a k3s container started by Testcontainers) after changing the base, as a new release would.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container for its `kubectl`)
- The article "Kustomize: Bases, Overlays and Components"

## Task

`src/main/resources/kustomize/` holds a `base` (a Deployment `web` with `LOG_LEVEL=info`), a Component `components/metrics` that adds Prometheus scrape annotations, and two overlays. The test first changes the base image from `busybox:1.37` to `busybox:1.38`, then renders both overlays and checks:

| | `overlays/prod` | `overlays/dev` |
|---|---|---|
| name, namespace | `prod-web`, `shop-prod` | `dev-web`, `shop-dev` |
| replicas | 3 | 1 |
| `LOG_LEVEL` | `warn` | `debug` |
| scrape annotations | yes | no |
| image | the base's new image | the base's new image |

You edit the overlays (the base and the Component are fine).

## Instructions

Run the tests first; `kubectl kustomize` explains each failure precisely.

- TODO-00: Include the Component correctly in prod.
- TODO-01: Make the replicas patch match.
- TODO-02: Set `LOG_LEVEL=warn` in prod.
- TODO-03: Build dev on top of the base instead of a copy.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kustomize-bases-overlays-and-components test
```

## Bonus (Optional)

- TODO-04 (optional): Add a `staging` overlay that is scraped like prod but has 1 replica and `LOG_LEVEL=info`. How many new files does it need, and why is the Component the right place for the annotations rather than the base?
