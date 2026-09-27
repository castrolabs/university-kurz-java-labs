# Logical Database Backups with kubectl exec and CronJobs (Solution)

## Overview

The CronJob runs its script with `bash -euo pipefail`, reads the password from the Secret into `MYSQL_PWD`, checks the dump's last line and uses `concurrencyPolicy: Forbid`. Five tests verify both runs against a real MariaDB on k3s.

## Key Concepts

- **`sh -e` does not see failures inside a pipeline.** The exit status of `mariadb-dump ... | gzip > file` is `gzip`'s, which succeeds, so the script continues and the Job completes. `set -o pipefail` makes the pipeline fail when any command in it fails. The image's `sh` (dash) does not support it; `bash` does.
- **A valid archive is not a valid backup.** An empty input compresses into a perfectly valid gzip file, so `gzip -t` passes on a failed dump. Check the content: `mariadb-dump` ends with `-- Dump completed on ...`, `pg_dump` with `-- PostgreSQL database dump complete`, and a periodic restore test beats both.
- **Passwords belong in Secrets and environment variables the client reads by itself** (`MYSQL_PWD`, `PGPASSWORD`), never in the manifest or on the command line, where `ps` shows them.
- **`concurrencyPolicy: Forbid`** skips a run while the previous one is still going, instead of two dumps competing for the same database and disk.
- **Alert on the result.** A failed Job is only useful if something notices it: `KubeJobFailed`, or an alert on `time() - kube_cronjob_status_last_successful_time`.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/logical-database-backups-with-kubectl-exec-and-cronjobs-solution test
```
