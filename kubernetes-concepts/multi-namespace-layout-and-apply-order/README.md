# Multi-Namespace Layout and Apply Order

## Goal

Make a deployment script that works on an empty cluster at the first attempt: objects created after what they depend on, custom resources after their definition is served, and a script that returns only when the app is available. The tests run it on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Multi-Namespace Layout and Apply Order"

## Task

`src/main/resources/deploy/` holds a Namespace `shop`, a ConfigMap and a Deployment `web` in it, a CustomResourceDefinition for `Widget`, and one `Widget`. The test copies the directory to `/lab/deploy` on a fresh node and runs your `src/main/resources/deploy.sh`. It checks that:

- the first run exits with 0 and prints no errors;
- the ConfigMap and the `Widget` exist;
- `deploy/web` is already available when the script returns;
- a second run also succeeds.

You edit `deploy.sh` and may reorganize `deploy/` freely (new directories, `kustomization.yaml` files).

## Instructions

Run the tests first. The failure message shows every error of the first run.

- TODO-00: Make the Namespace exist before the objects in it, with Kustomize.
- TODO-01: Make the `Widget` be created only once its CRD is served.
- TODO-02: Make the script wait for the app.

Run the tests until they all pass. Running `kubectl apply` twice, or retrying until it works, is not the fix: the first test rejects any error output.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/multi-namespace-layout-and-apply-order test
```

## Bonus (Optional)

- TODO-03 (optional): Put the CRD and the `Widget` in the same kustomization and apply it once. Kustomize sorts CRDs first; why does it still fail?
