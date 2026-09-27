# Kubernetes Events

## Goal

Write a script that prints the most recent Warning event in a namespace, and find out why the obvious `kubectl get events --sort-by=.lastTimestamp` gets it wrong. The tests create real events on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Kubernetes Events"

## Task

In the namespace `shop`, the test runs a Job `migrate` that fails (`src/test/resources/k8s/migrate.yaml`, given), waits a few seconds, and then creates a Pod `report` that asks for more memory than any node has (`report.yaml`, given). Then it runs your `src/main/resources/latest-warning.sh shop` inside the cluster node and checks that it prints exactly one line: the scheduler's `FailedScheduling` warning for `pod/report`.

You only edit `latest-warning.sh`.

## Instructions

Run the tests first. The script prints the Job's warning, which is older. The failure message lists every warning with its `lastTimestamp` and `eventTime`.

- TODO-00: Make the script print the most recent Warning.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubernetes-events test
```

## Bonus (Optional)

- TODO-01 (optional): Print every warning for one object only (for example `pod/report`), including repeated ones. How does `kubectl events` show an event that happened many times, and which field holds the count for each kind of event?
