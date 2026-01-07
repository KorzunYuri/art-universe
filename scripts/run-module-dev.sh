#!/bin/bash

# Check if we are in the project root
if [ ! -f "gradlew" ]; then
    echo "Error: Script must be run from the project root!"
    exit 1
fi

# Check if module path argument is provided
if [ -z "$1" ]; then
    echo "Usage: ./scripts/run-module-dev.sh <module-path> [additional-args]"
    echo "Example: ./scripts/run-module-dev.sh music:data:master"
    exit 1
fi

MODULE_PATH=$1
shift  # Remove first argument, leaving the rest for gradle

# Extract module name from path (get the last part after the last colon)
MODULE_NAME=$(echo $MODULE_PATH | sed 's/.*://')

echo "Starting module $MODULE_PATH"

# Load environment variables in order:
# 1. .project-root.env (REQUIRED)
PROJECT_ROOT_ENV_FILE=".project-root.env"
if [ ! -f "$PROJECT_ROOT_ENV_FILE" ]; then
    echo "Error: .project-root.env file not found!"
    echo "Please run: ./scripts/set-project-root.sh"
    exit 1
fi
echo "Loading PROJECT_ROOT from $PROJECT_ROOT_ENV_FILE"
export $(grep -v '^#' "$PROJECT_ROOT_ENV_FILE" | grep -v '^$' | xargs)

if [ -z "$PROJECT_ROOT" ]; then
    echo "Error: PROJECT_ROOT not set in .project-root.env"
    exit 1
fi

echo "Using PROJECT_ROOT=$PROJECT_ROOT"

# 2. env/docker/common/*.env (OPTIONAL - all common configuration files)
COMMON_ENV_DIR="$PROJECT_ROOT/env/docker/common"
if [ -d "$COMMON_ENV_DIR" ]; then
    echo "Loading common configuration files from $COMMON_ENV_DIR"
    for ENV_FILE in "$COMMON_ENV_DIR"/*.env; do
        if [ -f "$ENV_FILE" ]; then
            echo "  - Loading $(basename "$ENV_FILE")"
            export $(grep -v '^#' "$ENV_FILE" | grep -v '^$' | xargs)
        fi
    done
else
    echo "No common directory found at $COMMON_ENV_DIR"
fi

# 3. env/docker/local/[module name].env (OPTIONAL - local environment)
ENV_FILE_LOCAL="$PROJECT_ROOT/env/docker/local/$MODULE_NAME.env"
if [ -f "$ENV_FILE_LOCAL" ]; then
    echo "Loading local environment from $ENV_FILE_LOCAL"
    export $(grep -v '^#' "$ENV_FILE_LOCAL" | grep -v '^$' | xargs)
else
    echo "No local .env file found for module $MODULE_NAME (checked: $ENV_FILE_LOCAL)"
fi

# 4. env/docker/local/[module name].secrets.env (OPTIONAL - secrets)
ENV_FILE_SECRETS="$PROJECT_ROOT/env/docker/local/$MODULE_NAME.secrets.env"
if [ -f "$ENV_FILE_SECRETS" ]; then
    echo "Loading secrets from $ENV_FILE_SECRETS"
    export $(grep -v '^#' "$ENV_FILE_SECRETS" | grep -v '^$' | xargs)
else
    echo "No secrets file found for module $MODULE_NAME (checked: $ENV_FILE_SECRETS)"
fi

# 5. [module path]/dev.override.env (OPTIONAL - development overrides)
MODULE_DIR_PATH=$(echo $MODULE_PATH | sed 's/:/\//g')
ENV_FILE_OVERRIDE="$PROJECT_ROOT/$MODULE_DIR_PATH/dev.override.env"
if [ -f "$ENV_FILE_OVERRIDE" ]; then
    echo "Loading development overrides from $ENV_FILE_OVERRIDE"
    export $(grep -v '^#' "$ENV_FILE_OVERRIDE" | grep -v '^$' | xargs)
else
    echo "No development override file found for module $MODULE_NAME (checked: $ENV_FILE_OVERRIDE)"
fi

echo "========================================="
echo "Starting module with Spring profile: dev"
echo "========================================="

# Run gradle with provided arguments
./gradlew ":$MODULE_PATH:bootRun" --args='--spring.profiles.active=dev' "$@"
