# Kustomize Images: One Image, Many Roles

## Goal

Run one build of an application in three roles (API, worker, migration Job) and change the image of all of them in one place for production: another registry, pinned by digest. The tests render your overlay with the Kustomize built into `kubectl` (from a k3s container started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container for its `kubectl`)
- The article "Kustomize Images: One Image, Many Roles"

## Task

`src/main/resources/kustomize/base/app.yaml` runs the same image as a Deployment `api`, a Deployment `worker` and a Job `migrate`; only their arguments differ. Your `overlays/prod/kustomization.yaml` must make all three run exactly:

```
registry.lab/mirror/busybox@sha256:bdf57e528e45e4433820e045b29b4597825a1c9e38353532d90a01445013f82e
```

The tests render the overlay and check that the three roles run one single image, that it is that one, and that each role keeps its own arguments. You edit the prod overlay (and may tidy up the base).

## Instructions

Run the tests first. The failure message lists the images the rendered roles use.

- TODO-00: Find out why one role ignores the `images` entry, and fix it.
- TODO-01: Use the mirror and the digest.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/kustomize-images-one-image-many-roles test
```

## Bonus (Optional)

- TODO-02 (optional): In CI, `kustomize edit set image busybox=registry.lab/mirror/busybox@sha256:...` writes the same entry for you. Why does a digest make a rollback, and an audit of what ran in production, more reliable than a tag?
