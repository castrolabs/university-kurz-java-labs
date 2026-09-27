#!/bin/sh
# Runs inside the cluster node with kubectl configured as cluster admin.
set -eu

# TODO-00: Stop the "web" app in the "shop" namespace for maintenance. Its Deployment must stay,
#          so it can be started again later, but no Pod may keep running.
kubectl -n shop delete pods -l app=web

# TODO-01: "old-team" was deleted and has been Terminating ever since. Find what blocks it and remove
#          the blocker, without skipping the namespace's own cleanup. Check the namespace's status
#          conditions first.
