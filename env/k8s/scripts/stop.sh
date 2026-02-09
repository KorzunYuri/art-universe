#!/bin/bash

# Art Universe - Stop Kubernetes Deployment
# Usage: ./stop.sh <local|local-shared|prod>
#
# Removes all Kustomize-managed resources for the specified overlay.
# Data in PersistentVolumes is preserved (PVs/PVCs are not deleted).
# To also remove PVs/PVCs and namespaces, use cleanup.sh instead.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

show_usage() {
    echo "Usage: $0 <local|local-shared|prod>"
    echo ""
    echo "Removes Kustomize-managed resources. Data in PVs is preserved."
    echo "Use cleanup.sh for full teardown (including namespaces and PVs)."
    echo ""
    echo "Examples:"
    echo "  $0 local"
    echo "  $0 local-shared"
    echo "  $0 prod"
    exit 1
}

if [ $# -eq 0 ]; then
    echo "Error: Environment parameter is required"
    show_usage
fi

ENVIRONMENT=$1

# Validate environment
if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "local-shared" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo "Error: Invalid environment '$ENVIRONMENT'"
    show_usage
fi

echo -e "\033[36mStopping Art Universe K8s deployment ($ENVIRONMENT)...\033[0m"

# Delete Kustomize-managed resources
echo -e "\n\033[33mDeleting Kustomize resources for overlay: $ENVIRONMENT\033[0m"
kubectl delete -k "$K8S_DIR/overlays/$ENVIRONMENT" --ignore-not-found 2>/dev/null || true

echo -e "\n\033[33mRemaining resources:\033[0m"
kubectl get pods -n mu-data --no-headers 2>/dev/null | while read line; do echo "  [mu-data] $line"; done
kubectl get pods -n mu-lastfm --no-headers 2>/dev/null | while read line; do echo "  [mu-lastfm] $line"; done
kubectl get pods -n mu-apps --no-headers 2>/dev/null | while read line; do echo "  [mu-apps] $line"; done
kubectl get pods -n mu-frontend --no-headers 2>/dev/null | while read line; do echo "  [mu-frontend] $line"; done
kubectl get pods -n art-universe-monitoring --no-headers 2>/dev/null | while read line; do echo "  [monitoring] $line"; done

echo -e "\n\033[32mK8s deployment stopped ($ENVIRONMENT).\033[0m"
echo "Note: PersistentVolumes and namespaces are preserved. Use cleanup.sh for full teardown."
