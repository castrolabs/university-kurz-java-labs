# securityContext Hardening (Solution)

## Overview

The Pod runs with `runAsNonRoot` and the `RuntimeDefault` seccomp profile; the container has a read-only root filesystem, `allowPrivilegeEscalation: false` and drops every capability. The image is `nginxinc/nginx-unprivileged`, which listens on 8080 as UID 101, with an `emptyDir` on `/tmp` as its only writable path. The Service maps port 80 to the named port, so clients do not change.

## Key Concepts

- **Hardening breaks images that assume root.** With the official image, nginx fails with `mkdir() "/var/cache/nginx/client_temp" failed (30: Read-only file system)`. It also wants to bind port 80, write its pid file under `/run` and switch to the `nginx` user, none of which works without root or capabilities.
- **Two ways out.** Mount an `emptyDir` on every path the process writes (and move it to a high port), or use an image built to run unprivileged. The second keeps the manifest short and the knowledge in the image.
- **Verify in the kernel, not only in the YAML.** `/proc/1/status` shows what the process really got: `CapEff` (effective capabilities), `NoNewPrivs` (set by `allowPrivilegeEscalation: false`) and `Seccomp` (2 means a filter is active).
- **A readiness probe makes crashes visible.** Without it, a container that starts and dies a second later counts as available, and `kubectl rollout status` reports success.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/securitycontext-hardening-solution test
```
