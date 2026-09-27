# Pod Security Standards and Admission

## Goal

Make a namespace enforce the `restricted` Pod Security Standard, then get a Deployment running in it. The tests run on a real Kubernetes cluster (k3s, started by Testcontainers), where the built-in Pod Security Admission controller checks every Pod.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Pod Security Standards and Admission"

## Task

The test applies `src/main/resources/k8s/namespace.yaml`, then tries to create a Pod with no `securityContext` (`src/test/resources/k8s/root-pod.yaml`, given), then applies your `src/main/resources/k8s/web.yaml` and waits for its rollout. It checks that:

- `prod` enforces `restricted` (pinned to `latest`) and also warns and audits at that level;
- the root Pod is rejected by the API server;
- `web` has an available Pod, running as a non-root user and answering on port 8080.

You edit `namespace.yaml` and `web.yaml`.

## Instructions

Run the tests first. Everything runs as root and nothing is rejected.

- TODO-00: Add the Pod Security labels to `prod`.
- TODO-01: Run the tests again. `kubectl apply` of the Deployment still succeeds, but the rollout never completes: Pod Security Admission checks Pods, not Deployments, so it is the ReplicaSet controller that gets rejected. Read the `FailedCreate` event in the failure message and add each setting it asks for.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/pod-security-admission test
```

## Bonus (Optional)

- TODO-02 (optional): Label the namespace with `enforce: baseline` and `warn: restricted` instead. Apply the original Deployment by hand: what does `kubectl apply` print, and does the Pod start? When is this combination useful?
