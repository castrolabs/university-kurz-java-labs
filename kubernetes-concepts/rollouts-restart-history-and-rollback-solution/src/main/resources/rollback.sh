#!/bin/sh
# Runs inside the cluster node with kubectl configured as cluster admin.
set -eu

kubectl rollout history deploy/web

# A plain "undo" goes back one revision, to the buggy v2. Pick the revision explicitly.
# (Check what it contains first: kubectl rollout history deploy/web --revision=1)
kubectl rollout undo deploy/web --to-revision=1
kubectl rollout status deploy/web --timeout=90s

# Environment variables from a ConfigMap are read once, at container start. A restart replaces every
# Pod through a normal rolling update (it only changes an annotation in the Pod template).
kubectl rollout restart deploy/web
kubectl rollout status deploy/web --timeout=90s

kubectl rollout history deploy/web
