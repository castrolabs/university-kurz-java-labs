# Deploying from a CI Pipeline (Solution)

## Overview

`deploy.sh` applies the manifest, sets the image, waits with `kubectl rollout status --timeout=60s`, and on failure runs `kubectl rollout undo`, waits again and exits with 1. `ci-deployer` gets a Role in `shop` with the Deployment and ReplicaSet verbs the script uses, and nothing cluster-wide. Three tests run two releases and check the permissions.

## Key Concepts

- **A deploy step must wait.** `kubectl apply` and `set image` return when the API server has stored the change; without `rollout status` every release is green, including one whose image does not exist.
- **Time-box and roll back.** The timeout bounds how long a stuck release blocks the pipeline. `rollout undo` restores the previous template; with `maxUnavailable: 0` the old Pods kept serving the whole time, so users never saw the broken release.
- **Least privilege for the pipeline.** A CI token is stored outside the cluster and used by every job, which makes it a favourite target. `cluster-admin` means a leaked token owns the cluster (and every Secret in it). A namespaced Role with the verbs the script needs limits the damage to redeploying one app.
- **Find the verbs by running it.** `kubectl auth can-i --as=system:serviceaccount:shop:ci-deployer ...` and the `forbidden` errors of a dry run show exactly what is missing: here `rollout undo` needs to list ReplicaSets to find the previous revision.
- **Short-lived credentials.** `kubectl create token` issues expiring tokens; better still, many CI systems can exchange their own OIDC identity for cluster credentials, so no long-lived secret exists at all.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/deploying-from-ci-pipeline-solution test
```
