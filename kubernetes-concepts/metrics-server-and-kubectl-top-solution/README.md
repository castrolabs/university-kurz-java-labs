# metrics-server and kubectl top (Solution)

## Overview

The worker requests `100m` CPU with a `200m` limit, and the Deployment has no `replicas` field. Three tests check the HPA's metric, the limit, and that a re-apply keeps the scaled replica count.

## Key Concepts

- **Utilization is relative to the request.** The HPA divides usage by `resources.requests.cpu`. Without a request it cannot compute anything: `TARGETS` shows `<unknown>` and the condition `ScalingActive=False` says `missing request for cpu in container worker`. Every container of the Pod needs one.
- **metrics-server is the source.** It collects usage from every kubelet about every 15 seconds and serves the `metrics.k8s.io` API that both `kubectl top` and the HPA read. No metrics-server, no `kubectl top` and no resource-based autoscaling.
- **Throttled CPU caps the metric.** With a limit of `200m` and a request of `100m`, the busy worker can never show more than 200% utilization, however much work is waiting.
- **One owner per field.** With the HPA in place, `spec.replicas` belongs to it. Leaving `replicas: 1` in the manifest makes every `kubectl apply` scale back to 1 until the HPA corrects it again, which on a busy service is an outage window at every release. Remove the field (once it has been applied before, removing it also resets to 1 on that first apply) or use server-side apply with field managers.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/metrics-server-and-kubectl-top-solution test
```
