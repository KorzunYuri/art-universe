#!/bin/bash

# Cross-platform Docker stop script
# Works on Windows (Git Bash, WSL), macOS, and Linux

# Function to show usage
show_usage() {
    echo "Usage: $0 <environment>"
    echo ""
    echo "Available environments:"
    echo "  local - Stop local environment"
    echo "  prod  - Stop production environment"
    echo "  all   - Stop both environments"
    echo ""
    echo "Examples:"
    echo "  $0 local"
    echo "  $0 prod"
    echo "  $0 all"
    exit 1
}

# Check if environment parameter is provided
if [ $# -eq 0 ]; then
    echo "❌ Error: Environment parameter is required"
    show_usage
fi

ENVIRONMENT=$1

# Validate environment parameter
if [ "$ENVIRONMENT" != "local" ] && [ "$ENVIRONMENT" != "prod" ] && [ "$ENVIRONMENT" != "all" ]; then
    echo "❌ Error: Invalid environment '$ENVIRONMENT'"
    show_usage
fi

# Get the project root directory (two levels up from this script)
if [[ -n "$BASH_SOURCE" ]]; then
    # Bash
    PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
else
    # Other shells or Windows
    PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
fi

echo "=== Art Universe Docker Stop ==="
echo "Project root: $PROJECT_ROOT"
echo "Environment: $ENVIRONMENT"
echo ""

if [ "$ENVIRONMENT" == "local" ] || [ "$ENVIRONMENT" == "all" ]; then
    echo "Stopping local environment..."
    docker compose -f "$PROJECT_ROOT/env/docker/local/docker-compose.yml" stop 2>/dev/null || echo "Local environment not running"
fi

if [ "$ENVIRONMENT" == "prod" ] || [ "$ENVIRONMENT" == "all" ]; then
    echo "Stopping production environment..."
    docker compose -f "$PROJECT_ROOT/env/docker/prod/docker-compose.yml" stop 2>/dev/null || echo "Production environment not running"
fi

echo ""
echo "✅ Stop completed!"
