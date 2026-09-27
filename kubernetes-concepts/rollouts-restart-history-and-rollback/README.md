# Rollouts: Status, Restart, History and Rollback

## Goal

Recover from a bad release by rolling back to a specific revision, then restart the Pods to pick up a changed ConfigMap. You write a small operations script; the tests run it against a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `busybox` inside it)
- The article "Rollouts: Status, Restart, History and Rollback"

## Task

The test deploys `web` (`src/test/resources/k8s/web.yaml`, given) and releases three revisions, each with a `kubernetes.io/change-cause`:

1. `v1`: works.
2. `v2`: rolls out fine but has a bug.
3. `v3`: its image does not exist, so the rollout is stuck (the v2 Pods keep serving, thanks to `maxUnavailable: 0`).

Then it changes the `GREETING` in the ConfigMap `web-config` to `bonjour` and runs your `src/main/resources/rollback.sh` inside the cluster node. It checks that:

- the script exits with 0;
- both Pods serve `v1 bonjour`;
- the Deployment is at revision 5 and revision 1 is gone from the history.

You only edit `rollback.sh`.

## Instructions

Run the tests first. The failure message includes the rollout history.

- TODO-00: Roll back to the last revision that worked, and wait for it.
- TODO-01: Make the Pods read the new ConfigMap value without editing the Deployment, and wait for it.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/rollouts-restart-history-and-rollback test
```

## Bonus (Optional)

- TODO-02 (optional): Why did revision 1 disappear from the history, and what change-cause does revision 5 show? What would `kubectl rollout undo` (without `--to-revision`) do right after your script?
