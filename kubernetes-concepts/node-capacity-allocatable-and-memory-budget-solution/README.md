# Node Capacity, Allocatable and Memory Budget (Solution)

## Overview

`config.yaml` passes `system-reserved`, `kube-reserved` and an `eviction-hard` map with `memory.available<100Mi` to the kubelet. `budget.sh` subtracts the memory requests of every unfinished Pod on the node from `allocatable`, handling Kubernetes quantity suffixes, and divides by 256Mi. Two tests check the reservation and compare N with real scheduling.

## Key Concepts

- **Capacity vs allocatable.** Capacity is the node's memory; allocatable is what the scheduler may promise to Pods: capacity minus `system-reserved`, `kube-reserved` and the memory eviction threshold. Verified on this k3s: with no configuration, allocatable equals capacity, because k3s sets its own `eviction-hard` with disk thresholds only, and no reservations.
- **`eviction-hard` is a whole map.** Setting it replaces the defaults, so a memory threshold alone would drop the disk thresholds; repeat the ones you want to keep.
- **The scheduler counts requests, not usage.** Free = allocatable minus the sum of the requests of the Pods already on the node (every namespace, including `kube-system`). A Pod that uses more than it requests does not change that sum, which is why requests below real usage lead to memory pressure, eviction and OOM kills later.
- **Quantities have units.** `256Mi`, `1Gi`, `500M` and plain bytes all appear in real clusters; a script that assumes one unit silently gets the budget wrong.

## Running the Solution

```bash
mvn -pl kubernetes-concepts/node-capacity-allocatable-and-memory-budget-solution test
```
