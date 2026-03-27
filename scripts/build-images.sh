#!/bin/bash

# Art Universe - Build All Docker Images
# Usage: ./scripts/build-images.sh
#
# Builds all Docker images via Gradle dockerBuild tasks.
# Each module with ext.dockerImageName gets a dockerBuild task automatically.
# Run ./gradlew dockerBuildAll directly for the same result.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Detect gradlew command based on OS
if [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "win32" ]] || [[ -n "$WINDIR" ]]; then
    GRADLEW="./gradlew.bat"
else
    GRADLEW="./gradlew"
fi

echo -e "\033[36mBuilding Art Universe Docker images...\033[0m"
echo "Project root: $PROJECT_ROOT"
echo "Gradle command: $GRADLEW"

cd "$PROJECT_ROOT"
$GRADLEW dockerBuildAll -x test

echo -e "\n\033[32mAll images built successfully!\033[0m"
echo -e "\n\033[36mLocal images:\033[0m"
docker images | grep -E "^(mu-|au-|art-)"
