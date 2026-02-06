#!/bin/bash
# Art Universe - Deploy to Local Kubernetes (Docker Desktop)
# Usage: ./deploy-local.sh [--skip-ingress-check] [--timeout 600]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SKIP_INGRESS_CHECK=false
WAIT_TIMEOUT=600

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-ingress-check) SKIP_INGRESS_CHECK=true; shift ;;
        --timeout) WAIT_TIMEOUT="$2"; shift 2 ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

echo -e "\033[36mDeploying Art Universe to local Kubernetes...\033[0m"

# Verify kubectl context
CONTEXT=$(kubectl config current-context)
if [ "$CONTEXT" != "docker-desktop" ]; then
    echo -e "\033[33mWarning: Current context is '$CONTEXT', not 'docker-desktop'\033[0m"
    read -p "Continue? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "\033[31mAborted.\033[0m"
        exit 1
    fi
fi

# Install NGINX Ingress if not present
if [ "$SKIP_INGRESS_CHECK" = false ]; then
    if ! kubectl get namespace ingress-nginx &>/dev/null; then
        echo -e "\n\033[33mInstalling NGINX Ingress Controller...\033[0m"
        kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.9.4/deploy/static/provider/cloud/deploy.yaml

        echo -e "\033[33mWaiting for Ingress controller to be ready...\033[0m"
        kubectl wait --namespace ingress-nginx \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/component=controller \
            --timeout=120s
    else
        echo -e "\033[32mNGINX Ingress Controller already installed.\033[0m"
    fi
fi

# Create database init ConfigMaps from shared scripts (used by both Docker and K8s)
PROJECT_ROOT="$(cd "$K8S_DIR/../.." && pwd)"
echo -e "\n\033[33mCreating database init ConfigMaps from shared scripts...\033[0m"
kubectl create configmap postgres-lastfm-master-init -n mu-data \
    --from-file="01-init.sh=$PROJECT_ROOT/env/docker/common/db/mu-raw-lastfm/initdb/01-init.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl create configmap postgres-music-data-init -n mu-data \
    --from-file="01-init.sh=$PROJECT_ROOT/env/docker/common/db/mu/initdb/01-init.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
echo -e "\033[32mConfigMaps created.\033[0m"

# Apply Kustomize overlay
echo -e "\n\033[33mApplying Kubernetes manifests...\033[0m"
kubectl apply -k "$K8S_DIR/overlays/local"

# Wait for databases
echo -e "\n\033[33mWaiting for databases to be ready...\033[0m"
if kubectl wait --for=condition=ready pod -l app=postgres-lastfm-master -n mu-data --timeout="${WAIT_TIMEOUT}s" 2>/dev/null; then
    echo -e "\033[32mpostgres-lastfm-master ready.\033[0m"
else
    echo -e "\033[33mpostgres-lastfm-master not ready within timeout.\033[0m"
fi

if kubectl wait --for=condition=ready pod -l app=postgres-music-data -n mu-data --timeout="${WAIT_TIMEOUT}s" 2>/dev/null; then
    echo -e "\033[32mpostgres-music-data ready.\033[0m"
else
    echo -e "\033[33mpostgres-music-data not ready within timeout.\033[0m"
fi

# Wait for migrations
echo -e "\n\033[33mWaiting for migrations to complete...\033[0m"
if kubectl wait --for=condition=complete job/lastfm-liquibase-migration -n mu-data --timeout="${WAIT_TIMEOUT}s" 2>/dev/null; then
    echo -e "\033[32mMigrations completed.\033[0m"
else
    echo -e "\033[33mMigration job not complete within timeout. Check: kubectl logs job/lastfm-liquibase-migration -n mu-data\033[0m"
fi

# Wait for applications
echo -e "\n\033[33mWaiting for applications to be ready...\033[0m"

declare -a APPS=(
    "app=music-data:mu-apps"
    "app=lastfm-rest-api:mu-lastfm"
)

for app_def in "${APPS[@]}"; do
    IFS=':' read -r label namespace <<< "$app_def"
    if kubectl wait --for=condition=ready pod -l "$label" -n "$namespace" --timeout="${WAIT_TIMEOUT}s" 2>/dev/null; then
        echo -e "  \033[32m$label ready.\033[0m"
    else
        echo -e "  \033[33m$label not ready within timeout.\033[0m"
    fi
done

# Summary
echo -e "\n\033[36m========================================\033[0m"
echo -e "\033[36mDeployment Complete!\033[0m"
echo -e "\033[36m========================================\033[0m"

echo -e "\n\033[33mNamespace Status:\033[0m"
kubectl get pods -n mu-data --no-headers 2>/dev/null | while read line; do echo "  [mu-data] $line"; done
kubectl get pods -n mu-lastfm --no-headers 2>/dev/null | while read line; do echo "  [mu-lastfm] $line"; done
kubectl get pods -n mu-apps --no-headers 2>/dev/null | while read line; do echo "  [mu-apps] $line"; done
kubectl get pods -n mu-frontend --no-headers 2>/dev/null | while read line; do echo "  [mu-frontend] $line"; done
kubectl get pods -n art-universe-monitoring --no-headers 2>/dev/null | while read line; do echo "  [monitoring] $line"; done

echo -e "\n\033[33mAccess Points:\033[0m"
echo -e "  \033[32m- UI:                  http://localhost:4000\033[0m"
echo -e "  \033[32m- Music Data API:      http://localhost:9082\033[0m"
echo -e "  \033[32m- Music Quiz API:      http://localhost:9083\033[0m"
echo -e "  \033[32m- LastFM REST API:     http://localhost:9084\033[0m"
echo -e "  \033[32m- LastFM ETL REST API: http://localhost:9085\033[0m"
echo -e "  \033[32m- Grafana:             http://localhost:30000\033[0m"
echo -e "  \033[32m- Prometheus:          http://localhost:30090\033[0m"
