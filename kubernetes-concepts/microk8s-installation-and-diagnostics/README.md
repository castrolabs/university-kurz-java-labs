# MicroK8s Installation and Diagnostics

## Goal

Write an install script for MicroK8s that installs a pinned Kubernetes version, returns only when the cluster works, lets the normal user run kubectl, and collects a diagnostics report. The tests run your script on a fresh Ubuntu 24.04 VM created with Multipass.

## Prerequisites

- [Multipass](https://canonical.com/multipass) installed (the tests are skipped without it); the VM uses 2 CPUs, 4 GB of memory and 12 GB of disk, and is deleted at the end
- The article "MicroK8s Installation and Diagnostics"

## Task

The test launches a VM, copies your `src/main/resources/install.sh` into it, runs it as root, and checks that:

- the script exits with 0;
- MicroK8s is Kubernetes 1.34 and tracks `1.34/stable` (production runs 1.34);
- the node is `Ready` at the moment the script returns;
- the user `ubuntu` can run `microk8s kubectl get nodes` without sudo;
- `/home/ubuntu/diagnostics.tar.gz` is a MicroK8s inspection report readable by `ubuntu`.

You only edit `install.sh`.

## Instructions

Run the tests first (a run takes a few minutes).

- TODO-00: Pin the version.
- TODO-01: Wait until the cluster is usable.
- TODO-02: Give `ubuntu` access without sudo.
- TODO-03: Collect the diagnostics report.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/microk8s-installation-and-diagnostics test
```

## Bonus (Optional)

- TODO-04 (optional): Open the report. Which file shows the flags kubelite was started with, and which one the state of every MicroK8s service?
