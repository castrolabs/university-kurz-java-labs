# Rollouts: Status, Restart, History and Rollback (Solution)

## Overview

`rollback.sh` runs `kubectl rollout undo --to-revision=1`, waits with `rollout status`, then runs `kubectl rollout restart` and waits again. Three tests check the script, the content every Pod serves, and the revision numbers.

## Key Concepts

- **`undo` without a revision goes back one step**, which here is the buggy v2. Read `kubectl rollout history` (and `--revision=N` for the details) and pick the revision explicitly.
- **A rollback is a new revision.** Revision 1's template moves to the end as revision 4, so it disappears from its old place in the history. The restart adds revision 5.
- **Record why each revision exists.** `kubernetes.io/change-cause` is what makes the history readable. Set it right after the change: the Deployment controller copies it onto the ReplicaSet it currently runs, so annotating before the change relabels the previous revision.
- **Environment variables from a ConfigMap are read at container start.** `kubectl rollout restart` only adds a `restartedAt` annotation to the Pod template, which triggers a normal rolling update, with the same strategy and probes as any release.
- **Always wait.** `rollout undo` and `rollout restart` return immediately; `rollout status` is what tells a script that the change actually worked.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/rollouts-restart-history-and-rollback-solution test
```
