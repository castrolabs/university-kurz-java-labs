#!/bin/bash
# Makes this MicroK8s node trust the company registry registry.lab:5443, whose certificate is signed by
# the company's private CA (company-root-ca.crt, next to this script). Runs as root.
set -euo pipefail
lab=/home/ubuntu/lab

# The MicroK8s containerd reads per-registry settings from certs.d. The directory name is the registry
# host exactly as written in image references, port included.
dir=/var/snap/microk8s/current/args/certs.d/registry.lab:5443
mkdir -p "$dir"
cp "$lab/company-root-ca.crt" "$dir/ca.crt"
cat > "$dir/hosts.toml" <<TOML
server = "https://registry.lab:5443"

[host."https://registry.lab:5443"]
  capabilities = ["pull", "resolve"]
  ca = "$dir/ca.crt"
TOML
