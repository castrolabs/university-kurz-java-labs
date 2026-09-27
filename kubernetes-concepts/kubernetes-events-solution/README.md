# Kubernetes Events (Solution)

## Overview

`latest-warning.sh` uses `kubectl events --types=Warning --no-headers | tail -1`. Three tests check the exit code, that there is one line, and that it is the scheduler's warning about `pod/report`.

## Key Concepts

- **Two event APIs, one list.** Components that still write `core/v1` events (the Job controller, the kubelet) fill `lastTimestamp`. Components that use `events.k8s.io/v1` (the scheduler, among others) fill `eventTime` and leave `lastTimestamp` empty. `kubectl get events` shows both kinds together.
- **`--sort-by=.lastTimestamp` puts empty timestamps first,** so the scheduler's newest `FailedScheduling` looks like the oldest event, and `tail -1` returns something else. Scripts that search for "the latest problem" this way miss scheduling failures, which are usually the ones that matter.
- **`kubectl events` knows both formats,** sorts oldest first, and filters with `--types` and `--for pod/<name>`. Use it for scripts and for reading.
- **Events are short-lived.** They expire (after one hour by default), so a script that explains a failure has to run close to it, or the events have to be shipped somewhere else.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/kubernetes-events-solution test
```
