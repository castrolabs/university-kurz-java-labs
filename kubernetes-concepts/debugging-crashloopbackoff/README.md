# Debugging CrashLoopBackOff

## Goal

Diagnose and fix three Pods stuck in `CrashLoopBackOff`, each for a different reason: missing configuration, a wrong command, and an out-of-memory kill. The tests run the apps on a real Kubernetes cluster (k3s, started by Testcontainers) and show the same evidence you would look at in production.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Debugging CrashLoopBackOff"

## Task

The test applies your `src/main/resources/k8s/apps.yaml` (three Deployments, `api`, `worker` and `cache`, plus a ConfigMap) and waits 20 seconds. Each app must then be ready with **0 restarts**, and:

- `api` logs `connected to postgres://db.shared:5432/shop`;
- `worker` logs `working`;
- `cache` still has a memory limit.

For every app that is not running, the failure message shows the container's `state`, its `lastState` (reason and exit code) and its last log lines. You only edit `apps.yaml`.

## Instructions

Run the tests first. All three fail. For each one, decide from the evidence alone what is wrong before you look at the manifest: the exit code, the `reason` (`Error`, `RunContainerError`, `OOMKilled`) and whether there are logs at all.

- TODO-00: Fix `api` without changing the ConfigMap.
- TODO-01: Fix the command of `worker`.
- TODO-02: Fix `cache` while keeping a memory limit.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/debugging-crashloopbackoff test
```

## Bonus (Optional)

- TODO-03 (optional): Watch `kubectl get pod -w` for one of the broken apps. How does the wait between restarts grow, and what is its maximum?
