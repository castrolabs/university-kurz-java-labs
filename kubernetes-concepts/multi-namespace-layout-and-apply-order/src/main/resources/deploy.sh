#!/bin/sh
# Deploys everything in /lab/deploy onto an empty cluster. Runs on the node, as cluster admin.
# It must succeed on the first run, without errors, and again on every later run.
set -eu

# TODO-00: kubectl applies the files of a directory in alphabetical order. Organize /lab/deploy with
#          Kustomize (kustomization.yaml files) so that each object is created after what it needs.
# TODO-01: A custom resource cannot be created in the same step as its CustomResourceDefinition.
#          Apply the CRD first and wait until the API server serves the new kind.
# TODO-02: Return only when the web app is actually available.
kubectl apply -f /lab/deploy/
