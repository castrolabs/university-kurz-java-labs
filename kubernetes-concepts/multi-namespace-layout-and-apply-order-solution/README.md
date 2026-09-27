# Multi-Namespace Layout and Apply Order (Solution)

## Overview

`deploy/` is split into `crds/` and `app/`, each with a `kustomization.yaml`. `deploy.sh` applies `crds`, waits for the CRD's `Established` condition, applies `app`, and waits with `kubectl rollout status`. Four tests check the first run, the objects, availability and a second run.

## Key Concepts

- **`kubectl apply -f <dir>` is alphabetical.** `app.yaml` comes before `namespace.yaml`, so its objects fail with `namespaces "shop" not found`. The second run "works", which is why this problem survives in many repositories.
- **Kustomize sorts by kind.** Namespaces, then CRDs, ServiceAccounts, RBAC, ConfigMaps and Secrets, Services, and then workloads, whatever the order in `resources:`. One `kubectl apply -k` handles ordinary dependencies.
- **A CRD is not usable the moment it is created.** kubectl resolves every kind of the apply before sending anything, so a `Widget` in the same apply as its CRD fails with `no matches for kind "Widget" ... ensure CRDs are installed first`, even when Kustomize puts the CRD first. Apply CRDs as a separate step and `kubectl wait --for=condition=Established`.
- **`apply` returns when the API server has stored the objects,** not when Pods run. `kubectl rollout status` (or `kubectl wait`) is what turns a script's exit code into "the app is up".
- **Layout follows lifecycle.** Cluster-wide things (CRDs, namespaces, RBAC) change rarely and are often owned by someone else; the app changes on every release. Separate directories make that visible and let each be applied on its own.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/multi-namespace-layout-and-apply-order-solution test
```
