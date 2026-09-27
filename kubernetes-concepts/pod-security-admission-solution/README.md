# Pod Security Standards and Admission (Solution)

## Overview

`prod` is labelled with `enforce`, `enforce-version`, `warn` and `audit`, and `web` gets the four settings the `restricted` level requires. Four tests check the labels, the rejection of a root Pod, and the running Deployment.

## Key Concepts

- **Admission checks Pods, not their owners.** `kubectl apply` of a violating Deployment succeeds; the ReplicaSet controller is the one rejected, so the only trace is a `FailedCreate` event and `0/1` ready replicas. Use `warn` (or a server-side dry run of the namespace labels) to see violations at apply time.
- **What `restricted` asks for:** `runAsNonRoot: true` (with a numeric non-root user, because the image's default user is root), `allowPrivilegeEscalation: false`, `capabilities.drop: [ALL]` and `seccompProfile.type: RuntimeDefault`. The error message lists exactly the missing ones.
- **Pin the version for predictable upgrades.** `enforce-version: latest` picks up new rules when the cluster is upgraded; a pinned version (for example `v1.36`) keeps the rules stable until you move it.
- **The app has to cooperate.** Running as UID 1000 means it cannot write outside directories it owns and cannot bind ports below 1024, which is why the server writes to `/tmp` and listens on 8080.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/pod-security-admission-solution test
```
