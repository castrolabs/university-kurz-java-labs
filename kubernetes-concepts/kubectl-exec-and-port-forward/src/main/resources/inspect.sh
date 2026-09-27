#!/bin/sh
# Runs on the cluster node (not in a Pod), with kubectl configured as cluster admin.
# Prints two lines about the running "web" app:
#   1. the worker_processes line of the nginx.conf that the running container uses
#   2. the Server header the app sends back
set -eu

# TODO-00: The image has no shell and no cat. Read the file anyway, without changing the Deployment.
kubectl exec deploy/web -- cat /etc/nginx/nginx.conf | grep -m1 worker_processes

# TODO-01: This script runs outside the cluster network and its DNS. Reach the app through the API
#          server instead, and clean up after yourself.
wget -S -qO /dev/null http://web:8080 2>&1 | grep -i -m1 'server:'
