# Node Capacity, Allocatable and Memory Budget

## Goal

Keep memory back for the node itself, and compute how many more replicas of a workload the scheduler can still place, the way the scheduler computes it. The tests run on a real Kubernetes node (k3s, started by Testcontainers) and check your number against the scheduler's own decisions.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Node Capacity, Allocatable and Memory Budget"

## Task

The test starts k3s with your `src/main/resources/config.yaml` as `/etc/rancher/k3s/config.yaml`, deploys a `db` that requests 1Gi and a `worker` Deployment (256Mi per replica, 0 replicas) (`src/test/resources/k8s/worker.yaml`, given), and runs your `src/main/resources/budget.sh`, which must print one number N. It checks that:

- `capacity - allocatable` is exactly 868Mi (512Mi system, 256Mi Kubernetes, 100Mi eviction threshold);
- scaling `worker` to N schedules every replica, and scaling to N+1 leaves one `Pending`.

The number depends on the memory of your Docker machine; the test only compares it with the scheduler.

## Instructions

Run the tests first.

- TODO-00: Reserve memory and set a memory eviction threshold in `config.yaml`.
- TODO-01: Make `budget.sh` compute what the scheduler compares.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/node-capacity-allocatable-and-memory-budget test
```

## Bonus (Optional)

- TODO-02 (optional): Make the worker actually use 300Mi while requesting 256Mi. Does N change? What happens on a node where every replica does that?
