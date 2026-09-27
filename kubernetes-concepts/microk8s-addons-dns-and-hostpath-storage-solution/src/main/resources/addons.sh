#!/bin/bash
# Configures the add-ons of a fresh MicroK8s 1.35 node. Runs as root, after "microk8s status --wait-ready".
set -euo pipefail

# dns is enabled by default, and "enable" on an enabled add-on changes nothing (it only prints that it is
# already enabled). Disable it and enable it again with the forwarders as arguments.
microk8s disable dns
microk8s enable dns:1.1.1.1,8.8.8.8

microk8s enable hostpath-storage
microk8s kubectl apply -f /home/ubuntu/lab/storageclass.yaml
