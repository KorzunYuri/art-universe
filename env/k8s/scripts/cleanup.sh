#!/bin/bash

# Art Universe - Full Kubernetes Cleanup
# Usage: ./cleanup.sh <local|local-shared|prod>
#
# Performs full teardown: removes Kustomize resources, namespaces, and PVs/PVCs.
# For local-shared: PVs are deleted but data stays in Docker named volumes.
# For local: PVCs (and their dynamically-provisioned PVs) are deleted.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

show_usage() {
    echo "Usage: $0 <local|local-shared|prod>"
    echo ""
    echo "Full teardown: Kustomize resources + namespaces + PVs/PVCs."
    echo "Use stop.sh for graceful stop that preserves namespaces and data."
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

echo -e "\033[36mFull cleanup of Art Universe K8s deployment ($ENVIRONMENT)...\033[0m"

# Step 1: Remove Kustomize resources
echo -e "\n\033[33mStep 1: Deleting Kustomize resources...\033[0m"
kubectl delete -k "$K8S_DIR/overlays/$ENVIRONMENT" --ignore-not-found 2>/dev/null || true

# Step 2: Environment-specific cleanup (before namespace deletion)
case "$ENVIRONMENT" in
    local)
        echo -e "\n\033[33mStep 2: Deleting PVCs (local)...\033[0m"
        kubectl delete pvc --all -n mu-data --ignore-not-found 2>/dev/null || true
        kubectl delete pvc --all -n art-universe-monitoring --ignore-not-found 2>/dev/null || true
        ;;
    local-shared)
        echo -e "\n\033[33mStep 2: Deleting shared PVs (data stays in Docker named volumes)...\033[0m"
        kubectl delete pv shared-lastfm-master-data shared-lastfm-replica-data shared-music-data-db \
            shared-prometheus-data shared-grafana-data --ignore-not-found 2>/dev/null || true
        ;;
    prod)
        echo -e "\n\033[33mStep 2: No extra cleanup for prod.\033[0m"
        ;;
esac

# Step 3: Delete namespaces
echo -e "\n\033[33mStep 3: Deleting namespaces...\033[0m"
kubectl delete namespace mu-data mu-lastfm mu-apps mu-frontend art-universe-monitoring \
    --ignore-not-found 2>/dev/null || true

# Step 4: Clean up dangling images
echo -e "\n\033[33mStep 4: Cleaning up dangling images...\033[0m"
docker image prune -f 2>/dev/null || true

echo -e "\n\033[32mFull cleanup completed ($ENVIRONMENT).\033[0m"
