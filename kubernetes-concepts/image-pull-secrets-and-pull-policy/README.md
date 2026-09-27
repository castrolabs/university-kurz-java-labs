# Image Pull Secrets and Pull Policy

## Goal

Pull from a registry that requires a login, and make restarts pick up an image re-pushed under the same tag. The tests run a real registry with authentication (`registry:3` with htpasswd and TLS) next to a real Kubernetes node (k3s), both started by Testcontainers.

## Prerequisites

- Docker running locally (the tests start `registry:3`, `crane` and `rancher/k3s` containers)
- The article "Image Pull Secrets and Pull Policy"

## Task

The test starts `registry.lab` (user `lab`, password `lab-password`; the node already trusts its CA) and pushes BusyBox 1.36 as `registry.lab/team-a/web:1.0`. Then it applies your `src/main/resources/k8s/app.yaml` and checks that:

1. `deploy/web` in `team-a` starts;
2. after the test pushes BusyBox 1.37 **to the same tag** and runs `kubectl rollout restart`, the new Pod runs BusyBox 1.37.

You only edit `app.yaml`.

## Instructions

Run the tests first. The failure message shows the Warning events in `team-a`.

- TODO-00: Make the kubelet use the credentials in the Secret `registry-creds`.
- TODO-01: If it still fails, one of the warnings says why. Fix it.
- TODO-02: Make a restart run the image the tag points to now, not the copy already on the node.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/image-pull-secrets-and-pull-policy test
```

## Bonus (Optional)

- TODO-03 (optional): Instead of adding `imagePullSecrets` to every Pod spec, attach the Secret to the `default` ServiceAccount of `team-a`. Which Pods get it, and when?
