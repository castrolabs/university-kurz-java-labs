#!/bin/bash
# Makes this MicroK8s node trust the company registry registry.lab:5443, whose certificate is signed by
# the company's private CA (company-root-ca.crt, next to this script). Runs as root.
set -euo pipefail
lab=/home/ubuntu/lab

# TODO-00: This makes the CA trusted by the host (curl, apt, ...). Does the MicroK8s containerd use it?
cp "$lab/company-root-ca.crt" /usr/local/share/ca-certificates/company-root-ca.crt
update-ca-certificates

# TODO-01: Configure the registry for the MicroK8s containerd. Mind the exact directory name.
dir=/var/snap/microk8s/current/args/certs.d/registry.lab
mkdir -p "$dir"
