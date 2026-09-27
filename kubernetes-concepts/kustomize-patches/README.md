# Kustomize Patches: Strategic Merge vs JSON 6902

## Goal

Fix three patches in a production overlay: one that breaks the build, one that silently drops configuration, and one that misses Deployments it should cover. The tests build and apply the overlay on a real Kubernetes cluster (k3s, started by Testcontainers) and inspect the resulting Deployments.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container)
- The article "Kustomize Patches: Strategic Merge vs JSON 6902"

## Task

The base (`src/main/resources/kustomize/base`) has three Deployments: `web` (role `web`, with an `env` list) and two workers, `worker` and `worker-emails` (role `worker`, no `env` list). All three read the ConfigMap `app-config` through `envFrom`. The production overlay must:

- add `TZ=UTC` to every Deployment (and keep `web`'s own `PORT`);
- give `worker` the Secret `worker-secrets` in addition to `app-config`;
- run every worker with 3 replicas, while `web` keeps 2.

You only edit files under `overlays/production`. Preview the result with:

```bash
kubectl kustomize src/main/resources/kustomize/overlays/production
```

## Instructions

- TODO-00: The overlay does not build (`doc is missing path`). Rewrite the timezone patch so it works for Deployments with and without an `env` list.
- TODO-01: `worker-secrets-patch.yaml` drops the `app-config` reference. Fix it.
- TODO-02: Select the workers by label instead of by name.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kustomize-patches test
```

## Bonus (Optional)

- TODO-03 (optional): Remove `web`'s liveness probe (add one to the base first) with a strategic merge patch that uses `$patch: delete`, then try the same with a JSON 6902 `remove` operation. Which one breaks if the base's container order changes?
