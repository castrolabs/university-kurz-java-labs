#!/bin/sh
# Usage: latest-warning.sh <namespace>
# Prints one line: the most recent Warning event in the namespace (its reason, object and message).
set -eu

# Events written through the newer events.k8s.io API (the scheduler's, among others) have no
# lastTimestamp, only eventTime, so sorting by .lastTimestamp puts them first, as if they were the
# oldest. "kubectl events" understands both kinds and lists them oldest first.
kubectl -n "$1" events --types=Warning --no-headers | tail -1
