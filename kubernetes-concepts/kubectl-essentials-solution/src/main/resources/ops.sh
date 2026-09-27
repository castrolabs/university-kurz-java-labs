#!/bin/sh
# Three everyday tasks in the namespace shop. Runs on the node, with kubectl configured as cluster admin.
set -eu

# Without -n (and without a namespace in the file) objects go to the context's namespace: default.
kubectl -n shop apply -f /lab/k8s/api.yaml

# A label selector deletes exactly what it matches; --all deletes every Pod, bare ones included.
kubectl -n shop delete pods -l track=canary

# Ask for the fields instead of cutting columns out of a table made for humans.
kubectl -n shop get deploy --sort-by=.metadata.name --no-headers \
  -o custom-columns=NAME:.metadata.name,IMAGE:.spec.template.spec.containers[0].image | awk '{print $1, $2}'
