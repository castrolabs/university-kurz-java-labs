# Alertmanager and Basic Alerts

## Goal

Write an alerting rule that Prometheus actually loads, that matches real series, and that fires fast enough to be useful, and see it arrive in Alertmanager. The tests install the Prometheus Operator on a real Kubernetes cluster (k3s, started by Testcontainers) and query the Prometheus and Alertmanager APIs.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which downloads the Prometheus Operator v0.94.1 and pulls images from `quay.io`)
- The article "Alertmanager and Basic Alerts"
- The lab "Prometheus, Grafana and ServiceMonitors" helps, but is not required

## Task

The test installs the operator and deploys (`src/test/resources/k8s/`, given) `example-app` in `shop` with its ServiceMonitor, a client that keeps requesting a page that does not exist (so the app answers 404 every second), and Prometheus plus Alertmanager in `monitoring`. It applies your `src/main/resources/k8s/rules.yaml` and, within about two and a half minutes, checks that:

- Prometheus has loaded the rule `ExampleAppErrors`;
- the alert is firing in Alertmanager, with `severity: warning`.

You only edit `rules.yaml`.

## Instructions

Run the tests first. The failure messages show the rule groups Prometheus loaded, its alerts and their state, and the real labels of `http_requests_total`.

- TODO-00: Make Prometheus load the rule.
- TODO-01: Make the expression match the app's series.
- TODO-02: Make the alert fire within about a minute of sustained errors.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/alertmanager-basic-alerts test
```

## Bonus (Optional)

- TODO-03 (optional): Route `severity: warning` alerts to a receiver with an `AlertmanagerConfig` in `shop`. Which label must the `AlertmanagerConfig` have, and which matcher does the operator add to its routes automatically?
