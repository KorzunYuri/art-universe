#!/bin/bash

# Cross-platform Docker deployment script
# Works on Windows (Git Bash, WSL), macOS, and Linux

# Function to show usage
show_usage() {
    echo "Usage: $0 <environment>"
    echo ""
    echo "Available environments:"
    echo "  local - Deploy with local databases in containers"
    echo "  prod  - Deploy with external databases on host machine"
    echo ""
    echo "Examples:"
    echo "  $0 local"
    echo "  $0 prod"
    exit 1
}

# Check if environment parameter is provided
if [ $# -eq 0 ]; then
    echo "❌ Error: Environment parameter is required"
    show_usage
fi

ENVIRONMENT=$1

# Validate environment parameter
if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo "❌ Error: Invalid environment '$ENVIRONMENT'"
    show_usage
fi

# Function to detect if we're in WSL
is_wsl() {
    if [[ -f /proc/version ]] && grep -qi microsoft /proc/version; then
        return 0
    else
        return 1
    fi
}

# Function to get the correct gradlew command
get_gradlew_cmd() {
    if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]] || [[ -n "$WINDIR" ]]; then
        echo "./gradlew.bat"
    else
        echo "./gradlew"
    fi
}

# Get the project root directory (two levels up from this script)
if [[ -n "$BASH_SOURCE" ]]; then
    # Bash
    PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
else
    # Other shells or Windows
    PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
fi

# Get the appropriate gradlew command
GRADLEW_CMD=$(get_gradlew_cmd)

# Set environment-specific variables
if [ "$ENVIRONMENT" == "local" ]; then
    COMPOSE_FILE="$PROJECT_ROOT/env/docker/local/docker-compose.yml"
    ENV_NAME="Local"
    # Source the environment file to get the ports
    source "$PROJECT_ROOT/env/docker/local/.env"
else
    COMPOSE_FILE="$PROJECT_ROOT/env/docker/prod/docker-compose.yml"
    ENV_NAME="Production"
    # Source the environment file to get the ports
    source "$PROJECT_ROOT/env/docker/prod/.env"
fi

echo "=== Art Universe $ENV_NAME Environment Deployment ==="
echo "Project root: $PROJECT_ROOT"
echo "Gradle command: $GRADLEW_CMD"
echo "Environment: $ENVIRONMENT"

# Stop and remove existing containers and images
echo ""
echo "Step 1: Stopping and removing existing containers..."
docker compose -f "$COMPOSE_FILE" down --remove-orphans

# Build the project only for local environment
if [ "$ENVIRONMENT" == "local" ]; then
    echo ""
    echo "Step 2: Building project for local environment..."
    cd "$PROJECT_ROOT" || exit
    $GRADLEW_CMD clean build -x test

    if [ $? -ne 0 ]; then
        echo "❌ Build failed! Aborting deployment."
        exit 1
    fi
    
    STEP_NUMBER="Step 3"
else
    echo ""
    echo "Step 2: Skipping Gradle build for production environment (will build inside Docker)"
    STEP_NUMBER="Step 2"
fi

# Start with rebuilding images
echo ""
echo "$STEP_NUMBER: Starting $ENVIRONMENT environment..."
docker compose -f "$COMPOSE_FILE" up -d --build --force-recreate

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ $ENV_NAME environment deployed successfully!"
    echo ""
    echo "Services available at:"
    if [ "$ENVIRONMENT" == "local" ]; then
        echo "  - LastFM REST API: http://localhost:${MURAW_LASTFM_REST_API_EXTERNAL_PORT}"
        echo "  - LastFM ETL REST API: http://localhost:${MURAW_LASTFM_ETL_REST_API_EXTERNAL_PORT}"
        echo "  - LastFM Calls Generator: actuator http://localhost:${MURAW_LASTFM_CALLS_GENERATOR_ACTUATOR_EXTERNAL_PORT}"
        echo "  - LastFM Calls Performer: actuator http://localhost:${MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_EXTERNAL_PORT}"
        echo "  - LastFM Response Parser: actuator http://localhost:${MURAW_LASTFM_RESPONSE_PARSER_ACTUATOR_EXTERNAL_PORT}"
        echo "  - Music Data: http://localhost:${MU_DATA_APP_EXTERNAL_PORT}"
        echo "  - Music Quiz: http://localhost:${MU_QUIZ_APP_EXTERNAL_PORT}"
        echo "  - UI: http://localhost:${MU_UI_EXTERNAL_PORT}"
        echo "  - Adminer: http://localhost:${DB_ADMINER_PORT}"
        echo "  - Prometheus: http://localhost:${PROMETHEUS_PORT}"
        echo "  - Grafana: http://localhost:${GRAFANA_PORT}"
    else
        echo "  - LastFM REST API: http://localhost:${MURAW_LASTFM_REST_API_EXTERNAL_PORT}"
        echo "  - LastFM ETL REST API: http://localhost:${MURAW_LASTFM_ETL_REST_API_EXTERNAL_PORT}"
        echo "  - LastFM Calls Generator: actuator http://localhost:${MURAW_LASTFM_CALLS_GENERATOR_ACTUATOR_EXTERNAL_PORT}"
        echo "  - LastFM Calls Performer: actuator http://localhost:${MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_EXTERNAL_PORT}"
        echo "  - LastFM Response Parser: actuator http://localhost:${MURAW_LASTFM_RESPONSE_PARSER_ACTUATOR_EXTERNAL_PORT}"
        echo "  - Music Data: http://localhost:${MU_DATA_APP_EXTERNAL_PORT}"
        echo "  - Music Quiz: http://localhost:${MU_QUIZ_APP_EXTERNAL_PORT}"
        echo "  - UI: http://localhost:${MU_UI_EXTERNAL_PORT}"
        echo "  - Adminer: http://localhost:${DB_ADMINER_PORT}"
        echo ""
        echo "Note: Applications connect to external databases on host machine"
    fi
else
    echo "❌ Deployment failed!"
    exit 1
fi
