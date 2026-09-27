# Startup, Liveness and Readiness Probes (Solution)

## Overview

`slow.yaml` adds a startup probe (5 s x 24 = 120 s budget) in front of the unchanged liveness probe, and `web.yaml` adds a readiness probe on `/readyz`. Five tests verify both scenarios against a real k3s cluster.

## Key Concepts

- **Liveness answers "is it stuck", not "has it started".** With no startup probe, liveness starts checking immediately; three refused connections 10 s apart kill a container that only needed 40 s to start, and it loops forever.
- **A startup probe holds liveness and readiness back** until it succeeds once. That gives slow starts a generous budget while liveness stays tight for real hangs (`livenessProbeIsKeptTight` guards against the tempting "fix" of a huge `initialDelaySeconds`).
- **Without a readiness probe a Pod is Ready as soon as its container starts.** The rollout then replaces healthy Pods with broken ones and `kubectl rollout status` reports success. With the probe, the new Pod never becomes available, `maxUnavailable` (25% of 2, rounded down to 0) keeps both old Pods, the Service keeps routing to them, and the rollout visibly stalls until `progressDeadlineSeconds`.
- **The grace period matters when reading probe behaviour.** `sh` as PID 1 ignores SIGTERM, so a killed container keeps running for `terminationGracePeriodSeconds` (30 s by default) and may even finish starting in the meantime (bonus TODO-02).

## Running the Solution

```bash
mvn -pl kubernetes-concepts/startup-liveness-readiness-probes-solution test
```
