# Pods, ReplicaSets, Deployments, Services and Namespaces

## Goal

Turn a bare Pod into a Deployment, see the chain of objects Kubernetes builds from it, and put a Service in front of every replica. The tests run on a real Kubernetes cluster (k3s, started by Testcontainers) and delete your Pods to see whether they come back.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Pods, ReplicaSets, Deployments, Services and Namespaces"

## Task

Your `src/main/resources/k8s/web.yaml` creates the namespace `shop`, a web server that answers `hello from <pod name>`, and a Service `web`. The test applies it and checks that:

- the web Pods are owned by a ReplicaSet, which is owned by the Deployment `web`, with 2 ready replicas;
- requests to `http://web.shop` reach both replicas;
- after all web Pods are deleted, new ones come up and the Service answers again.

You only edit `web.yaml`.

## Instructions

Run the tests first.

- TODO-00: Replace the bare Pod with a Deployment of 2 replicas.
- TODO-01: Make the Service select every replica.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kubernetes-core-objects test
```

## Bonus (Optional)

- TODO-02 (optional): Change the image of the Deployment and run `kubectl -n shop get rs`. How many ReplicaSets are there now, and what are their replica counts?
