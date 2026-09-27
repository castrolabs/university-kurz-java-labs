#!/bin/sh
# Merges the kubeconfigs of two clusters into one file, /lab/merged.yaml, with one context per cluster:
#   staging  ->  the cluster in /lab/staging.yaml
#   prod     ->  the cluster in /lab/prod.yaml
# Both files come straight from their clusters (k3s), so every entry in both is called "default".
set -eu

# kubectl can rename a context but not a cluster or a user, so rename every entry in a copy of each
# file. The names are whole YAML values ("name: default", "cluster: default", "user: default", ...),
# quoted or not depending on the tool that wrote the file.
for env in staging prod; do
  sed -E "s/^([[:space:]-]*(name|cluster|user|current-context)): \"?default\"?$/\1: $env/" "/lab/$env.yaml" > "/tmp/$env.yaml"
done

# Now nothing collides. --flatten embeds the certificate data, so the result stands alone.
KUBECONFIG=/tmp/staging.yaml:/tmp/prod.yaml kubectl config view --flatten > /lab/merged.yaml
rm -f /tmp/staging.yaml /tmp/prod.yaml

# The safe default: a command without --context goes to staging.
kubectl --kubeconfig /lab/merged.yaml config use-context staging
