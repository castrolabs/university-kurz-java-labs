# Projected Volumes: Combining Multiple Secrets (Solution)

## Overview

`pod.yaml` keeps the single `projected` volume from the starter and adds `items` to each source, mapping every key into a per-tenant directory. Five tests verify the result against a real k3s cluster.

## Key Concepts

- **A projected volume merges all sources into one directory, and conflicts are silent.** With no `items`, both Secrets write `DB_USER` and `DB_PASSWORD` to the same path. The API server accepts the Pod, the kubelet mounts it, no event is emitted, and the last source in the list wins (`naiveProjectionSilentlyLetsTheLastSourceWin` reads `beta-s3cret`). Swap the order of the sources and tenant alpha's app would authenticate as beta without any error.
- **`items` renames, and `path` may contain directories.** `key: DB_PASSWORD` with `path: alpha/DB_PASSWORD` is all it takes to namespace a key, so both tenants share one mount and one volume.
- **`items` is an allow-list.** The moment a source has `items`, only the listed keys are projected. That is why `tenantSpecificKeysAreNotDropped` exists: forgetting `ALPHA_API_KEY` fails nothing at deploy time, it just is not there when the app looks for it.
- **`readOnly: true` covers the whole mount**, subdirectories included (`mountIsReadOnly` tries to write into `alpha/`). Secret volumes are tmpfs-backed and rewritten by the kubelet anyway, so a write would be lost on the next sync even if it were allowed.
- **Files are symlinks into a timestamped `..data` directory.** The kubelet updates Secrets atomically by swapping the `..data` link, which is why `ls -la /run/secrets` shows `alpha` and `beta` as links: a running process never sees half of an update.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/projected-volumes-combining-multiple-secrets-solution test
```
