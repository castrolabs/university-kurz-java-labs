# Image Pull Secrets and Pull Policy (Solution)

## Overview

The Secret `registry-creds` lives in `team-a`, the Pod template references it in `imagePullSecrets`, and the container uses `imagePullPolicy: Always`. Two tests check the first pull and the pull after a re-push.

## Key Concepts

- **The kubelet never guesses credentials.** Without `imagePullSecrets` (on the Pod or its ServiceAccount) the pull is anonymous, and the registry answers `no basic auth credentials`.
- **Pull secrets are namespaced.** A Pod can only reference Secrets in its own namespace. With the Secret in `default`, the kubelet warns `FailedToRetrieveImagePullSecret ... attempting to pull the image may not succeed` and pulls anonymously anyway: the second warning is the real clue, the pull error looks the same as before. Every namespace that pulls from the registry needs its own copy.
- **`IfNotPresent` is the default for any tag except `latest`.** When the image is already on the node, the kubelet does not contact the registry, so a tag re-pushed with new content keeps running the old image, silently, on that node only.
- **`Always` checks the manifest, not the layers.** Layers already on the node are reused, so it is cheap; but it needs the registry to be reachable whenever a container starts.
- **Better than both: immutable references.** Never re-push a tag (a new version gets a new tag), or deploy by digest (`image: registry.lab/team-a/web@sha256:...`), which is the only reference that cannot change underneath you.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/image-pull-secrets-and-pull-policy-solution test
```
