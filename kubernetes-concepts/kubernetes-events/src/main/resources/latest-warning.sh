#!/bin/sh
# Usage: latest-warning.sh <namespace>
# Prints one line: the most recent Warning event in the namespace (its reason, object and message).
set -eu

# TODO-00: This looks right and prints the wrong event. Find out which events it misses and why,
#          and print the most recent Warning correctly.
kubectl -n "$1" get events --field-selector type=Warning --sort-by=.lastTimestamp --no-headers | tail -1
