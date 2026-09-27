# Kustomize Patches: Strategic Merge vs JSON 6902 (Solution)

## Overview

The timezone patch becomes a targeted strategic merge patch, the worker patch lists both `envFrom` sources, and the replicas patch selects `role=worker`. Four tests verify the applied Deployments on a real k3s cluster.

## Key Concepts

- **JSON 6902 `add` needs the parent path to exist.** `/containers/0/env/-` appends to a list; the workers have no `env` list, so the whole build fails. JSON patches are precise and brittle: they also depend on the container index.
- **Strategic merge knows Kubernetes list semantics.** `containers` merge by `name` and `env` merges by `name`, so the same patch adds `TZ` to a container with or without an `env` list and keeps `web`'s `PORT`. Used with a `target`, the patch's own `metadata.name` is ignored for matching.
- **Lists without a merge key are replaced, silently.** `envFrom`, `args` and `command` are replaced wholesale by a strategic merge patch. Listing only the new Secret removed the `app-config` reference without any error, which is why the fixed patch repeats both entries.
- **Target by label, not by name.** `labelSelector: role=worker` covers `worker-emails` today and any worker added to the base tomorrow.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kustomize-patches-solution test
```
