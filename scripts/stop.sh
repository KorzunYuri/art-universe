#!/bin/bash

# Art Universe - Stop Orchestrator
# Usage: ./scripts/stop.sh <docker|k8s> <environment>
#
# Platforms and environments:
#   docker: local, prod, all
#   k8s:    local, local-shared, prod
#
# Examples:
#   ./scripts/stop.sh docker local
#   ./scripts/stop.sh k8s local-shared

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

show_usage() {
    echo "Usage: $0 <docker|k8s> <environment>"
    echo ""
    echo "Platforms and environments:"
    echo "  docker: local, prod, all"
    echo "  k8s:    local, local-shared, prod"
    echo ""
    echo "Examples:"
    echo "  $0 docker local"
    echo "  $0 k8s local-shared"
    exit 1
}

# Require at least 2 arguments
if [ $# -lt 2 ]; then
    echo "Error: Platform and environment are required"
    show_usage
fi

PLATFORM=$1
ENVIRONMENT=$2

# Validate platform
if [ "$PLATFORM" != "docker" ] && [ "$PLATFORM" != "k8s" ]; then
    echo "Error: Invalid platform '$PLATFORM'. Must be 'docker' or 'k8s'"
    show_usage
fi

# Validate environment per platform
case "$PLATFORM" in
    docker)
        if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "prod" ] && [ "$ENVIRONMENT" != "all" ]; then
            echo "Error: Invalid Docker environment '$ENVIRONMENT'. Must be 'local', 'prod', or 'all'"
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

# Delegate to platform-specific script
case "$PLATFORM" in
    docker)
        exec "$PROJECT_ROOT/env/docker/stop.sh" "$ENVIRONMENT"
        ;;
    k8s)
        exec "$PROJECT_ROOT/env/k8s/scripts/stop.sh" "$ENVIRONMENT"
        ;;
esac
