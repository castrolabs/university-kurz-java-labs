# Kubeconfig, Contexts and Admin Access (Solution)

## Overview

`merge.sh` renames every `default` cluster, user and context to the environment's name in a copy of each file, merges the copies with `kubectl config view --flatten`, and sets `staging` as the current context. Four tests use the merged file against two real clusters.

## Key Concepts

- **Kubeconfig files merge by name, and the first file wins.** With `KUBECONFIG=a:b`, an entry in `b` with the same name as one in `a` is dropped without any warning. Two clusters installed the same way (k3s, kind, many installers) produce exactly this collision, and the "merged" file silently points at one cluster.
- **Three kinds of entries, three names.** A context joins a cluster entry and a user entry by name. `kubectl config rename-context` renames only the context; clusters and users must be renamed in the file itself (or recreated with `set-cluster` and `set-credentials`).
- **`--flatten` makes the file portable.** It embeds certificate data instead of file paths, so the merged file keeps working after the sources are deleted, and must then be protected like a key.
- **Choose the default on purpose.** The current context is where every command without `--context` goes. Defaulting to staging (or to nothing) makes the dangerous cluster an explicit choice; a shell prompt that shows the context helps too.
- **Admin kubeconfigs are keys to the whole cluster.** The certificates k3s and kubeadm generate are in `system:masters`, which bypasses RBAC and cannot be revoked short of rotating the cluster CA. Keep them for break-glass use and give people their own, RBAC-bound credentials.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubeconfig-contexts-and-admin-access-solution test
```
