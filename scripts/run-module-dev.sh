#!/bin/bash

# Define project root as current directory
export PROJECT_ROOT=$(pwd)

# Check if we are in the project root
if [ ! -f "gradlew" ]; then
    echo "Error: Script must be run from the project root!"
    exit 1
fi

# Check if module path argument is provided
if [ -z "$1" ]; then
    echo "Usage: ./scripts/run-module-dev.sh <module-path> [additional-args]"
    echo "Example: ./scripts/run-module-dev.sh music-universe:music-data"
    exit 1
fi

MODULE_PATH=$1
shift  # Remove first argument, leaving the rest for gradle

# Extract module name from path (get the last part after the last colon)
MODULE_NAME=$(echo $MODULE_PATH | sed 's/.*://')

echo "Starting module $MODULE_PATH with PROJECT_ROOT=$PROJECT_ROOT"

# Load environment variables in order:
# 1. env/docker/local/[module name].env
ENV_FILE_1="$PROJECT_ROOT/env/docker/local/$MODULE_NAME.env"
if [ -f "$ENV_FILE_1" ]; then
    echo "Loading environment variables from $ENV_FILE_1"
    export $(grep -v '^#' "$ENV_FILE_1" | grep -v '^$' | xargs)
else
    echo "No main .env file found for module $MODULE_NAME at path $ENV_FILE_1"
fi

# 2. env/docker/local/[module name].secrets.env
ENV_FILE_2="$PROJECT_ROOT/env/docker/local/$MODULE_NAME.secrets.env"
if [ -f "$ENV_FILE_2" ]; then
    echo "Loading secrets from $ENV_FILE_2"
    export $(grep -v '^#' "$ENV_FILE_2" | grep -v '^$' | xargs)
else
    echo "No secrets file found for module $MODULE_NAME at path $ENV_FILE_2"
fi

# 3. [module path]/dev.override.env
MODULE_DIR_PATH=$(echo $MODULE_PATH | sed 's/:/\//')
ENV_FILE_3="$PROJECT_ROOT/$MODULE_DIR_PATH/dev.override.env"
if [ -f "$ENV_FILE_3" ]; then
    echo "Loading local overrides from $ENV_FILE_3"
    export $(grep -v '^#' "$ENV_FILE_3" | grep -v '^$' | xargs)
else
    echo "No local override file found for module $MODULE_NAME at path $ENV_FILE_3"
fi

# Run gradle with provided arguments
./gradlew ":$MODULE_PATH:bootRun" --args='--spring.profiles.active=dev' "$@"
