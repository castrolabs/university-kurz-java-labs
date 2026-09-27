# Secrets as Environment Variables vs Mounted Files

## Goal

Rotate a Secret without restarting the Pod and make the application actually see the new value. The test rotates the password for you and watches what the container sees, on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Secrets as Environment Variables vs Mounted Files"

## Task

The test creates the Secret `db-credentials` (`DB_PASSWORD=old-111`), applies `src/main/resources/k8s/app.yaml`, then rotates the Secret to `new-222` with `kubectl apply` (same name, no restart) and waits up to 150 seconds for `/etc/app/secrets/DB_PASSWORD` inside the Pod to change.

You only edit `app.yaml`.

## Instructions

Run the tests before changing anything. `naiveEnvVarAndSubPathKeepTheOldPassword` passes: long after the rotation, a Pod with an environment variable and a `subPath` mount still sees `old-111`.

- TODO-00: Remove the `DB_PASSWORD` environment variable. The app only reads the file.
- TODO-01: Mount the Secret as a directory at `/etc/app/secrets` instead of a single file with `subPath`.

Run the tests until they all pass. The passing run prints how long the kubelet took to refresh the file (usually 60 to 90 seconds: the kubelet sync period plus its cache).

## Running the Lab

```bash
mvn -pl kubernetes-concepts/secrets-as-env-vars-vs-mounted-files test
```

Expect about two to three minutes per run: the rotation is real and the kubelet only refreshes Secret volumes periodically.

## Bonus (Optional)

- TODO-02 (optional): Add `defaultMode: 0400` to the Secret volume and check it with `stat -c %a /etc/app/secrets/..data/DB_PASSWORD`. Why does `ls -la /etc/app/secrets` show `DB_PASSWORD` as a symlink?
