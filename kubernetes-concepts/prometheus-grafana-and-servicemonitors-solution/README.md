# Prometheus, Grafana and ServiceMonitors (Solution)

## Overview

The ServiceMonitor has the label `release: lab`, selects Services labelled `app: example-app`, and scrapes the Service port named `web`. Two tests query Prometheus for `up` and for the app's metrics.

## Key Concepts

- **Three selectors, three silent failures.**
  - Prometheus `serviceMonitorSelector` picks ServiceMonitors by **their** labels. Without `release: lab`, the ServiceMonitor is ignored: no scrape pool at all. (With kube-prometheus-stack, the label is usually `release: <helm release name>`.)
  - ServiceMonitor `selector` picks **Services** by their labels. A mismatch leaves a scrape pool whose discovered endpoints are all dropped.
  - `endpoints[].port` is the **name** of a Service port. A wrong name also drops everything.
- **Read Prometheus, not the ServiceMonitor.** `/api/v1/scrape_pools`, the targets page (`/targets`) and the dropped-target counts show which of the three is wrong. The ServiceMonitor object itself looks valid in every case.
- **Changes take minutes to arrive.** The operator writes the configuration to a Secret mounted in the Prometheus Pod; the kubelet refreshes it within one to three minutes (verified: about three), and only then the config reloader tells Prometheus. Wait before concluding that a fix did not work.
- **Grafana is a reader.** Dashboards run PromQL against Prometheus as a data source; if a target is not in Prometheus, no dashboard can show it.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/prometheus-grafana-and-servicemonitors-solution test
```
