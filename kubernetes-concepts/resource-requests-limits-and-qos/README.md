# Requests, Limits, QoS Classes and OOMKilled

## Goal

Fix three Pods with resource problems on a real Kubernetes cluster (k3s, started by Testcontainers): one that the kernel keeps killing, one that must be the last to be evicted, and one that never gets scheduled.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The articles "Requests, Limits, QoS Classes and OOMKilled" and "Node Capacity, Allocatable and Memory Budget"

## Task

The test applies `src/main/resources/k8s/pods.yaml` (Pods `worker`, `db` and `batch` in namespace `example`) and inspects them after about a minute. You only edit that file.

## Instructions

Run the tests first. `naiveWorkerIsOomKilled` passes and shows the signature of a container killed for exceeding its memory limit: reason `OOMKilled`, exit code `137`.

- TODO-00: `worker` builds a 150 MiB table in memory and keeps restarting. Give it enough memory, but keep a memory limit.
- TODO-01: `db` must have the QoS class `Guaranteed`.
- TODO-02: `batch` stays `Pending`. Read the scheduler's message; the job really needs about 128 MiB.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/resource-requests-limits-and-qos test
```

A run takes one to two minutes.

## Bonus (Optional)

- TODO-03 (optional): Remove the CPU limit from `db` and add it back with a different value than the request. Which QoS class does each variant get, and why is a CPU limit the one place where it is worth accepting CPU throttling?
