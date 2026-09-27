# Centralized Logs with Loki and Grafana Alloy (Solution)

## Overview

The pipeline discovers Pods, adds `namespace`, `pod` and `container` labels, reads logs through the API server with `loki.source.kubernetes`, masks passwords in a `loki.process` stage, and pushes to `http://loki.logging:3100/loki/api/v1/push`. Two tests query Loki for the shop's logs and for the password.

## Key Concepts

- **The push URL includes the API path.** `loki.write` needs the full `/loki/api/v1/push` endpoint; the base URL answers 404, which shows up only in Alloy's own logs while Loki stays empty.
- **Labels are Loki's index.** Queries start with a label selector (`{namespace="shop"}`), and only labels are indexed; the log text is searched by scanning. Every stream needs the labels your team searches by, and only those: labels with many values (user IDs, order numbers, request IDs) create one stream per value and slow Loki down. Put those in the log line and filter with `|=` or a parser.
- **Relabeling turns discovery metadata into labels.** `discovery.kubernetes` provides `__meta_kubernetes_*` fields; anything that starts with `__` is dropped unless a `rule` copies it to a real label.
- **Mask at the source.** A `loki.process` stage runs in the cluster, before anything is sent. Once a secret is stored in Loki it is in every backup and visible to everyone who can query that tenant; retention rules do not remove it quickly.
- **Two ways to read logs.** `loki.source.kubernetes` (used here) tails through the API server and needs only RBAC; reading `/var/log/pods` from a DaemonSet with `hostPath` scales better on large clusters and survives API server load.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/centralized-logs-loki-alloy-solution test
```
