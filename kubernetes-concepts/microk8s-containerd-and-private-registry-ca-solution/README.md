# MicroK8s containerd and Private Registry Trust (Solution)

## Overview

`trust-registry.sh` creates `/var/snap/microk8s/current/args/certs.d/registry.lab:5443/` with the CA as `ca.crt` and a `hosts.toml` that points at it. Three tests check the script, a Pod pulling from the registry, and that verification stays on.

## Key Concepts

- **The host's trust store is not containerd's.** Verified: with the CA installed through `update-ca-certificates` only, the Pod still failed to pull. The MicroK8s containerd takes registry trust from `certs.d`.
- **The directory name is the registry host as written in the image, port included.** `certs.d/registry.lab/` does not apply to `registry.lab:5443/...`.
- **No restart needed.** containerd reads `certs.d` on every pull (verified: the Pod pulled right after the files were written). Changes to the `args` files and `containerd-template.toml` do need `snap restart microk8s`.
- **Trust, not skip.** `skip_verify = true` also makes pulls work, and accepts any certificate, including an attacker's. Credentials do not belong in `hosts.toml` either: use `imagePullSecrets`.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/microk8s-containerd-and-private-registry-ca-solution test
```
