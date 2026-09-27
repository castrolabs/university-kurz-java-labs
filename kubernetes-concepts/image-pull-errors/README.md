# ImagePullBackOff and ErrImagePull

## Goal

Pull an image from a private registry whose certificate is signed by a private CA, and read pull errors precisely enough to tell a trust problem from a missing tag. The tests run a real registry (`registry:3` with TLS) next to a real Kubernetes node (k3s), both started by Testcontainers.

## Prerequisites

- Docker running locally (the tests start `registry:3`, `crane` and `rancher/k3s` containers)
- The article "ImagePullBackOff and ErrImagePull"

## Task

The test starts a registry reachable from the node as `registry.lab`, serving HTTPS with a certificate signed by a private CA, and copies `busybox:1.37` into it as `registry.lab/tools/busybox:1.37`. It then starts the k3s node with:

- the CA certificate at `/etc/rancher/k3s/lab-ca.crt`;
- your `src/main/resources/registries.yaml` at `/etc/rancher/k3s/registries.yaml`;

and applies your `src/main/resources/k8s/web.yaml`. It checks that `web` runs, and that `registries.yaml` trusts the CA instead of disabling verification.

## Instructions

Run the tests first. The failure message shows the node's latest pull error and the tags that exist in the registry.

- TODO-00: Make the node trust `registry.lab` in `registries.yaml`.
- TODO-01: Run again. The error changed: fix the image reference in `web.yaml`.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/image-pull-errors test
```

## Bonus (Optional)

- TODO-02 (optional): The error message is the same `ErrImagePull` / `ImagePullBackOff` for both problems. Which part of the event message tells you, in one glance, whether to look at the node's configuration or at the manifest?
