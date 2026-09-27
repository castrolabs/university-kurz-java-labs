# Pods, ReplicaSets, Deployments, Services and Namespaces (Solution)

## Overview

The Pod definition moves unchanged into the template of a Deployment with 2 replicas, and the Service selects `app: web`, a label every replica has. Three tests check the ownership chain, the load balancing and the self-healing.

## Key Concepts

- **A bare Pod is not managed.** Nothing recreates it after a delete, a drain or an eviction. Controllers exist for that: they compare the desired state with what exists and act on the difference.
- **The chain.** A Deployment creates a ReplicaSet per version of its template; a ReplicaSet keeps a number of identical Pods alive. Each object points at its owner in `metadata.ownerReferences`, which is also what deletes children with their parent.
- **Selectors connect objects by labels.** The Deployment's `selector` must match its template labels. A Service's `selector` picks Pods by labels too: a label only one Pod carries (`instance: web-0`) selects only that Pod, or none.
- **Namespaces scope names.** `web` in `shop` is reachable as `web.shop` from anywhere, and `kubectl -n shop` or a manifest's `namespace` decides where objects land.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubernetes-core-objects-solution test
```
