# Logical Database Backups with kubectl exec and CronJobs

## Goal

Turn a backup CronJob that "succeeds" without data into one that fails loudly when the dump fails, keeps the password out of the manifest and never runs twice at once. The tests use a real MariaDB on a real Kubernetes cluster (k3s, started by Testcontainers).

## Prerequisites

- Docker running locally (the tests start a `rancher/k3s` container and pull `mariadb` and `busybox` inside it)
- The articles "Jobs, CronJobs and restartPolicy" and "Logical Database Backups with kubectl exec and CronJobs"

## Task

The test deploys MariaDB (`src/test/resources/k8s/db.yaml`, given) with a table of three orders, applies your `src/main/resources/k8s/backup.yaml`, and runs the CronJob twice with `kubectl create job --from=cronjob/db-backup`:

1. with the database up: the Job must complete and the dump must contain the data;
2. with the database scaled to zero: the Job must **fail**.

You only edit `backup.yaml`.

## Instructions

Run the tests first. The second run is the interesting one: `mariadb-dump` cannot connect, yet the Job ends `Complete`, and the dump file passes `gzip -t`.

- TODO-00: Make a failing command anywhere in the pipeline fail the Job.
- TODO-01: Move the password to the Secret `db-root` (variable `MYSQL_PWD`) and remove it from the command.
- TODO-02: Forbid overlapping runs.

Run the tests until they all pass.

## Running the Lab

```bash
mvn -pl kubernetes-concepts/logical-database-backups-with-kubectl-exec-and-cronjobs test
```

The first run pulls the MariaDB image (about 100 MB).

## Bonus (Optional)

- TODO-03 (optional): Replace `gzip -t` with a check of the dump's content (the last line of a complete `mariadb-dump` output is `-- Dump completed on <date>`). Why is `gzip -t` alone not a backup check?
