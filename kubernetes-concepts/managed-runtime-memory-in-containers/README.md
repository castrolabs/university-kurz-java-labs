# Managed Runtime Memory in Containers

## Goal

Size a JVM correctly inside a container memory limit: heap relative to the limit, the right garbage collector, and a process that exits when the heap runs out. The tests run a real Java app on a real Kubernetes cluster (k3s, started by Testcontainers) with Temurin 25.

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container, which pulls `eclipse-temurin:25-jre` from `mirror.gcr.io`)
- The article "Managed Runtime Memory in Containers"

## Task

`src/main/java/CacheWarmup.java` is the app (given): it prints its max heap, its garbage collectors and whether `ExitOnOutOfMemoryError` is on, then loads 200 MiB into an in-memory cache. The test copies the compiled class to the node and applies your `src/main/resources/k8s/cache.yaml`, which runs it with a 512Mi memory limit. It checks that:

- the limit is still 512Mi;
- the cache warms up without any restart;
- the max heap is 60 to 80% of the limit, set as a percentage (no `-Xmx`);
- the JVM uses G1;
- `ExitOnOutOfMemoryError` is on.

You only edit `cache.yaml`.

## Instructions

Run the tests first. The failure message shows the app's output: how much heap the JVM chose on its own, and which collector.

- TODO-00: Size the heap.
- TODO-01: Choose G1.
- TODO-02: Make a heap out-of-memory end the process.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/managed-runtime-memory-in-containers test
```

## Bonus (Optional)

- TODO-03 (optional): Set `MaxRAMPercentage=100` and make the app keep allocating after warm-up. What kills it first, the JVM or the kernel, and how does each case look in `kubectl get pod` and in the logs?
