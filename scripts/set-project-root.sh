#!/bin/bash

# Script to set PROJECT_ROOT environment variable for IntelliJ IDEA run configurations
# This script detects the environment (Windows/WSL) and sets the appropriate path format

# Function to detect if we're in WSL
is_wsl() {
    if [[ -f /proc/version ]] && grep -qi microsoft /proc/version; then
        return 0  # true - we're in WSL
    else
        return 1  # false - we're not in WSL
    fi
}

# Function to convert WSL path to Windows path
wsl_to_windows_path() {
    local wsl_path="$1"
    # Convert /mnt/d/... to D:\...
    echo "$wsl_path" | sed 's|^/mnt/\([a-z]\)/|\U\1:/|' | sed 's|/|\\|g'
}

# Function to convert Windows path to WSL path
windows_to_wsl_path() {
    local win_path="$1"
    # Convert D:\... to /mnt/d/...
    echo "$win_path" | sed 's|^\([A-Z]\):|/mnt/\L\1|' | sed 's|\\|/|g'
}

# Get the current script directory and resolve project root (one level up from scripts)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT_RESOLVED="$(cd "$SCRIPT_DIR/.." && pwd)"

# Detect environment and set PROJECT_ROOT accordingly
if is_wsl; then
    # We're in WSL - use WSL path format
    export PROJECT_ROOT="$PROJECT_ROOT_RESOLVED"
    echo "WSL environment detected"
    echo "PROJECT_ROOT set to: $PROJECT_ROOT"
else
    # We're in native Windows or other Unix - check if path looks like WSL mount
    if [[ "$PROJECT_ROOT_RESOLVED" == /mnt/* ]]; then
        # Convert WSL path to Windows path
        export PROJECT_ROOT=$(wsl_to_windows_path "$PROJECT_ROOT_RESOLVED")
        echo "WSL mount path detected, converted to Windows format"
        echo "PROJECT_ROOT set to: $PROJECT_ROOT"
    else
        # Use path as-is
        export PROJECT_ROOT="$PROJECT_ROOT_RESOLVED"
        echo "Native path format"
        echo "PROJECT_ROOT set to: $PROJECT_ROOT"
    fi
fi

# For IntelliJ IDEA - also create a temporary file with the variable in project root
echo "PROJECT_ROOT=$PROJECT_ROOT" > "$PROJECT_ROOT_RESOLVED/.project-root.env"
echo "Environment variable saved to .project-root.env"

# Export for current session
echo "To use in current session, run: export PROJECT_ROOT=\"$PROJECT_ROOT\""
