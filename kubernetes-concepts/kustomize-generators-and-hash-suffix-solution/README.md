# Kustomize Generators and the Hash Suffix (Solution)

## Overview

The base generates `app-config` with a `configMapGenerator`; the overlay merges its own values into it (`behavior: merge`) and generates `db-credentials` from `db.env` with a `secretGenerator`. Four tests verify the result on a real k3s cluster, including a configuration change.

## Key Concepts

- **Generated names carry a content hash** (`app-config-<hash>`), and Kustomize rewrites every reference to them (`envFrom`, volumes, `imagePullSecrets`...).
- **A new value means a new name, which means a new Pod template.** That template change triggers a normal rolling update, so `kubectl apply -k` alone rolls out configuration. With a plain ConfigMap, the Deployment was unchanged, nothing restarted, and environment variables are only read at container start.
- **`behavior: merge` layers generators.** An overlay generator with the same name as the base must say `merge` (add and override keys) or `replace`; without it the build fails.
- **Secrets from files, not from YAML in Git.** `secretGenerator` reads `db.env` at build time; the file itself belongs in a password manager or encrypted storage, never in the repository.
- **Old generated objects accumulate** (TODO-03). They are what makes `rollout undo` restore the old configuration together with the old Pods; clean them up deliberately.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kustomize-generators-and-hash-suffix-solution test
```
