# Requests, Limits, QoS Classes and OOMKilled (Solution)

## Overview

`pods.yaml` gives `worker` a 256 Mi request and limit, makes `db` Guaranteed by adding a CPU limit equal to its request, and brings `batch` down to a request the node can satisfy. Five tests verify the Pods on a real k3s cluster.

## Key Concepts

- **The memory limit is enforced by the kernel.** A container that allocates more than its limit is killed by the OOM killer: `lastState.terminated.reason: OOMKilled`, `exitCode: 137` (128 + SIGKILL), then `CrashLoopBackOff`. The fix is a limit that fits the real working set, not removing the limit (`workerKeepsAMemoryLimit`).
- **QoS classes come from requests and limits.** `Guaranteed` needs CPU and memory requests equal to limits in every container. `db` had a memory limit only, which is `Burstable`. Guaranteed Pods are evicted last under node memory pressure.
- **The scheduler only compares requests with allocatable.** A 64 Gi request can never fit, and the Pod stays `Pending` with `Insufficient memory`. Nothing is broken: the request is simply larger than any node.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/resource-requests-limits-and-qos-solution test
```
