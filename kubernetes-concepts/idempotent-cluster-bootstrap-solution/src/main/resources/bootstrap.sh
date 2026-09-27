#!/bin/sh
# Prepares the cluster for team-a. Runs on the node, as cluster admin, with two variables:
#   LOG_LEVEL    log level for the team's apps
#   DB_PASSWORD  database password for the team's apps
# It is run on every change (new values included), so it must succeed every time and leave the
# cluster in the state it describes.
set -eu

# "create --dry-run=client -o yaml" only builds the object; "apply" creates it, updates it, or does
# nothing. The YAML goes through the pipe, never to the output: apply prints only the object name.
apply() { kubectl apply -f - ; }

kubectl create namespace team-a --dry-run=client -o yaml | apply
kubectl label namespace team-a env=prod --overwrite
kubectl -n team-a create configmap app-config --from-literal=LOG_LEVEL="$LOG_LEVEL" --dry-run=client -o yaml | apply
kubectl -n team-a create secret generic db-creds --from-literal=password="$DB_PASSWORD" --dry-run=client -o yaml | apply
kubectl -n team-a create serviceaccount deployer --dry-run=client -o yaml | apply
kubectl -n team-a create rolebinding deployer-edit --clusterrole=edit --serviceaccount=team-a:deployer \
  --dry-run=client -o yaml | apply
