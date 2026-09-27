# Centralized Logs with Loki and Grafana Alloy

## Goal

Ship the logs of every Pod to Loki with Grafana Alloy, labelled so they can be searched by namespace, and with passwords masked before they leave the cluster. The tests run Loki and Alloy on a real Kubernetes cluster (k3s, started by Testcontainers) and query Loki's API.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which pulls Loki and Alloy from `mirror.gcr.io` and `busybox`)
- The article "Centralized Logs with Loki and Grafana Alloy"

## Task

The test deploys (`src/test/resources/k8s/logging.yaml`, given) Loki and Alloy in `logging`, and a `checkout` Pod in `shop` that logs an order line and a login line (with a password) every second. Alloy reads its pipeline from your `src/main/resources/k8s/alloy-config.yaml`. The test checks, through Loki's API, that:

- `{namespace="shop"}` returns the checkout logs;
- no stored log line contains the password, and the login lines show `password=****` instead.

You only edit `alloy-config.yaml`.

## Instructions

Run the tests first. The failure message shows the labels Loki knows and Alloy's last errors.

- TODO-00: Make the logs reach Loki.
- TODO-01: Make them searchable by namespace.
- TODO-02: Mask the passwords before they are sent.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/centralized-logs-loki-alloy test
```

## Bonus (Optional)

- TODO-03 (optional): Add the Pod's `app` label as a Loki label, and then think about adding the order number as one. Why is the first fine and the second a problem for Loki?
