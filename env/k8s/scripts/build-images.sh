#!/bin/bash
# Art Universe - Build Docker Images for K8s Local Deployment
# Usage: ./build-images.sh [--skip-gradle] [--skip-layers]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../.." && pwd)"

SKIP_GRADLE=false
SKIP_LAYERS=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-gradle) SKIP_GRADLE=true; shift ;;
        --skip-layers) SKIP_LAYERS=true; shift ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

echo -e "\033[36mBuilding Art Universe images for K8s local deployment...\033[0m"
echo -e "\033[90mProject root: $PROJECT_ROOT\033[0m"

# Gradle modules that need bootJar and extractLayers
GRADLE_MODULES=(
    ":music:data:raw:lastfm:lastfm-rest-api"
    ":music:data:raw:lastfm:etl:lastfm-etl-rest-api"
    ":music:data:raw:lastfm:etl:lastfm-calls-generator"
    ":music:data:raw:lastfm:etl:lastfm-calls-performer"
    ":music:data:raw:lastfm:etl:lastfm-response-parser"
    ":music:data:master"
    ":music:quiz"
)

# Build Gradle project and extract layers (unless skipped)
if [ "$SKIP_GRADLE" = false ]; then
    echo -e "\n\033[33mBuilding Gradle project and extracting layers...\033[0m"
    cd "$PROJECT_ROOT"

    # Build all modules
    BUILD_TASKS=""
    for module in "${GRADLE_MODULES[@]}"; do
        BUILD_TASKS="$BUILD_TASKS ${module}:build"
    done
    ./gradlew $BUILD_TASKS -x test

    # Extract layers for all modules (unless skipped)
    if [ "$SKIP_LAYERS" = false ]; then
        echo -e "\n\033[33mCleaning old docker-layers directories...\033[0m"
        # Module paths for cleanup
        MODULE_PATHS=(
            "music/data/raw/lastfm/lastfm-rest-api"
            "music/data/raw/lastfm/etl/lastfm-etl-rest-api"
            "music/data/raw/lastfm/etl/lastfm-calls-generator"
            "music/data/raw/lastfm/etl/lastfm-calls-performer"
            "music/data/raw/lastfm/etl/lastfm-response-parser"
            "music/data/master"
            "music/quiz"
        )
        for module_path in "${MODULE_PATHS[@]}"; do
            layers_dir="$PROJECT_ROOT/$module_path/build/docker-layers"
            if [ -d "$layers_dir" ]; then
                rm -rf "$layers_dir"
                echo -e "  \033[90mRemoved: $layers_dir\033[0m"
            fi
        done

        echo -e "\n\033[33mExtracting Spring Boot layers...\033[0m"
        EXTRACT_TASKS=""
        for module in "${GRADLE_MODULES[@]}"; do
            EXTRACT_TASKS="$EXTRACT_TASKS ${module}:extractLayers"
        done
        ./gradlew $EXTRACT_TASKS
    fi

    # Build liquibase module
    echo -e "\n\033[33mBuilding additional modules...\033[0m"
    ./gradlew :music:data:raw:lastfm:migrations:lastfm-liquibase-service:build -x test
fi

# Service definitions for Docker build
# Format: "path:image-name:dockerfile"
declare -a SERVICES=(
    "music/data/raw/lastfm/migrations/lastfm-liquibase-service:mu-lastfm-liquibase:Dockerfile.local"
    "music/data/raw/lastfm/lastfm-rest-api:mu-lastfm-rest-api:Dockerfile.local"
    "music/data/raw/lastfm/etl/lastfm-etl-rest-api:mu-lastfm-etl-rest-api:Dockerfile.local"
    "music/data/raw/lastfm/etl/lastfm-calls-generator:mu-lastfm-calls-generator:Dockerfile.local"
    "music/data/raw/lastfm/etl/lastfm-calls-performer:mu-lastfm-calls-performer:Dockerfile.local"
    "music/data/raw/lastfm/etl/lastfm-response-parser:mu-lastfm-response-parser:Dockerfile.local"
    "music/data/master:mu-music-data:Dockerfile.local"
    "music/quiz:mu-music-quiz:Dockerfile.local"
    "music/ui:mu-music-ui:Dockerfile"
)

successful=()
failed=()

echo -e "\n\033[33mBuilding Docker images...\033[0m"

for service_def in "${SERVICES[@]}"; do
    IFS=':' read -r svc_path image_name dockerfile <<< "$service_def"
    service_path="$PROJECT_ROOT/$svc_path"
    full_image_name="$image_name:local"

    echo -e "\n\033[33mBuilding $full_image_name...\033[0m"
    echo -e "  \033[90mContext: $service_path\033[0m"
    echo -e "  \033[90mDockerfile: $dockerfile\033[0m"

    if docker build -t "$full_image_name" -f "$service_path/$dockerfile" "$service_path"; then
        successful+=("$full_image_name")
        echo -e "  \033[32mSuccess!\033[0m"
    else
        failed+=("$full_image_name")
        echo -e "  \033[31mFailed!\033[0m"
    fi
done

echo -e "\n\033[36m========================================\033[0m"
echo -e "\033[36mBuild Summary\033[0m"
echo -e "\033[36m========================================\033[0m"

if [ ${#successful[@]} -gt 0 ]; then
    echo -e "\n\033[32mSuccessfully built:\033[0m"
    for img in "${successful[@]}"; do
        echo -e "  \033[32m- $img\033[0m"
    done
fi

if [ ${#failed[@]} -gt 0 ]; then
    echo -e "\n\033[31mFailed to build:\033[0m"
    for img in "${failed[@]}"; do
        echo -e "  \033[31m- $img\033[0m"
    done
    exit 1
fi

echo -e "\n\033[32mAll images built successfully!\033[0m"
echo -e "\n\033[36mLocal images:\033[0m"
docker images | grep "^mu-"
