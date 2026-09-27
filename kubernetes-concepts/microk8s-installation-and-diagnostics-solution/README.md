# MicroK8s Installation and Diagnostics (Solution)

## Overview

`install.sh` runs `snap install microk8s --classic --channel=1.34/stable`, then `microk8s status --wait-ready`, adds `ubuntu` to the `microk8s` group with its own `~/.kube`, and copies the tarball `microk8s inspect` reports to the user's home. Five tests check the result on a real VM.

## Key Concepts

- **Pin the channel.** Without `--channel`, snap installs the snap's default track of the day. Verified in September 2026: a plain install got 1.35 while `latest/stable` was already 1.36, so the same script installs different versions over time. `<minor>/stable` gets patch updates automatically and a new minor only when you change the channel.
- **Installed is not ready.** `snap install` returns when the services start; the node became `Ready` only later. `microk8s status --wait-ready` is the right first line of anything that uses the cluster.
- **The `microk8s` group is cluster-admin.** It gives read access to the admin credentials, so treat it like the `docker` group. A new login session (here, every `multipass exec`) picks up the group; an existing shell needs `newgrp microk8s`.
- **`microk8s inspect` first.** It checks every service and packs their logs, the component arguments and host details into one tarball, which is what to attach to any bug report.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/microk8s-installation-and-diagnostics-solution test
```
