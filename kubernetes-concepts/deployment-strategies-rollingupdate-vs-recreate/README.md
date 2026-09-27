# Deployment Strategies: RollingUpdate vs Recreate

## Goal

Choose the right rollout strategy for two very different workloads on a real Kubernetes cluster (k3s, started by Testcontainers): a stateless web app that must keep full capacity even when a new image is broken, and a singleton that must never run twice.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Deployment Strategies: RollingUpdate vs Recreate"

## Task

The test applies `src/main/resources/k8s/web.yaml` (2 replicas behind a Service) and `src/main/resources/k8s/singleton.yaml` (1 replica), then:

1. rolls `web` out to an image tag that does not exist and, 30 seconds later, checks how many replicas are still available and whether the Service answers;
2. restarts `singleton` and counts, twice per second, how many of its Pods are running at the same time.

You only edit the `strategy` of each Deployment.

## Instructions

Run the tests first and read both failures.

- TODO-00 (`web.yaml`): Change `maxSurge`/`maxUnavailable` so a rollout never goes below 2 available Pods.
- TODO-01 (`singleton.yaml`): Make the rollout stop the old Pod before it starts the new one.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/deployment-strategies-rollingupdate-vs-recreate test
```

A run takes about 90 seconds.

## Bonus (Optional)

- TODO-02 (optional): Set `replicas: 4` on `web` with the default strategy (25% / 25%) and roll out the broken image again. How many Pods exist and how many are available? Work out the rounding rule from what you see.
