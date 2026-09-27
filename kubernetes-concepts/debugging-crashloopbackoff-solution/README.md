# Debugging CrashLoopBackOff (Solution)

## Overview

`api` maps the ConfigMap key `database-url` to `DATABASE_URL` with `configMapKeyRef`, `worker` splits its command into one array element per argument, and `cache` gets a 128Mi limit. Three tests check that every app is ready with 0 restarts.

## Key Concepts

- **The evidence tells the three apart.**
  - `api`: `reason: Error`, exit code 1, and logs (`fatal: DATABASE_URL is not set`). The app ran and gave up, so the answer is in its logs.
  - `worker`: `reason: RunContainerError`, exit code 128, **no logs**, and a message `exec: "sh -c while true; ...": executable file not found`. The process never started; the problem is in the Pod spec, not the app.
  - `cache`: `reason: OOMKilled`, exit code 137 (128 + SIGKILL), no logs from its last run. The kernel killed it; the app never saw an error.
- **`envFrom` copies keys as they are.** The key `database-url` becomes a variable called `database-url`, not `DATABASE_URL`. When the names differ, use `env` with `configMapKeyRef`.
- **`command` is an array of arguments, not a shell line.** `["sh -c while ..."]` asks for an executable with that whole name. Write `["sh", "-c", "..."]`.
- **A memory limit covers the whole process.** The cache needs its 64 MB plus the runtime around it. Raising the limit (and the request with it) fixes it; removing the limit only hides the problem until the node runs out of memory.
- **Logs of a crash loop:** `kubectl logs` shows the current container if it already exited, `kubectl logs --previous` the one before; a container waiting to be restarted has none.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/debugging-crashloopbackoff-solution test
```
