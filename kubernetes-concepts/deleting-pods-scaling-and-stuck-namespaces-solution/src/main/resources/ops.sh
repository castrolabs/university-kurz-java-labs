#!/bin/sh
# Runs inside the cluster node with kubectl configured as cluster admin.
set -eu

# Deleted Pods are replaced by their ReplicaSet. Scale the owner instead; the Deployment stays.
kubectl -n shop scale deploy/web --replicas=0

# The namespace condition names what is left and which finalizers hold it:
kubectl get namespace old-team -o jsonpath='{.status.conditions[?(@.status=="True")].message}'; echo

# Remove the finalizer from the stuck object, not from the namespace. The namespace controller then
# finishes its own cleanup and removes the namespace by itself.
for kind in $(kubectl api-resources --verbs=list,patch --namespaced -o name); do
  for obj in $(kubectl -n old-team get "$kind" -o name 2>/dev/null); do
    if [ -n "$(kubectl -n old-team get "$obj" -o jsonpath='{.metadata.finalizers}')" ]; then
      echo "removing finalizers from $obj"
      kubectl -n old-team patch "$obj" --type=merge -p '{"metadata":{"finalizers":null}}'
    fi
  done
done
