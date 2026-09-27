# Managed Runtime Memory in Containers (Solution)

## Overview

The container sets `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError`. Five tests check the limit, a clean warm-up, the heap size, the collector and the exit flag, all from the app's own output.

## Key Concepts

- **The JVM reads the container limit, and takes only 25% by default.** Under 512Mi the starter got a 123 MiB heap and died with `OutOfMemoryError: Java heap space`, while the container had plenty of memory left.
- **Relative beats absolute.** `MaxRAMPercentage` follows the limit if someone changes it; `-Xmx` silently stays behind. 60 to 75% is the usual range: the rest of the process (metaspace, code cache, thread stacks, native buffers) needs its share, and a heap too close to the limit turns into an `OOMKilled` (exit code 137) with no Java error at all.
- **Small containers get the Serial collector.** The JVM uses G1 only on a "server class" machine (at least 2 CPUs and about 1792 MiB). Under 512Mi it picked Serial (`Copy`, `MarkSweepCompact`), which pauses the whole app for every collection. Choose the collector explicitly.
- **`ExitOnOutOfMemoryError` makes failures visible.** Without it, an `OutOfMemoryError` kills one thread and may leave a half-working process that Kubernetes considers healthy. With it, the process exits and Kubernetes restarts it, and the restart count shows the problem.
- **`JAVA_TOOL_OPTIONS` is read by every JVM** in the container, including tools run with `kubectl exec`, and the JVM prints `Picked up JAVA_TOOL_OPTIONS: ...` so the settings show up in the logs.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/managed-runtime-memory-in-containers-solution test
```
