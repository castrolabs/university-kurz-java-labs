# ConfigMaps and Environment Variables

## Goal

Get a Pod that consumes a ConfigMap and a Secret as environment variables from `CreateContainerConfigError` to running, and find out where a variable nobody declared comes from. The tests run against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "ConfigMaps and Environment Variables"

## Task

The test creates, in namespace `example`:

- ConfigMap `app-config` with `APP_MODE=production` and `LOG_LEVEL=info` (shared by every app, do not change it)
- Secret `db-credentials` with keys `username` and `password`
- Service `redis` (port 6379)

Then it applies `src/main/resources/k8s/app.yaml`. The app refuses to start if `REDIS_PORT` is set to something that is not a number. You only edit `app.yaml`.

## Instructions

Run the tests first: all five fail with the Pod's current state. Fix one error at a time, re-running the tests after each change; the kubelet reports the first problem it finds, so every fix reveals the next one.

- TODO-00: `feature-flags` does not exist in this environment, and it must not block the Pod.
- TODO-01: `DB_PASSWORD` references a key the Secret does not have.
- TODO-02: The Pod starts and crashes with `REDIS_PORT is not a number`. Find where that variable comes from and stop it.
- TODO-03: Run this Pod with `LOG_LEVEL=debug` while `app-config` keeps `info`.

The cluster is removed at the end of each run, so rely on the failure messages: they include the Pod's state and message.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/configmaps-and-environment-variables test
```

The first run pulls the k3s image (about 250 MB). A broken starter takes about two minutes (the test waits for the Pod); once fixed it takes around 30 seconds.

## Bonus (Optional)

- TODO-04 (optional): Add `prefix: FF_` to the `feature-flags` source and create the ConfigMap with one key. Which variable name does the container get? What happens if two `envFrom` sources define the same key?
