#!/bin/bash
# Installs MicroK8s on a fresh Ubuntu machine. Runs as root; the machine's normal user is "ubuntu".
set -euo pipefail

# <k8s minor>/<risk>: patch updates arrive automatically, a new minor only when you change the channel.
# Without --channel you get the default track of the day (1.35 in September 2026, while latest was 1.36).
snap install microk8s --classic --channel=1.34/stable

# snap install returns when the services are started, not when the API server answers.
microk8s status --wait-ready --timeout 600

# The microk8s group can read the admin credentials: it is effectively cluster-admin, like the docker group.
usermod -a -G microk8s ubuntu
mkdir -p /home/ubuntu/.kube
chown -R ubuntu:ubuntu /home/ubuntu/.kube

# inspect checks every service and writes a tarball; its path contains the snap revision and a date.
report=$(microk8s inspect | grep -o '/var/snap/microk8s/[^ ]*inspection-report-[^ ]*\.tar\.gz')
cp "$report" /home/ubuntu/diagnostics.tar.gz
chown ubuntu:ubuntu /home/ubuntu/diagnostics.tar.gz
