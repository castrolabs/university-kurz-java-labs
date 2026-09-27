# kubectl exec, debug and port-forward (Solution)

## Overview

`inspect.sh` adds an ephemeral `busybox` container with `kubectl debug --target=nginx`, reads the file through `/proc/1/root`, waits for the container to finish and prints its logs. Then it opens `kubectl port-forward deploy/web 18080:8080` in the background, waits until it answers, reads the header, and kills the tunnel on exit. Three tests check the exit code, both lines, and that no tunnel is left.

## Key Concepts

- **`kubectl exec` runs a program from the image.** Minimal images have no shell and no `cat`, so `exec: "cat": executable file not found` is expected, not a bug.
- **`kubectl debug --target` brings the tools.** An ephemeral container with any image joins the Pod and shares the target container's process namespace. The target's PID 1 is visible, and `/proc/1/root` is its root filesystem, exactly as the running process sees it, including mounted volumes.
- **Permissions still apply.** nginx runs as UID 65532; the debug container runs as root and can read `/proc/1/root` because kubectl's default debugging profile keeps `SYS_PTRACE` (verified: its `CapEff` includes it).
- **Ephemeral containers stay.** They cannot be removed from the Pod; they disappear only with the Pod. Scripts need a new name for every run, and `--attach` can miss the output of short commands, so read `kubectl logs -c <name>` after the container finishes.
- **`kubectl port-forward` is a tunnel through the API server.** It needs no Service, DNS or route to the Pod network, only permission for `pods/portforward`. It runs until killed, so a script must kill it (`trap ... EXIT`).

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubectl-exec-and-port-forward-solution test
```
