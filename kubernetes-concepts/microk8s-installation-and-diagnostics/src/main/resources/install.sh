#!/bin/bash
# Installs MicroK8s on a fresh Ubuntu machine. Runs as root; the machine's normal user is "ubuntu".
set -euo pipefail

# TODO-00: Production runs Kubernetes 1.34. Which version does this install today, and next year?
snap install microk8s --classic

# TODO-01: The script must not return before the cluster can actually be used.

# TODO-02: The user ubuntu must be able to run "microk8s kubectl" without sudo.

# TODO-03: Collect a diagnostics report and leave it at /home/ubuntu/diagnostics.tar.gz, readable by ubuntu.
