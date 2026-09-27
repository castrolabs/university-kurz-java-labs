# ConfigMaps and Environment Variables (Solution)

## Overview

`app.yaml` marks the optional ConfigMap as optional, references the right Secret key, disables service links and overrides one variable with an explicit `env` entry. Five tests verify the running Pod against a real k3s cluster.

## Key Concepts

- **Missing references block the container, they do not crash it.** A missing ConfigMap, Secret or key leaves the Pod in `CreateContainerConfigError` with a precise message (`configmap "feature-flags" not found`, `couldn't find key db-password in Secret example/db-credentials`). The kubelet keeps retrying, so creating the object later unblocks the Pod on its own. `optional: true` says the source may legitimately be absent.
- **Errors come one at a time.** `envFrom` is resolved before `env`, and the kubelet reports the first failure only, which is why fixing one TODO reveals the next.
- **Service links inject variables you never declared.** For every Service that exists in the namespace when the Pod starts, Kubernetes adds Docker-link style variables. A Service named `redis` produces `REDIS_PORT=tcp://10.43.x.x:6379`, which collides with any app that reads `REDIS_PORT` as a number. `enableServiceLinks: false` removes them; Services are found through DNS anyway.
- **Explicit `env` beats `envFrom`.** `LOG_LEVEL=debug` in `env` wins over `LOG_LEVEL=info` from the ConfigMap, whatever the order in the manifest. Between two `envFrom` sources with the same key, the last one wins silently.
- **Environment variables are read once.** Changing the ConfigMap now does not change the running container; restart the Pod (or use a hashed ConfigMap name, see the Kustomize generators lab).

## Running the Solution

```bash
mvn -pl kubernetes-concepts/configmaps-and-environment-variables-solution test
```
