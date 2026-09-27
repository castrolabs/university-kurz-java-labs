# Prometheus, Grafana and ServiceMonitors

## Goal

Get an application scraped by a Prometheus managed by the Prometheus Operator, by writing the `ServiceMonitor` that connects them. The tests install the operator on a real Kubernetes cluster (k3s, started by Testcontainers) and query Prometheus's API for the app's targets and metrics.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which downloads the Prometheus Operator v0.94.1 and pulls images from `quay.io`)
- The article "Prometheus, Grafana and ServiceMonitors"

## Task

The test installs the operator, deploys `example-app` (2 replicas, metrics on the Service port `web`) in `shop` (`src/test/resources/k8s/app.yaml`, given), applies your `src/main/resources/k8s/servicemonitor.yaml`, and starts a Prometheus `lab` in `monitoring` (`monitoring.yaml`, given). It checks that:

- `up{namespace="shop"}` is 1 for both replicas;
- the app's own metrics (`version{namespace="shop"}`) are stored.

You only edit `servicemonitor.yaml`.

## Instructions

Run the tests first. Prometheus never reports an error for a ServiceMonitor it ignores or cannot match; the failure message shows what it did load: its scrape pools, the healthy targets, and how many discovered endpoints each pool dropped.

- TODO-00: Make Prometheus `lab` load the ServiceMonitor.
- TODO-01: Select the right Service.
- TODO-02: Point at the right port.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/prometheus-grafana-and-servicemonitors test
```

## Bonus (Optional)

- TODO-03 (optional): Grafana reads from Prometheus with PromQL. Write the query a dashboard panel would use for the request rate of `example-app` per Pod over the last 5 minutes, and try it against the Prometheus API as the test does.
