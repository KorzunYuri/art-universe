#!/bin/bash

# Art Universe - Docker Compose Deployment Script
# Usage: ./env/docker/deploy.sh <environment> [--skip-build]
#
# Environments: local, prod
# --skip-build: skip Docker image building (use pre-built images)

set -e

# Function to show usage
show_usage() {
    echo "Usage: $0 <environment> [--skip-build]"
    echo ""
    echo "Available environments:"
    echo "  local - Deploy with local databases in containers"
    echo "  prod  - Deploy with external databases on host machine"
    echo ""
    echo "Options:"
    echo "  --skip-build  Skip Docker image building (use pre-built images)"
    echo ""
    echo "Examples:"
    echo "  $0 local"
    echo "  $0 prod --skip-build"
    exit 1
}

# Check if environment parameter is provided
if [ $# -eq 0 ]; then
    echo "Error: Environment parameter is required"
    show_usage
fi

ENVIRONMENT=$1
shift

# Validate environment parameter
if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo "Error: Invalid environment '$ENVIRONMENT'"
    show_usage
fi

# Parse optional flags
SKIP_BUILD=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build) SKIP_BUILD=true; shift ;;
        *) echo "Unknown option: $1"; show_usage ;;
    esac
done

# Get the project root directory (two levels up from this script)
if [[ -n "$BASH_SOURCE" ]]; then
    PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
else
    PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
fi

# Set environment-specific variables
if [ "$ENVIRONMENT" == "local" ]; then
    COMPOSE_FILE="$PROJECT_ROOT/env/docker/local/docker-compose.yml"
    ENV_NAME="Local"
    source "$PROJECT_ROOT/env/docker/local/.env"
else
    COMPOSE_FILE="$PROJECT_ROOT/env/docker/prod/docker-compose.yml"
    ENV_NAME="Production"
    source "$PROJECT_ROOT/env/docker/prod/.env"
fi

echo "=== Art Universe $ENV_NAME Environment Deployment ==="
echo "Project root: $PROJECT_ROOT"
echo "Environment: $ENVIRONMENT"

# Stop and remove existing containers
echo ""
echo "Step 1: Stopping and removing existing containers..."
docker compose -f "$COMPOSE_FILE" down --remove-orphans

# Build Docker images
if [ "$SKIP_BUILD" = false ]; then
    echo ""
    echo "Step 2: Building Docker images..."
    "$PROJECT_ROOT/scripts/build-images.sh"
else
    echo ""
    echo "Step 2: Skipping image build (--skip-build)"
fi

# Start services with pre-built images
echo ""
echo "Step 3: Starting $ENVIRONMENT environment..."
docker compose -f "$COMPOSE_FILE" up -d --force-recreate

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ $ENV_NAME environment deployed successfully!"
    echo ""
    echo "Services available at:"
    echo "  - LastFM REST API: http://localhost:${MURAW_LASTFM_REST_API_EXTERNAL_PORT}"
    echo "  - LastFM ETL REST API: http://localhost:${MURAW_LASTFM_ETL_REST_API_EXTERNAL_PORT}"
    echo "  - LastFM Calls Generator: actuator http://localhost:${MURAW_LASTFM_CALLS_GENERATOR_ACTUATOR_EXTERNAL_PORT}"
    echo "  - LastFM Calls Performer: actuator http://localhost:${MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_EXTERNAL_PORT}"
    echo "  - LastFM Response Parser: actuator http://localhost:${MURAW_LASTFM_RESPONSE_PARSER_ACTUATOR_EXTERNAL_PORT}"
    echo "  - Music Data: http://localhost:${MU_DATA_APP_EXTERNAL_PORT}"
    echo "  - Music Quiz: http://localhost:${MU_QUIZ_APP_EXTERNAL_PORT}"
    echo "  - UI: http://localhost:${MU_UI_EXTERNAL_PORT}"
    if [ "$ENVIRONMENT" == "local" ]; then
        echo "  - Prometheus: http://localhost:${PROMETHEUS_PORT}"
        echo "  - Grafana: http://localhost:${GRAFANA_PORT}"
    else
        echo ""
        echo "Note: Applications connect to external databases on host machine"
    fi
else
    echo "Deployment failed!"
    exit 1
fi
