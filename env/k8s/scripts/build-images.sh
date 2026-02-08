#!/bin/bash
# Art Universe - Build Docker Images for K8s Deployment
# Usage: ./build-images.sh [--skip-gradle]
# Images are built via Gradle dockerBuild tasks defined in each module.
# Run ./gradlew dockerBuildAll directly for the same result.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

SKIP_GRADLE=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-gradle) SKIP_GRADLE=true; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

echo -e "\033[36mBuilding Art Universe images for K8s deployment...\033[0m"

if [ "$SKIP_GRADLE" = false ]; then
    cd "$PROJECT_ROOT"
    ./gradlew dockerBuildAll -x test
else
    echo -e "\033[33mSkipping Gradle build (using existing images)\033[0m"
fi

echo -e "\n\033[32mAll images built successfully!\033[0m"
echo -e "\n\033[36mLocal images:\033[0m"
docker images | grep "^mu-"
