#!/bin/sh
# Merges the kubeconfigs of two clusters into one file, /lab/merged.yaml, with one context per cluster:
#   staging  ->  the cluster in /lab/staging.yaml
#   prod     ->  the cluster in /lab/prod.yaml
# Both files come straight from their clusters (k3s), so every entry in both is called "default".
set -eu

# TODO-00: kubectl merges kubeconfig files by name, and the first file wins. Give the clusters, users
#          and contexts of each file their own names before merging, and keep the certificates embedded.
# TODO-01: Whoever uses this file and forgets --context must land on staging, never on prod.
KUBECONFIG=/lab/staging.yaml:/lab/prod.yaml kubectl config view --flatten > /lab/merged.yaml
