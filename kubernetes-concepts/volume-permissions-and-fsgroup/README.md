# Volume Permissions and fsGroup

## Goal

Run a process as a non-root user and still let it read a file from a volume that belongs to root, without loosening the file's permissions. The tests run against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Volume Permissions and fsGroup"

## Task

The test creates the Secret `app-tls` (key `tls.key`) and applies `src/main/resources/k8s/app.yaml`: a Pod that must run as non-root, mounts the Secret with `defaultMode: 0400` and logs `key loaded` once it can read the key. You only edit the Pod's `securityContext`.

## Instructions

Run the tests first and read the Pod state in the failure message.

- TODO-00: Make the container start under `runAsNonRoot: true` by running it as user 1000, group 1000.
- TODO-01: The key is now unreadable (`Permission denied`: the file is `root:root 0400`). Give the volume's files to group 1000 without making them world readable (`keyIsNotWorldReadable` rejects `defaultMode: 0444`).

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/volume-permissions-and-fsgroup test
```

## Bonus (Optional)

- TODO-02 (optional): Mount an `emptyDir` at `/data` as well and compare `ls -ldn /data` with and without `fsGroup`. Then read the article's section on `hostPath` volumes: why would the same manifest behave differently on MicroK8s hostpath storage?
