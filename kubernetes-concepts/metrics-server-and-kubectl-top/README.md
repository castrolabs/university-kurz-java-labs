# metrics-server and kubectl top

## Goal

Make a HorizontalPodAutoscaler work, and keep deployments from undoing its decisions. The tests run on a real Kubernetes cluster (k3s, started by Testcontainers), which ships metrics-server, so `kubectl top` and CPU autoscaling work as in production.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "metrics-server and kubectl top"
- Patience: metrics-server needs a minute or two after the cluster starts, and the starter takes about six minutes to fail

## Task

Your `src/main/resources/k8s/worker.yaml` has a Deployment `worker` that keeps its CPU busy, and an HPA that should keep it at 50% CPU utilization with 1 to 3 replicas. The test applies it and checks that:

1. the HPA gets a CPU utilization, and `kubectl top pods` shows the worker;
2. the worker is limited to `200m` CPU;
3. the HPA scales the busy worker to 3 replicas, and applying `worker.yaml` again (the next release) keeps 3.

You only edit `worker.yaml`.

## Instructions

Run the tests first. The failure message shows the HPA's conditions.

- TODO-00: Make the HPA able to compute a utilization, and set the limit.
- TODO-01: Make `kubectl apply` leave the replica count to the HPA.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/metrics-server-and-kubectl-top test
```

## Bonus (Optional)

- TODO-02 (optional): While the test runs, compare `kubectl top pods` with the HPA's `TARGETS` column. With a request of `100m` and a limit of `200m`, why does the HPA report 200% and never more?
