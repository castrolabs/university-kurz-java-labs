# Idempotent Cluster Bootstrap

## Goal

Turn an imperative bootstrap script that only works once into one that can run on every change: it creates what is missing, updates what changed, and succeeds when nothing changed. The tests run it three times on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container)
- The article "Idempotent Cluster Bootstrap"

## Task

Your `src/main/resources/bootstrap.sh` prepares the namespace `team-a`: a label, a ConfigMap with `LOG_LEVEL`, a Secret with `DB_PASSWORD`, a ServiceAccount `deployer` and a RoleBinding that gives it `edit`. The test runs it:

1. on an empty cluster, with `LOG_LEVEL=info` and a first password;
2. again, with `LOG_LEVEL=debug` and a rotated password;
3. again, with the same values as run 2.

It checks that every run exits with 0, that the cluster ends up with `debug` and the rotated password, that the label and the permissions are in place, and that no password (plain or base64) ever appears in the script's output.

You only edit `bootstrap.sh`.

## Instructions

Run the tests first. The second run fails at its first line.

- TODO-00: Make every command idempotent.
- TODO-01: Keep the password out of the output.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/idempotent-cluster-bootstrap test
```

## Bonus (Optional)

- TODO-02 (optional): Change the RoleBinding to `--clusterrole=view` and run your script against a cluster where it already exists with `edit`. What happens, and why can `kubectl apply` not fix it?
