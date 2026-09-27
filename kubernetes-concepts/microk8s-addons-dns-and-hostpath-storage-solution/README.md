# MicroK8s Add-ons: DNS and Hostpath Storage (Solution)

## Overview

`addons.sh` disables and re-enables `dns` with the forwarders, enables `hostpath-storage`, and applies a StorageClass `db-retain` with `reclaimPolicy: Retain`, `pvDir: /mnt/db-volumes` and no default annotation. Four tests check the Corefile, name resolution, the default class and the retained data.

## Key Concepts

- **Enabling an enabled add-on does nothing.** `dns` is on in a fresh install; `microk8s enable dns:1.1.1.1,8.8.8.8` only prints `Addon core/dns is already enabled` and CoreDNS keeps `forward . /etc/resolv.conf`. Disable it first (or edit the `coredns` ConfigMap).
- **Explicit forwarders.** Forwarding to the node's `resolv.conf` works until the node's DNS changes (DHCP, a VPN, split-horizon DNS); pinned forwarders make Pods independent of that.
- **One default StorageClass.** Two classes annotated as default make it ambiguous which one a claim without `storageClassName` gets. Special-purpose classes should be requested by name.
- **`Retain` and `pvDir`.** With `Delete`, removing the claim removes the directory and the data within seconds; with `Retain` the PV becomes `Released` and the directory stays until someone decides. `pvDir` puts the directories on a disk of your choice instead of `/var/snap/microk8s/common/default-storage`.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/microk8s-addons-dns-and-hostpath-storage-solution test
```
