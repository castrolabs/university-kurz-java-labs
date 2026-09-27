# Volume Backups, Velero and Restore Drills (Solution)

## Overview

The Backup sets `defaultVolumesToFsBackup: true`, and the Restore maps `shop` to `shop-drill` with `namespaceMapping`. Three tests check the backup's volume data, the restored database, and production.

## Key Concepts

- **A backup of objects is not a backup of data.** Without volume backups Velero stores only definitions: a restore recreates the PersistentVolumeClaim and the storage provisioner creates a new, empty volume. The backup is `Completed` either way, which is why this goes unnoticed until the day it is needed.
- **Two ways to get volume data.** File system backup (`defaultVolumesToFsBackup`, or per Pod with the `backup.velero.io/backup-volumes` annotation) copies files with the node agent and kopia to the object store: it works with most volumes, including `local` PVs like k3s's local-path, but not `hostPath` ones. CSI snapshots are faster and consistent at the storage level, when the storage supports them.
- **Drill next to production.** `namespaceMapping` restores under another name, so the drill proves the backup without risking production. Restoring into the original namespace skips objects that already exist, which here would have "succeeded" without restoring anything.
- **A drill measures RPO and RTO.** The age of the last good backup is the data you would lose; the time the restore took is your downtime. Only a restore shows both, and whether the application actually starts with the restored data.
- **Object store credentials matter.** The store must accept Velero's signed requests: here SeaweedFS needed an identity for the same key pair, otherwise the object backup worked and the kopia repository failed.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/volume-backups-velero-and-restore-drills-solution test
```
