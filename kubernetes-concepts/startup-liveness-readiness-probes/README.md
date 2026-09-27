# Startup, Liveness and Readiness Probes

## Goal

Fix two probe mistakes against a real Kubernetes cluster (k3s, started by Testcontainers): a liveness probe that kills a slow app before it ever finishes starting, and a Deployment whose missing readiness probe lets a broken version replace every healthy Pod.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Startup, Liveness and Readiness Probes"

## Task

The test applies two manifests in namespace `example`:

- `src/main/resources/k8s/slow.yaml`: an app that needs about 40 seconds before it answers `/healthz`.
- `src/main/resources/k8s/web.yaml`: a Deployment with 2 replicas and a Service. After it is up, the test rolls out `VERSION=2`, which starts but never serves `/readyz`, and checks what the Service returns 45 seconds later.

You only edit those two files.

## Instructions

Run the tests first and read the failures: `slow` keeps restarting, and after the broken rollout nobody serves `/readyz` while `kubectl rollout status` says everything is fine.

- TODO-00 (`slow.yaml`): Add a `startupProbe` with a budget of at least 60 seconds. Do not loosen the liveness probe: a hung app must still be restarted within about 30 seconds.
- TODO-01 (`web.yaml`): Add a `readinessProbe` on `/readyz`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/startup-liveness-readiness-probes test
```

A run takes about three minutes: the slow app, the liveness window and the rollout all happen in real time.

## Bonus (Optional)

- TODO-02 (optional): Remove `terminationGracePeriodSeconds: 5` from `slow.yaml` and run the starter version again. Why can the app answer `/healthz` once even though the liveness probe already decided to kill it?
