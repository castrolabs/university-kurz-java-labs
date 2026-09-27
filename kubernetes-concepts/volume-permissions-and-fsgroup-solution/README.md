# Volume Permissions and fsGroup (Solution)

## Overview

The Pod's `securityContext` sets `runAsUser: 1000`, `runAsGroup: 1000` and `fsGroup: 1000`. Three tests verify on a real k3s cluster that the app runs as 1000:1000, reads its key, and that the key is still not world readable.

## Key Concepts

- **`runAsNonRoot` checks the effective user, not your intention.** The busybox image runs as root, so the kubelet refuses to start it (`container has runAsNonRoot and image will run as root`). An explicit non-zero `runAsUser` fixes it.
- **A non-root process cannot read root's 0400 files.** Secret files are owned by root. Making them `0444` would work and expose the key to every user in the container.
- **`fsGroup` changes ownership instead of widening access.** For volume types that support it, the kubelet sets the group of the files to `fsGroup`, adds group read (the key becomes `0440 root:1000`) and adds the group to every process. Verified: without `fsGroup` the file is `-r-------- 0 0`, with it `-r--r----- 0 1000`.
- **The same mechanism fixes new block volumes** (a fresh ext4 disk is `root:root 0755`). It does nothing for `hostPath` volumes, which is why manifests tested only on hostpath-style storage (MicroK8s, k3s local-path, both `0777`) break later on real storage.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/volume-permissions-and-fsgroup-solution test
```
