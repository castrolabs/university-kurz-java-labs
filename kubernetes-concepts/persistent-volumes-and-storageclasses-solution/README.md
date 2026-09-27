# PersistentVolumes, Claims and StorageClasses (Solution)

## Overview

The claim uses `local-retain`, the volume is mounted where the app writes (`/data`), and the StorageClass uses `reclaimPolicy: Retain`. Three tests verify binding, persistence across a Pod restart and survival of the volume after the claim is deleted, on a real k3s cluster.

## Key Concepts

- **A claim with an unknown StorageClass never binds.** No provisioner answers it, and the Pod stays `Pending` with `pod has unbound immediate PersistentVolumeClaims`. `storageClassName` is immutable: a claim created with the wrong class must be recreated.
- **`WaitForFirstConsumer` is normal Pending.** The volume is only provisioned when a Pod that uses the claim is scheduled, so it lands on that Pod's node.
- **A volume only persists what is written into it.** With the mount at `/var/lib/notes` and the app writing to `/data`, every restart started from an empty file: the Pod was running, the claim was bound, and nothing was persisted. Always test persistence by restarting the Pod.
- **The reclaim policy decides what deleting the claim does.** `Delete` (the default for dynamic provisioning) removes the PV and the data on the node. `Retain` leaves the PV `Released` with its data, to be recovered by hand (TODO-03: clear `spec.claimRef`).

## Running the Solution

```bash
mvn -pl kubernetes-concepts/persistent-volumes-and-storageclasses-solution test
```
