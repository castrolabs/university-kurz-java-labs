# securityContext Hardening

## Goal

Run nginx with a read-only root filesystem, no Linux capabilities, no privilege escalation, a seccomp filter and a non-root user, and keep it serving traffic. The tests read `/proc/1/status` inside the container on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `nginx` and `busybox` inside it)
- The article "securityContext Hardening"

## Task

The test applies your `src/main/resources/k8s/web.yaml` (a Deployment and a Service) and a `client` Pod (given), then checks that:

- the container cannot write to its root filesystem;
- its main process has `CapEff: 0000000000000000`, `NoNewPrivs: 1` and `Seccomp: 2`, and does not run as UID 0;
- `http://web` (port 80) still returns the nginx welcome page from the `client` Pod.

You only edit `web.yaml`.

## Instructions

Run the tests first. nginx serves the page, but as root with the default capabilities and a writable filesystem.

- TODO-00: Add the `securityContext` settings (Pod level and container level) that the tests check.
- TODO-01: Run the tests again. nginx now crashes on start; the failure message shows its last log lines. Make it run without weakening any of the settings from TODO-00.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/securitycontext-hardening test
```

## Bonus (Optional)

- TODO-02 (optional): Solve TODO-01 the other way too. If you switched images, go back to `nginx:1.29-alpine` and find every directory it needs to write to; if you kept the official image, try `nginxinc/nginx-unprivileged`. Which solution would you rather maintain?
