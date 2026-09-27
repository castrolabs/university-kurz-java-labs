#!/bin/sh
# Runs on the cluster node (not in a Pod), with kubectl configured as cluster admin.
# Prints two lines about the running "web" app:
#   1. the worker_processes line of the nginx.conf that the running container uses
#   2. the Server header the app sends back
set -eu

pod=$(kubectl get pod -l app=web -o jsonpath='{.items[0].metadata.name}')
name="inspect-$$"   # ephemeral containers cannot be removed, so every run needs a new name

# An ephemeral container with tools, sharing the process namespace of the "nginx" container.
# /proc/1/root is the root filesystem as nginx (PID 1 there) sees it.
kubectl debug "pod/$pod" -q --image=busybox:1.37 --target=nginx -c "$name" -- \
  grep -m1 worker_processes /proc/1/root/etc/nginx/nginx.conf >/dev/null
# --attach can miss the output of a command this short, so wait for it to finish and read its logs.
i=0
until kubectl get pod "$pod" -o jsonpath="{.status.ephemeralContainerStatuses[?(@.name==\"$name\")].state.terminated.reason}" | grep -q .; do
  i=$((i + 1)); [ "$i" -lt 60 ] || { echo "debug container did not finish" >&2; exit 1; }
  sleep 1
done
kubectl logs "$pod" -c "$name"

# A tunnel through the API server to the Pod: no Service, DNS or network route needed.
kubectl port-forward deploy/web 18080:8080 >/dev/null 2>&1 &
pf=$!
trap 'kill "$pf" 2>/dev/null' EXIT
i=0
until wget -q -O /dev/null http://127.0.0.1:18080 2>/dev/null; do
  i=$((i + 1)); [ "$i" -lt 30 ] || { echo "port-forward did not come up" >&2; exit 1; }
  sleep 1
done
wget -S -qO /dev/null http://127.0.0.1:18080 2>&1 | grep -i -m1 'server:' | sed 's/^ *//'
