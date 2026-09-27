# Jobs, CronJobs and restartPolicy

## Goal

Configure four one-off Jobs so that each one ends the way it should: accepted and completed, failed fast, stopped by a deadline, and cleaned up after it finishes. The tests run against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Jobs, CronJobs and restartPolicy"

## Task

The test applies `src/main/resources/k8s/jobs.yaml` in namespace `example` and watches the four Jobs for up to 90 seconds:

- `migrate` must be accepted by the API server and complete.
- `flaky-import` always fails and must be declared failed after exactly 2 attempts.
- `report` hangs forever and must be stopped after 20 seconds.
- `cleanup` must disappear, Pods included, shortly after it finishes.

You only edit `jobs.yaml`.

## Instructions

Run the tests first. The failure for `migrate` contains the API server's validation message.

- TODO-00: Give `migrate` a valid `restartPolicy` that keeps every failed attempt in its own Pod.
- TODO-01: Limit `flaky-import` to one retry.
- TODO-02: Stop `report` after 20 seconds, whatever the retries.
- TODO-03: Delete `cleanup` at most 30 seconds after it finishes.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/jobs-and-restart-policy test
```

A run takes one to two minutes.

## Bonus (Optional)

- TODO-04 (optional): Add an image tag change to `migrate` and apply the file again while the Job still exists. Which error do you get, and which of the four Jobs would make a repeated `kubectl apply` of this file fail in a real pipeline?
