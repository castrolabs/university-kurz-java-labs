# MicroK8s Add-ons: DNS and Hostpath Storage

## Goal

Configure two MicroK8s add-ons properly: CoreDNS forwarding to resolvers you chose, and a StorageClass for a database that keeps its data on a dedicated directory after the claim is deleted. The tests run on a fresh MicroK8s 1.35 in an Ubuntu VM created with Multipass.

## Prerequisites

- [Multipass](https://canonical.com/multipass) installed (the tests are skipped without it); the VM is deleted at the end
- The article "MicroK8s Add-ons: DNS and Hostpath Storage"

## Task

The test installs MicroK8s 1.35, copies your `src/main/resources/addons.sh` and `storageclass.yaml` into the VM, runs the script as root, and checks that:

- CoreDNS forwards to `1.1.1.1 8.8.8.8`, and a Pod resolves `example.com`;
- `microk8s-hostpath` is still the only default StorageClass;
- a database Pod using the class `db-retain` gets a volume under `/mnt/db-volumes`, and after its Pod and claim are deleted the PersistentVolume is `Released` and the data is still on disk.

## Instructions

Run the tests first (a run takes a few minutes).

- TODO-00: Make the DNS forwarders actually change.
- TODO-01: Fix `storageclass.yaml`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/microk8s-addons-dns-and-hostpath-storage test
```

## Bonus (Optional)

- TODO-02 (optional): Write 200 MiB into a claim of 50 Mi on `db-retain`. What happens, and what does that mean for a node's disk?
