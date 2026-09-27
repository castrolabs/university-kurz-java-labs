# Services: ClusterIP and NodePort

## Goal

Fix the three most common reasons a Service does not answer: a selector that matches no Pod, a `targetPort` that points at the wrong container port, and a NodePort outside the allowed range. The tests read EndpointSlices and make real HTTP requests on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Services: ClusterIP and NodePort"

## Task

The test applies your `src/main/resources/k8s/shop.yaml` (a Deployment with 2 replicas listening on 8080, a ClusterIP Service `shop` and a NodePort Service `shop-public`) and a `client` Pod (given). It checks that:

- the EndpointSlice of `shop` lists both replicas;
- `http://shop` answers `shop ok` from the `client` Pod;
- `shop-public` is created with node port 30080 and answers on the node's IP.

You only edit `shop.yaml`.

## Instructions

Run the tests first.

- TODO-00: Give `shop` endpoints. `kubectl get endpointslices -l kubernetes.io/service-name=shop` shows what it selects.
- TODO-01: Once `shop` has endpoints, `http://shop` still refuses connections. Fix where the Service sends traffic.
- TODO-02: Read the error from `kubectl apply` and fix `shop-public`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/services-clusterip-and-nodeport test
```

## Bonus (Optional)

- TODO-03 (optional): Remove `nodePort` from `shop-public` entirely and apply it again. Which port does it get, and why is a fixed value only worth it when something outside the cluster depends on it?
