#!/bin/sh
# Deploys everything in /lab/deploy onto an empty cluster. Runs on the node, as cluster admin.
# It must succeed on the first run, without errors, and again on every later run.
set -eu

# 1. The CRD, then wait until the API server serves the new kind. Until then, "kind: Widget" is unknown.
kubectl apply -k /lab/deploy/crds
kubectl wait --for=condition=Established crd/widgets.lab.example.com --timeout=60s

# 2. Everything else in one apply; Kustomize puts the Namespace before the objects inside it.
kubectl apply -k /lab/deploy/app

# 3. apply only records the desired state. Wait for the result.
kubectl -n shop rollout status deploy/web --timeout=120s
