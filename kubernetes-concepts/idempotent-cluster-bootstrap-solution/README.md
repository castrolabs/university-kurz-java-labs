# Idempotent Cluster Bootstrap (Solution)

## Overview

Every `kubectl create` becomes `kubectl create ... --dry-run=client -o yaml | kubectl apply -f -`, and the label uses `--overwrite`. Four tests run the script three times and check the values, the permissions and the output.

## Key Concepts

- **`create` states an action, `apply` states a result.** `kubectl create` fails with `AlreadyExists` on the second run, and would never update a value anyway. `kubectl apply` creates, updates or leaves alone, so running it twice is safe.
- **Generators plus apply.** `kubectl create <kind> ... --dry-run=client -o yaml` is the quickest way to build a correct object (a Secret from literals, a RoleBinding) without writing YAML by hand; piping it into `apply` makes it idempotent.
- **Every imperative verb has its own flag.** `kubectl label` and `kubectl annotate` need `--overwrite` to change an existing value; `kubectl create namespace` has no "if missing" option at all.
- **Secrets in pipelines stay in the pipe.** The generated YAML contains the password in base64, which is only encoding. Never print it, never run the script with `set -x`, and remember that CI systems keep logs for a long time. `kubectl apply` itself prints only `secret/db-creds configured`.
- **Some fields are immutable.** A RoleBinding's `roleRef` (like a Deployment's selector or a Job's template) cannot be changed by `apply`; the object has to be deleted and recreated, or reconciled with `kubectl auth reconcile` for RBAC.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/idempotent-cluster-bootstrap-solution test
```
