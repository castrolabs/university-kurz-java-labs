#!/bin/sh
# Prints one number: how many more replicas of a Pod requesting 256Mi of memory the scheduler can still
# place on this (single-node) cluster right now.
set -eu

# TODO-01: The scheduler does not look at the node's total memory, and it does not look at what Pods use
#          either. Compute what it really compares, then divide.
capacity_ki=$(kubectl get nodes -o jsonpath='{.items[0].status.capacity.memory}' | sed 's/Ki$//')
echo $((capacity_ki / 262144))
