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
    echo "Usage: ./run-local.sh <module-path> [additional-args]"
    echo "Example: ./run-local.sh music-universe:music-data"
    exit 1
fi

MODULE_PATH=$1
shift  # Remove first argument, leaving the rest for gradle

# Extract module name from path
MODULE_NAME=$(echo $MODULE_PATH | sed 's/.*://')

# Define path to module's .env file
ENV_FILE="$PROJECT_ROOT/${MODULE_PATH/://}/.env"

# Load environment variables from module's .env file if it exists
if [ -f "$ENV_FILE" ]; then
    echo "Loading environment variables from $ENV_FILE"
    export $(grep -v '^#' "$ENV_FILE" | xargs)
else
    echo "No .env file found for module $MODULE_NAME at path $ENV_FILE"
fi

echo "Starting module $MODULE_PATH with PROJECT_ROOT=$PROJECT_ROOT"

# Run gradle with provided arguments
./gradlew ":$MODULE_PATH:bootRun" --args='--spring.profiles.active=local' "$@"
