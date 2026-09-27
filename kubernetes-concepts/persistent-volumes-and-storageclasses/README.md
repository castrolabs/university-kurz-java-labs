# PersistentVolumes, Claims and StorageClasses

## Goal

Give a small stateful service storage that really persists, on a real Kubernetes cluster (k3s, started by Testcontainers): a claim that binds, data that survives a Pod restart, and a volume that survives an accidental `kubectl delete pvc`.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "PersistentVolumes, Claims and StorageClasses"

## Task

`src/main/resources/k8s/storage.yaml` defines a StorageClass, a PersistentVolumeClaim and a Deployment `notes` whose container appends one line to `/data/notes.txt` every time it starts. The test:

1. applies the file and waits for the Deployment;
2. deletes the Pod and expects the new Pod to find **two** lines in `/data/notes.txt`;
3. scales the Deployment to zero, deletes the claim and expects the PersistentVolume to still exist (`Released`).

You only edit `storage.yaml`.

## Instructions

Run the tests first. The failure shows the scheduler's warning for the Pending Pod.

- TODO-00: Make the claim use the StorageClass defined in the file.
- TODO-01: Make the app's writes land on the volume.
- TODO-02: Keep the volume and its data when the claim is deleted.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/persistent-volumes-and-storageclasses test
```

A run takes about two minutes.

## Bonus (Optional)

- TODO-03 (optional): After the claim is deleted, the PV is `Released` but a new claim with the same name does not bind to it. Find out why (`kubectl get pv -o yaml`, look at `spec.claimRef`) and make a new claim reuse the old data.
