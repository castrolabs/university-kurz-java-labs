# Kustomize Generators and the Hash Suffix

## Goal

Replace hand-written ConfigMap and Secret YAML with Kustomize generators, and see why the hash suffix they add is what makes configuration changes roll out on their own. The tests apply your Kustomize directories to a real Kubernetes cluster (k3s, started by Testcontainers, whose `kubectl` includes Kustomize).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The articles "Kustomize: Bases, Overlays and Components" and "Kustomize Generators and the Hash Suffix"

## Task

`src/main/resources/kustomize` has a base (a Deployment and a ConfigMap) and a `staging` overlay (a patch and a Secret). The test runs `kubectl apply -k overlays/staging`, then changes `GREETING` in your files from `hi-staging` to `hi-v2`, applies again, and expects the running Pod to see the new value within a minute. You only edit files under `kustomize/`.

Preview what you build at any time:

```bash
kubectl kustomize src/main/resources/kustomize/overlays/staging
```

## Instructions

Run the tests first. The Pod works, but the configuration change never reaches it, and the password comes from a file committed to Git.

- TODO-00 (`base/kustomization.yaml`): Delete `configmap.yaml` and generate `app-config` with a `configMapGenerator`.
- TODO-01 (`overlays/staging/kustomization.yaml`): Replace the patch with a `configMapGenerator` entry that merges `GREETING=hi-staging` and `ENVIRONMENT=staging` into the base. Delete `config-patch.yaml`.
- TODO-02 (`overlays/staging/kustomization.yaml`): Delete `secret.yaml` and generate `db-credentials` from `db.env`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kustomize-generators-and-hash-suffix test
```

## Bonus (Optional)

- TODO-03 (optional): After the tests pass, run `kubectl kustomize` twice with different values and compare the ConfigMap names. What happens to the old ConfigMaps in a cluster after many deploys, and why is keeping them useful for `kubectl rollout undo`?
