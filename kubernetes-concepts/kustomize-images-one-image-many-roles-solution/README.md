# Kustomize Images: One Image, Many Roles (Solution)

## Overview

The prod overlay has two `images` entries, one for `busybox` and one for `docker.io/library/busybox`, both with `newName: registry.lab/mirror/busybox` and the release `digest`. (Writing the image the same way everywhere in the base, so that one entry is enough, is just as good.) Three tests check the rendered images and arguments.

## Key Concepts

- **`images` matches names as written.** `busybox` and `docker.io/library/busybox` pull the same image, but they are two different names for Kustomize, so an entry for one silently skips the other. A tag in the entry's `name` is ignored for matching. Write image references one way across a repository.
- **One entry, every role.** Deployments, Jobs, CronJobs and init containers all get the new reference, so the roles can never run different releases by accident; only their `command`/`args` differ.
- **`newName`, `newTag`, `digest`.** `newName` moves the image to another registry or repository (a mirror, a private registry), `newTag` changes the tag, and `digest` pins the exact content (`@sha256:...`) and replaces the tag.
- **Digests are immutable.** A tag can be re-pushed (see the image pull policy lab); a digest always means the same bytes, which makes rollbacks exact and tells you afterwards what really ran.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kustomize-images-one-image-many-roles-solution test
```
