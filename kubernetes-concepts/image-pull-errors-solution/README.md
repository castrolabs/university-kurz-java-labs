# ImagePullBackOff and ErrImagePull (Solution)

## Overview

`registries.yaml` sets `configs."registry.lab".tls.ca_file` to the lab CA, and `web` uses the tag `1.37`, which exists. Two tests check that the Pod runs and that TLS verification stays on.

## Key Concepts

- **Same status, different causes.** `ErrImagePull` is the first failure, `ImagePullBackOff` the wait before the next attempt. The cause is only in the event message:
  - `x509: certificate signed by unknown authority`: the node does not trust the registry. Fix it on the **node** (containerd configuration), not in the Pod.
  - `NotFound ... not found`: the registry answered and has no such tag. Fix the **manifest** (or push the tag).
  - `401 Unauthorized` / `pull access denied`: credentials, which is the next lab.
- **Trust a CA per registry, do not skip verification.** `ca_file` trusts the private CA for `registry.lab` only; `insecure_skip_verify: true` would accept any certificate, including a malicious one.
- **Node configuration is read at startup.** k3s reads `registries.yaml` when it starts (MicroK8s and plain containerd use `certs.d/<host>/hosts.toml`), so a change needs a restart on every node that pulls from the registry.
- **Wrong architecture is not always an error.** An `amd64`-only image on an `arm64` node usually pulls fine and then fails with `exec format error` (CrashLoopBackOff, not a pull error). On machines with CPU emulation installed, such as Docker Desktop, it even runs, slowly: this lab's author verified that on an Apple Silicon Mac. Publish multi-architecture images and check with `crane manifest` or `docker manifest inspect`.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/image-pull-errors-solution test
```
