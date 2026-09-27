# ServiceAccount Tokens and Automount (Solution)

## Overview

`web` sets `automountServiceAccountToken: false`, and `watcher` runs as the new ServiceAccount `config-watcher`, bound to a Role that can only get, list and watch ConfigMaps in `team-a`. Five tests call the API from the Pods on a real k3s cluster.

## Key Concepts

- **Every Pod gets a token by default**, mounted at `/var/run/secrets/kubernetes.io/serviceaccount`, with the permissions of its ServiceAccount (`default` if none is set). For an app that never calls the API, that token is pure attack surface: code execution in the container becomes API access.
- **Permissions on `default` spread silently.** A binding added "to make one tool work" is inherited by every Pod in the namespace that does not choose another ServiceAccount.
- **Dedicated ServiceAccounts make permissions explicit and auditable**: one identity per workload, one Role with exactly what it needs. The test proves it both ways: 200 for ConfigMaps, 403 for Secrets.
- **Projected tokens are bound**: to the Pod (invalid once it is deleted), to an audience, and with an expiry the kubelet renews (TODO-02).
- **The API server is reachable without DNS** through `KUBERNETES_SERVICE_HOST`/`PORT`, which Kubernetes injects into every container; the tests use them so they do not depend on CoreDNS being ready.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/serviceaccount-tokens-and-automount-solution test
```
