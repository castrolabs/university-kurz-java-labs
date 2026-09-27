# Secrets as Environment Variables vs Mounted Files (Solution)

## Overview

`app.yaml` drops the environment variable and mounts the whole Secret as a directory (with `defaultMode: 0400`, the bonus). Four tests verify it against a real k3s cluster, including an actual rotation.

## Key Concepts

- **Environment variables are resolved once, at container start.** Kubernetes cannot change a running process's environment, so after a rotation the old value stays until the container restarts. It is also readable in `/proc/<pid>/environ`, inherited by every child process and printed by many crash reporters.
- **Directory mounts are refreshed by the kubelet.** It writes the new content into a fresh timestamped directory and swaps the `..data` symlink, so the update is atomic. The delay is the kubelet sync period plus its Secret cache: about a minute, not instant (the test prints the measured value).
- **`subPath` mounts are a snapshot.** They never receive updates, which makes them a silent trap for anything that rotates.
- **The file only helps if the app re-reads it.** Reading it per connection (or on an authentication error) is enough; caching it forever at startup brings back the env var problem.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/secrets-as-env-vars-vs-mounted-files-solution test
```
