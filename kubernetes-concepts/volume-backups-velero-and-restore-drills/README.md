# Volume Backups, Velero and Restore Drills

## Goal

Back up a namespace with Velero including the data on its volumes, and prove it with a restore drill that does not touch production. The tests run Velero (with its node agent), an S3-compatible object store and a small database on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which installs the Velero Helm chart and pulls images from `mirror.gcr.io`)
- The article "Volume Backups, Velero and Restore Drills"
- Patience: one run takes a few minutes

## Task

The test installs SeaweedFS as the backup store and Velero with the AWS plugin and the node agent (`src/test/resources/k8s/`, given), deploys a database in `shop` whose data lives on a PersistentVolume, and writes a unique order into it. Then it applies your `src/main/resources/k8s/backup.yaml`, waits for the backup, applies your `restore.yaml`, and checks that:

- the backup is `Completed` and contains the data of the `data` volume;
- the drill restored a working database into the namespace `shop-drill`, with that order in it;
- the production database in `shop` was not touched.

You edit `backup.yaml` and `restore.yaml` (Velero objects: the same things `velero backup create` and `velero restore create` make).

## Instructions

Run the tests first.

- TODO-00: Include the volume data in the backup.
- TODO-01: Restore into `shop-drill` instead of `shop`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/volume-backups-velero-and-restore-drills test
```

## Bonus (Optional)

- TODO-02 (optional): Turn the backup into a `Schedule` that runs every night and keeps backups for 14 days. Which fields do you need, and how would you notice that it stopped producing backups?
