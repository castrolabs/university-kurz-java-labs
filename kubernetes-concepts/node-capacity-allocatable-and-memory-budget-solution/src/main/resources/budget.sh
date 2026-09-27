#!/bin/sh
# Prints one number: how many more replicas of a Pod requesting 256Mi of memory the scheduler can still
# place on this (single-node) cluster right now.
set -eu

# The scheduler compares requests with allocatable: free = allocatable - sum of the memory requests of
# every Pod on the node that has not finished. Actual usage plays no part.
node=$(kubectl get nodes -o jsonpath='{.items[0].metadata.name}')
allocatable=$(kubectl get node "$node" -o jsonpath='{.status.allocatable.memory}')
requests=$(kubectl get pods -A --field-selector="spec.nodeName=$node,status.phase!=Succeeded,status.phase!=Failed" \
  -o jsonpath='{range .items[*].spec.containers[*]}{.resources.requests.memory}{"\n"}{end}')

# Quantities come as 256Mi, 70Mi, 1Gi, 128974848 (bytes), 500M, ...
printf '%s\n%s\n' "$allocatable" "$requests" | awk '
  function bytes(q,   n, u) {
    n = q + 0; u = q; sub(/^[0-9.]+/, "", u)
    if (u == "Ki") return n * 1024;    if (u == "Mi") return n * 1048576;  if (u == "Gi") return n * 1073741824
    if (u == "k")  return n * 1000;    if (u == "M")  return n * 1000000;  if (u == "G")  return n * 1000000000
    return n
  }
  NR == 1 { free = bytes($1); next }
  NF      { free -= bytes($1) }
  END     { n = int(free / (256 * 1048576)); print (n < 0 ? 0 : n) }'
