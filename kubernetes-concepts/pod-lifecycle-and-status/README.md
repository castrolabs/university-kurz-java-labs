# Pod Lifecycle and Status

## Goal

Write a script that classifies Pods reliably from their status fields (phase, conditions, container states) instead of the `STATUS` column of `kubectl get pods`. The tests create one Pod in each common situation on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Pod Lifecycle and Status"

## Task

The test creates seven Pods in the namespace `lab` (`src/test/resources/k8s/pods.yaml`, given; the comment next to each name is the expected category), waits until each one is in its state, and then runs your `src/main/resources/classify.sh lab` four times, three seconds apart. Every run must print exactly:

| Pod | Category |
|---|---|
| `ok` | `ready` |
| `warming-up` | `not-ready` |
| `too-big` | `unschedulable` |
| `bad-image` | `image-pull` |
| `crashing` | `crash-loop` |
| `batch-done` | `succeeded` |
| `batch-failed` | `failed` |

You only edit `classify.sh`.

## Instructions

Run the tests first. The failure message shows what `kubectl get pods` displayed at that moment, so you can compare the `STATUS` column with what the Pod really is.

- TODO-00: Classify from the Pod's status fields. `kubectl get pod <name> -o yaml` on your own cluster (or the article) shows which ones exist.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/pod-lifecycle-and-status test
```

## Bonus (Optional)

- TODO-01 (optional): Extend the script to Pods with several containers and init containers. Which container should decide the category when one is crash-looping and another is healthy?
