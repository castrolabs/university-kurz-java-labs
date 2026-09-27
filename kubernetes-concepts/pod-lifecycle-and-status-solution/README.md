# Pod Lifecycle and Status (Solution)

## Overview

`classify.sh` reads, with one `-o jsonpath` call, each Pod's phase, the reason of its `PodScheduled` condition, the status of its `Ready` condition, its container's waiting reason and its restart count, and decides from those. One test runs it four times and compares every run with the expected categories.

## Key Concepts

- **The `STATUS` column is computed for display.** It shows the most interesting reason it finds, so the same Pod shows different words over time, and different Pods show the same word:
  - a crash-looping container shows `Error` or `CrashLoopBackOff` (or even `Running`) depending on the moment, and `Error` looks exactly like a finished Pod that failed;
  - a running container that is not ready still shows `Running`; only the `READY` column (`0/1`) tells.
- **Phase is coarse on purpose.** `Pending`, `Running`, `Succeeded`, `Failed` (and `Unknown`). A crash-looping Pod is `Running`, and an unschedulable one and one pulling its image are both `Pending`.
- **Conditions and container states hold the detail.** `PodScheduled` with reason `Unschedulable`, `Ready` `True`/`False`, `state.waiting.reason` (`ErrImagePull`, `ImagePullBackOff`, `CrashLoopBackOff`, `CreateContainerConfigError`), `lastState.terminated` (exit code, `OOMKilled`), and `restartCount`.
- **Use `|` or another separator with jsonpath.** Empty fields otherwise shift every column after them when the shell splits the line.
- **Crash loops are Ready for an instant** at every restart. Any single snapshot can be misleading; decisions that matter (alerts, scripts that fail a deployment) should look at the restart count or a condition held over time.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/pod-lifecycle-and-status-solution test
```
