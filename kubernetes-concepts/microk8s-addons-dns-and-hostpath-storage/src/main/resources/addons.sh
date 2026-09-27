#!/bin/bash
# Configures the add-ons of a fresh MicroK8s 1.35 node. Runs as root, after "microk8s status --wait-ready".
set -euo pipefail

# TODO-00: Pods must resolve external names through 1.1.1.1 and 8.8.8.8, not through whatever the node's
#          resolv.conf says today. The dns add-on is already enabled on a fresh install: check what this
#          line really changes.
microk8s enable dns:1.1.1.1,8.8.8.8

microk8s enable hostpath-storage

# TODO-01: See storageclass.yaml.
microk8s kubectl apply -f /home/ubuntu/lab/storageclass.yaml
