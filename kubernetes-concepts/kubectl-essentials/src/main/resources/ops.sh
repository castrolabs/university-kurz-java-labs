#!/bin/sh
# Three everyday tasks in the namespace shop. Runs on the node, with kubectl configured as cluster admin.
set -eu

# TODO-00: Deploy api.yaml into shop. Where does this command put it?
kubectl apply -f /lab/k8s/api.yaml

# TODO-01: The canary is over: remove the canary Pods, and only them.
kubectl -n shop delete pods --all

# TODO-02: Print one line per Deployment in shop, "<name> <image>", sorted by name, and nothing else.
kubectl -n shop get deploy -o wide | awk '{print $1, $7}'
