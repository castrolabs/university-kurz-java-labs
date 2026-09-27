# Deployment Strategies: RollingUpdate vs Recreate (Solution)

## Overview

`web` uses `maxSurge: 1, maxUnavailable: 0`, and `singleton` uses `strategy: Recreate`. Four tests verify both rollouts on a real k3s cluster.

## Key Concepts

- **`maxUnavailable: 0` is what protects capacity.** The controller may only remove an old Pod once a new one is available. A new Pod stuck in `ImagePullBackOff` never becomes available, so both old Pods keep serving and the rollout simply stalls. With `maxSurge: 0, maxUnavailable: 1`, the controller removed an old Pod first, leaving 1 of 2.
- **Readiness makes this work.** "Available" means Ready for `minReadySeconds`; the readiness probe on `web` is what lets a broken new version fail visibly instead of taking traffic.
- **RollingUpdate always overlaps old and new.** With one replica and the default 25% surge (rounded up to 1), the new Pod starts while the old one still runs. For a scheduler, a consumer that assumes it is alone or a database on a volume, that overlap is the bug. `Recreate` stops everything first, at the cost of a short gap.
- **Rounding:** `maxSurge` percentages round up, `maxUnavailable` percentages round down (TODO-02: with 4 replicas that is 1 and 1).

## Running the Solution

```bash
mvn -pl kubernetes-concepts/deployment-strategies-rollingupdate-vs-recreate-solution test
```
