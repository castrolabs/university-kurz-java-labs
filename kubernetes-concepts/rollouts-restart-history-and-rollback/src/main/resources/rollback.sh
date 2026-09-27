#!/bin/sh
# Runs inside the cluster node with kubectl configured as cluster admin.
set -eu

# TODO-00: v3 never became ready and v2 has a bug. Roll deploy/web back to the last revision that
#          worked (the failure message shows the rollout history) and wait until the rollout is done.
kubectl rollout undo deploy/web

# TODO-01: The ConfigMap web-config was changed, but the Pods only read it at startup. Make them pick it
#          up without editing the Deployment, and wait until they have.
