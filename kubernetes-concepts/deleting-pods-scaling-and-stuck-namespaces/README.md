# Deleting Pods, Scaling and Stuck Namespaces

## Goal

Stop an application the right way, and unblock a namespace stuck in `Terminating` without leaving anything behind. You write a small operations script; the tests run it against a real Kubernetes cluster (k3s, started by Testcontainers) and check the result.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Deleting Pods, Scaling and Stuck Namespaces"

## Task

The test creates (from `src/test/resources/k8s/cluster.yaml`, given) a Deployment `web` with 2 replicas in `shop`, and a namespace `old-team` containing a ConfigMap with a finalizer that no controller will ever remove. It deletes `old-team`, waits until it is stuck, and then runs your `src/main/resources/ops.sh` inside the cluster node. It checks that:

- the script exits with 0;
- `deploy/web` still exists, but no `web` Pod is running;
- `old-team` disappears, and when it is created again nothing from the old namespace comes back.

You only edit `ops.sh`.

## Instructions

Run the tests first.

- TODO-00: Stop `web` so that its Pods do not come back.
- TODO-01: Unblock `old-team`. The failure message shows the namespace's status conditions, which say exactly what is left. Do not remove the namespace's own `kubernetes` finalizer (for example through the `/finalize` subresource): the last test detects it.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/deleting-pods-scaling-and-stuck-namespaces test
```

## Bonus (Optional)

- TODO-02 (optional): Solve TODO-01 by force-finalizing the namespace instead and run the tests. Look at the `leftover` ConfigMap that comes back: what is its `deletionTimestamp`, and why is it still in etcd?
