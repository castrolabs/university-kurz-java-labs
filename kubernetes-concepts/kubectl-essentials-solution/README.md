# kubectl Essentials (Solution)

## Overview

`ops.sh` applies with `-n shop`, deletes with `-l track=canary`, and prints the report with `--sort-by`, `--no-headers` and `custom-columns`. Four tests check where `api` landed, which Pods remain, and the exact output.

## Key Concepts

- **The namespace comes from somewhere.** A manifest without `metadata.namespace` goes to `-n`, or to the context's namespace, which is usually `default`. Set it in the manifest, pass `-n` every time, or set it on the context (`kubectl config set-context --current --namespace=shop`); pick one habit and keep it.
- **Selectors are the safe way to delete.** `delete pods --all` removes every Pod in the namespace: Deployment Pods come back, bare Pods do not. `-l track=canary` removes exactly what it matches; `--dry-run=client` shows the list first.
- **Tables are for humans.** `get` output changes with `-o wide`, adds a header, and shifts columns when a value has spaces. For scripts ask for the fields: `-o jsonpath`, `-o custom-columns` with `--no-headers`, or `-o json` with `jq`, and `--sort-by` for a stable order.
- **Read before you change.** `kubectl explain deployment.spec.strategy` documents any field offline, and `kubectl diff -f` shows what `apply` would change.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubectl-essentials-solution test
```
