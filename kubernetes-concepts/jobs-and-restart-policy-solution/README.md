# Jobs, CronJobs and restartPolicy (Solution)

## Overview

`jobs.yaml` sets `restartPolicy: Never` on `migrate`, `backoffLimit: 1` on `flaky-import`, `activeDeadlineSeconds: 20` on `report` and `ttlSecondsAfterFinished: 10` on `cleanup`. Four tests verify each outcome on a real k3s cluster.

## Key Concepts

- **A Job's Pod template accepts only `Never` or `OnFailure`.** `Always`, the Pod default, is rejected at admission (`valid values: "OnFailure", "Never"`). With `Never`, every retry is a new Pod whose logs you can still read; `OnFailure` restarts the container in place and keeps only the previous attempt's logs.
- **`backoffLimit` counts retries, not attempts.** `backoffLimit: 1` means 2 Pods, then `Failed` with reason `BackoffLimitExceeded`. The default of 6, with exponential back-off (10 s, 20 s, 40 s...), takes minutes to give up.
- **`activeDeadlineSeconds` is a wall clock for the whole Job.** When it expires, running Pods are killed and the Job fails with `DeadlineExceeded`, even if retries remain. It is the protection against a hung task.
- **`ttlSecondsAfterFinished` cleans up.** The TTL controller deletes the finished Job and cascades to its Pods. Pick a value long enough to investigate failures; CronJobs use history limits instead.
- **A Job's template is immutable.** Re-applying a changed Job fails with `field is immutable` (TODO-04), which is why pipelines delete old Jobs first, give them versioned names, or rely on the TTL.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/jobs-and-restart-policy-solution test
```
