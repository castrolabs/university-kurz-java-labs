#!/bin/sh
# The pipeline's deploy step. Usage: IMAGE=<image> deploy.sh   (KUBECONFIG points at the CI credentials)
# The job must fail when the release does not come up, and leave the previous release running.
set -eu

kubectl apply -f /lab/k8s/app.yaml
kubectl -n shop set image deploy/web web="$IMAGE"
# TODO-00: This returns as soon as the API server has stored the change, so the job is always green.
#          Wait for the rollout (with a time limit); if it fails, roll back and fail the job.
