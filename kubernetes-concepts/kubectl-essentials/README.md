# kubectl Essentials

## Goal

Do three everyday kubectl tasks without the classic accidents: deploying into the wrong namespace, deleting more than intended, and parsing a table made for humans. You write a small script; the tests run it on the node of a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "kubectl Essentials"

## Task

The namespace `shop` has a Deployment `cache`, a bare Pod `web-stable` and two canary Pods labelled `track=canary` (`src/test/resources/k8s/shop.yaml`, given). The test runs your `src/main/resources/ops.sh` and checks that:

- `api` (from `src/main/resources/k8s/api.yaml`) is deployed into `shop`, and nothing into `default`;
- the canary Pods are gone and `web-stable` is untouched;
- the script prints exactly `api busybox:1.37` and `cache busybox:1.36`, one per line, sorted.

You edit `ops.sh` (and may edit `api.yaml`).

## Instructions

Run the tests first; every task goes wrong in its own way.

- TODO-00: Deploy `api` into `shop`.
- TODO-01: Delete only the canary Pods.
- TODO-02: Print the report from the objects' fields.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubectl-essentials test
```

## Bonus (Optional)

- TODO-03 (optional): Before deleting, run the same command with `--dry-run=client`. What does it print, and why is that a good habit for any delete with a selector?
