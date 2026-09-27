# MicroK8s containerd and Private Registry Trust

## Goal

Make MicroK8s pull images from a registry whose certificate is signed by a private company CA, the way MicroK8s's containerd expects it. The tests run a real TLS registry inside a fresh MicroK8s 1.35 in an Ubuntu VM created with Multipass.

## Prerequisites

- [Multipass](https://canonical.com/multipass) installed (the tests are skipped without it); the VM is deleted at the end
- The article "MicroK8s containerd and Private Registry Trust"

## Task

The test installs MicroK8s 1.35, runs the company registry at `registry.lab:5443` (TLS, signed by the CA in `src/main/resources/company-root-ca.crt`) and copies busybox into it (`src/test/resources/`, given). Then it runs your `src/main/resources/trust-registry.sh` as root and starts a Pod with the image `registry.lab:5443/tools/busybox:1.37`. It checks that:

- the script exits with 0;
- the Pod starts;
- no registry configuration turns TLS verification off.

You only edit `trust-registry.sh`.

## Instructions

Run the tests first. The failure message shows the node's pull error.

- TODO-00: Find out whether trusting the CA on the host is enough for MicroK8s.
- TODO-01: Configure the registry in MicroK8s's containerd, with the right directory name.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/microk8s-containerd-and-private-registry-ca test
```

## Bonus (Optional)

- TODO-02 (optional): Pull the image with `sudo microk8s ctr images pull --hosts-dir /var/snap/microk8s/current/args/certs.d registry.lab:5443/tools/busybox:1.37`, without Kubernetes. Why is that a quicker way to debug registry trust?
