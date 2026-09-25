# Projected Volumes: Combining Multiple Secrets

## Goal

Mount two tenants' Secrets into the same directory with a single `projected` volume, and find out what happens when both Secrets use the same key names. The test runs against a real Kubernetes cluster (k3s, started by Testcontainers), so what you see is exactly what a production kubelet does.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Kubernetes Secrets Fundamentals" (`kubectl create secret generic --from-env-file`)

## Task

The test creates the namespace and both Secrets for you, exactly like you would by hand:

```bash
kubectl -n example create secret generic tenant-alpha \
  --from-env-file=secrets/example/alpha.env

kubectl -n example create secret generic tenant-beta \
  --from-env-file=secrets/example/beta.env
```

Then it applies `src/main/resources/k8s/pod.yaml`, which already projects both Secrets into `/run/secrets` (read-only). The catch: both `.env` files define `DB_USER` and `DB_PASSWORD`. Your application needs *both* tenants' credentials, plus each tenant's own key (`ALPHA_API_KEY`, `BETA_WEBHOOK_TOKEN`), laid out like this:

```
/run/secrets/alpha/DB_USER
/run/secrets/alpha/DB_PASSWORD
/run/secrets/alpha/ALPHA_API_KEY
/run/secrets/beta/DB_USER
/run/secrets/beta/DB_PASSWORD
/run/secrets/beta/BETA_WEBHOOK_TOKEN
```

You only edit `pod.yaml`. The `.env` files and the test are given.

## Instructions

Run the tests once before changing anything. `naiveProjectionSilentlyLetsTheLastSourceWin` passes and tells you the whole story: Kubernetes does not reject the conflict, it does not even emit an event. The later source just wins.

Complete the following TODOs in `src/main/resources/k8s/pod.yaml`:

- TODO-00: Add `items` to the `tenant-alpha` source so every key lands under `alpha/`.
- TODO-01: Do the same for `tenant-beta` under `beta/`. Do not forget the tenant-specific key: `items` is an allow-list, and an unlisted key silently disappears from the volume.

Run the tests until they all pass.

## Running the Lab

From the project root:

```bash
mvn -pl kubernetes-concepts/projected-volumes-combining-multiple-secrets test
```

Or from the lab directory:

```bash
cd kubernetes-concepts/projected-volumes-combining-multiple-secrets
mvn test
```

The first run pulls the k3s image (about 250 MB); later runs take around 30 seconds.

## Bonus (Optional)

- TODO-02 (optional): Add `defaultMode: 0400` to the projected volume and check the result with `kubectl -n example exec app -- stat -c %a /run/secrets/alpha/DB_PASSWORD`. Then run `ls -la /run/secrets`: why are `alpha` and `beta` symlinks into a `..data` directory?
