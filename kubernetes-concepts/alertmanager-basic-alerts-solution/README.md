# Alertmanager and Basic Alerts (Solution)

## Overview

The PrometheusRule has the label `release: lab`, the expression filters on `code=~"4..|5.."`, and the alert fires after `for: 30s`. Two tests check that the rule is loaded and that the alert reaches Alertmanager.

## Key Concepts

- **Rules are selected like ServiceMonitors.** Prometheus `ruleSelector` picks PrometheusRules by their labels; without `release: lab`, the rule is silently ignored and never appears in `/api/v1/rules`.
- **A selector on a missing label matches nothing, silently.** `status=~"4.."` is valid PromQL; the metric simply has no `status` label (it has `code`), so the expression returns an empty result, which never alerts. Check the real labels (`/api/v1/series`, or the Prometheus UI) before writing the expression.
- **`for` is the delay between pending and firing.** The expression must stay true for the whole `for` duration. `1h` means the team hears about an outage an hour late; `0s` alerts on every blip. Add the scrape interval, the `rate` window and the evaluation interval to know when an alert can fire at the earliest.
- **Prometheus decides, Alertmanager delivers.** Prometheus evaluates the rules and sends firing alerts to Alertmanager, which groups, silences, and routes them to receivers. An alert visible in Alertmanager's API (`/api/v2/alerts`) proves the whole chain up to routing.
- **Rule changes take minutes to arrive** for the same reason as ServiceMonitor changes: they reach the Prometheus Pod through a mounted volume refreshed by the kubelet.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/alertmanager-basic-alerts-solution test
```
