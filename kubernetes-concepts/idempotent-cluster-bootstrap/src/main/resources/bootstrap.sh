#!/bin/sh
# Prepares the cluster for team-a. Runs on the node, as cluster admin, with two variables:
#   LOG_LEVEL    log level for the team's apps
#   DB_PASSWORD  database password for the team's apps
# It is run on every change (new values included), so it must succeed every time and leave the
# cluster in the state it describes.
set -eu

# TODO-00: Every line works on an empty cluster and fails on the second run. Make each one idempotent:
#          create what is missing, update what changed, succeed when nothing changed.
# TODO-01: Make sure the password never ends up in the script's output (CI logs keep it forever).
kubectl create namespace team-a
kubectl label namespace team-a env=prod
kubectl -n team-a create configmap app-config --from-literal=LOG_LEVEL="$LOG_LEVEL"
kubectl -n team-a create secret generic db-creds --from-literal=password="$DB_PASSWORD"
kubectl -n team-a create serviceaccount deployer
kubectl -n team-a create rolebinding deployer-edit --clusterrole=edit --serviceaccount=team-a:deployer
