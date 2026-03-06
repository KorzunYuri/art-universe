#!/bin/bash
# Art Universe - Deploy to Local Kubernetes with Shared Docker Compose Volumes
# Usage: ./deploy-local-shared.sh [--skip-ingress-check] [--timeout 600] [--force]
# IMPORTANT: Stop Docker Compose before running this script!

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
K8S_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

SKIP_INGRESS_CHECK=false
WAIT_TIMEOUT=600
FORCE=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-ingress-check) SKIP_INGRESS_CHECK=true; shift ;;
        --timeout) WAIT_TIMEOUT="$2"; shift 2 ;;
        --force) FORCE=true; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

echo -e "\033[36mDeploying Art Universe to local Kubernetes (shared volumes)...\033[0m"

# Warn about Docker Compose
if [ "$FORCE" = false ]; then
    echo -e "\n\033[33mWARNING: This overlay shares data volumes with Docker Compose.\033[0m"
    echo -e "\033[33mPostgreSQL requires exclusive access to its data directory.\033[0m"
    echo -e "\033[33mMake sure Docker Compose is STOPPED before continuing.\033[0m"
    read -p $'\nIs Docker Compose stopped? (y/n) ' -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "\033[31mAborted. Stop Docker Compose first, then re-run this script.\033[0m"
        exit 1
    fi
fi

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

# Apply namespaces first (secrets and PVs need them)
echo -e "\n\033[33mApplying namespaces...\033[0m"
kubectl apply -f "$K8S_DIR/base/namespaces.yaml"

# Pre-apply shared PVs before the overlay
# This ensures PVs exist before StatefulSet PVCs are created (avoids race with dynamic provisioner)
echo -e "\n\033[33mPre-applying shared volume PVs...\033[0m"
kubectl apply -f "$K8S_DIR/overlays/local-shared/shared-volumes.yaml"

# Create database init ConfigMaps from shared scripts (used by both Docker and K8s)
PROJECT_ROOT="$(cd "$K8S_DIR/../.." && pwd)"
echo -e "\n\033[33mCreating database init ConfigMaps from shared scripts...\033[0m"
kubectl create configmap postgres-master-init -n mu-data \
    --from-file="01-init.sh=$PROJECT_ROOT/env/docker/common/db/initdb/01-init.sh" \
    --dry-run=client -o yaml | kubectl apply -f -
echo -e "\033[32mConfigMaps created.\033[0m"

# Create Grafana ConfigMaps from shared provisioning files
DASHBOARDS_DIR="$PROJECT_ROOT/env/docker/common/grafana/provisioning/dashboards"
echo -e "\n\033[33mCreating Grafana ConfigMaps from shared provisioning files...\033[0m"
kubectl create configmap grafana-dashboards -n art-universe-monitoring \
    --from-file="dashboard.yml=$DASHBOARDS_DIR/dashboard.yml" \
    --from-file="lastfm_raw_data_dashboard.json=$DASHBOARDS_DIR/lastfm_raw_data_dashboard.json" \
    --from-file="lastfm_database_metrics_dashboard.json=$DASHBOARDS_DIR/lastfm_database_metrics_dashboard.json" \
    --from-file="system_metrics_dashboard.json=$DASHBOARDS_DIR/system_metrics_dashboard.json" \
    --from-file="repository_performance_dashboard.json=$DASHBOARDS_DIR/repository_performance_dashboard.json" \
    --from-file="rest_api_performance_dashboard.json=$DASHBOARDS_DIR/rest_api_performance_dashboard.json" \
    --dry-run=client -o yaml | kubectl apply -f -
kubectl create configmap grafana-datasources -n art-universe-monitoring \
    --from-file="datasource.yml=$PROJECT_ROOT/env/docker/common/grafana/provisioning/datasources/datasource.yml" \
    --dry-run=client -o yaml | kubectl apply -f -
echo -e "\033[32mGrafana ConfigMaps created.\033[0m"

# Apply secrets (reuse from local overlay)
SECRETS_FILE="$K8S_DIR/overlays/local/secrets/secrets.yaml"
if [ -f "$SECRETS_FILE" ]; then
    echo -e "\n\033[33mApplying secrets...\033[0m"
    kubectl apply -f "$SECRETS_FILE"
    echo -e "\033[32mSecrets applied.\033[0m"
else
    echo -e "\n\033[33mNo secrets file found at: $SECRETS_FILE\033[0m"
    echo -e "\033[33mCopy and fill: overlays/local/secrets/secrets.yaml.template\033[0m"
fi

# Apply Kustomize overlay
echo -e "\n\033[33mApplying Kubernetes manifests (local-shared overlay)...\033[0m"
kubectl apply -k "$K8S_DIR/overlays/local-shared"

# Verify PV binding
echo -e "\n\033[33mChecking PersistentVolume binding...\033[0m"
kubectl get pv | grep "shared-" || true

# Wait for databases
echo -e "\n\033[33mWaiting for databases to be ready...\033[0m"
if kubectl wait --for=condition=ready pod -l app=postgres-master -n mu-data --timeout="${WAIT_TIMEOUT}s" 2>/dev/null; then
    echo -e "\033[32mpostgres-master ready.\033[0m"
else
    echo -e "\033[33mpostgres-master not ready within timeout.\033[0m"
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
echo -e "\033[36mDeployment Complete (Shared Volumes)!\033[0m"
echo -e "\033[36m========================================\033[0m"

echo -e "\n\033[33mPersistentVolume Status:\033[0m"
kubectl get pv | grep "shared-" || true

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

echo -e "\n\033[33mNote: Data is shared with Docker Compose volumes.\033[0m"
echo -e "\033[33mTo switch back: kubectl delete -k overlays/local-shared, then start Docker Compose.\033[0m"
