#!/bin/bash

# Art Universe - Deploy Orchestrator
# Usage: ./scripts/deploy.sh <docker|k8s> <environment> [--skip-build] [extra flags...]
#
# Platforms and environments:
#   docker: local, prod
#   k8s:    local, local-shared, prod
#
# Options:
#   --skip-build  Skip Docker image building (use pre-built images)
#
# Extra flags are forwarded to the platform-specific deploy script.
# K8s flags: --skip-ingress-check, --timeout <seconds>, --force (local-shared only)
#
# Examples:
#   ./scripts/deploy.sh docker local
#   ./scripts/deploy.sh k8s local-shared --skip-build
#   ./scripts/deploy.sh k8s prod --skip-ingress-check --timeout 300

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

show_usage() {
    echo "Usage: $0 <docker|k8s> <environment> [--skip-build] [extra flags...]"
    echo ""
    echo "Platforms and environments:"
    echo "  docker: local, prod"
    echo "  k8s:    local, local-shared, prod"
    echo ""
    echo "Options:"
    echo "  --skip-build  Skip Docker image building"
    echo ""
    echo "Extra flags are forwarded to the platform-specific deploy script."
    echo "K8s flags: --skip-ingress-check, --timeout <seconds>, --force (local-shared only)"
    echo ""
    echo "Examples:"
    echo "  $0 docker local"
    echo "  $0 k8s local-shared --skip-build"
    echo "  $0 k8s prod --skip-ingress-check --timeout 300"
    exit 1
}

# Require at least 2 arguments
if [ $# -lt 2 ]; then
    echo "Error: Platform and environment are required"
    show_usage
fi

PLATFORM=$1
ENVIRONMENT=$2
shift 2

# Validate platform
if [ "$PLATFORM" != "docker" ] && [ "$PLATFORM" != "k8s" ]; then
    echo "Error: Invalid platform '$PLATFORM'. Must be 'docker' or 'k8s'"
    show_usage
fi

# Validate environment per platform
case "$PLATFORM" in
    docker)
        if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "prod" ]; then
            echo "Error: Invalid Docker environment '$ENVIRONMENT'. Must be 'local' or 'prod'"
            show_usage
        fi
        ;;
    k8s)
        if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "local-shared" ] && [ "$ENVIRONMENT" != "prod" ]; then
            echo "Error: Invalid K8s environment '$ENVIRONMENT'. Must be 'local', 'local-shared', or 'prod'"
            show_usage
        fi
        ;;
esac

# Extract --skip-build from remaining args, collect the rest as extra flags
SKIP_BUILD=false
EXTRA_FLAGS=()
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build) SKIP_BUILD=true; shift ;;
        *) EXTRA_FLAGS+=("$1"); shift ;;
    esac
done

echo "=== Art Universe Deploy ==="
echo "Platform: $PLATFORM"
echo "Environment: $ENVIRONMENT"

# Build images unless skipped
if [ "$SKIP_BUILD" = false ]; then
    "$PROJECT_ROOT/scripts/build-images.sh"
else
    echo "Skipping image build (--skip-build)"
fi

# Delegate to platform-specific script
echo ""
case "$PLATFORM" in
    docker)
        exec "$PROJECT_ROOT/env/docker/deploy.sh" "$ENVIRONMENT" --skip-build "${EXTRA_FLAGS[@]}"
        ;;
    k8s)
        exec "$PROJECT_ROOT/env/k8s/scripts/deploy-${ENVIRONMENT}.sh" "${EXTRA_FLAGS[@]}"
        ;;
esac
