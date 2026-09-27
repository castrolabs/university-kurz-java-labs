#!/bin/sh
# The pipeline's deploy step. Usage: IMAGE=<image> deploy.sh   (KUBECONFIG points at the CI credentials)
# The job must fail when the release does not come up, and leave the previous release running.
set -eu

kubectl apply -f /lab/k8s/app.yaml
kubectl -n shop set image deploy/web web="$IMAGE"

# apply/set only store the desired state. rollout status turns "the Pods came up" into an exit code;
# the timeout bounds how long a stuck release can hold the pipeline.
if ! kubectl -n shop rollout status deploy/web --timeout=60s; then
  echo "release $IMAGE did not become available, rolling back" >&2
  kubectl -n shop rollout undo deploy/web
  kubectl -n shop rollout status deploy/web --timeout=60s
  exit 1
fi
