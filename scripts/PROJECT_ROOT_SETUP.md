# PROJECT_ROOT Environment Variable Setup

This document describes how to set up the `PROJECT_ROOT` environment variable for IntelliJ IDEA run configurations and other development tools.

## Overview

Some modules (like `music-data-raw-lastfm`) require the `PROJECT_ROOT` environment variable to be set to locate configuration files and resources. This is especially important when running individual modules from IntelliJ IDEA.

## Scripts Location

The scripts are located in the `scripts/` directory:
- `scripts/set-project-root.sh` - For Unix/Linux/macOS/WSL/Git Bash
- `scripts/set-project-root.bat` - For Windows Command Prompt

## Scripts

### set-project-root.sh (Unix/Linux/macOS/WSL/Git Bash)

Cross-platform bash script that:
- Detects if running in WSL or native environment
- Sets appropriate path format (Windows vs Unix)
- Exports `PROJECT_ROOT` environment variable
- Creates `.project-root.env` file in project root for IntelliJ IDEA

**Usage:**
```bash
# Make executable (if needed)
chmod +x scripts/set-project-root.sh

# Run the script from project root
./scripts/set-project-root.sh
```

### set-project-root.bat (Windows Command Prompt)

Windows batch script that:
- Sets `PROJECT_ROOT` to project root directory
- Creates `.project-root.env` file in project root
- Optionally sets permanent environment variable

**Usage:**
```cmd
scripts\set-project-root.bat
```

## IntelliJ IDEA Integration

### Method 1: Before Launch Task

1. Open your run configuration (e.g., for `music-data-raw-lastfm`)
2. Go to "Before launch" section
3. Add "Run External tool"
4. Configure the external tool:
   - **Name**: Set PROJECT_ROOT
   - **Program**: `bash` (or full path to bash)
   - **Arguments**: `scripts/set-project-root.sh`
   - **Working directory**: `$ProjectFileDir$`

### Method 2: Environment Variables

1. Run the appropriate script once to generate `.project-root.env` in project root
2. In your run configuration, add environment variable:
   - **Name**: `PROJECT_ROOT`
   - **Value**: Copy from the generated `.project-root.env` file

### Method 3: VM Options (for Java applications)

Add to VM options:
```
-DPROJECT_ROOT=/path/to/your/project
```

## Environment Detection

The scripts automatically detect the environment:

### WSL Detection
- Checks for `/proc/version` containing "microsoft"
- Uses WSL path format (`/mnt/d/...`)

### Windows Detection
- Detects Git Bash, MSYS, or native Windows
- Converts paths to Windows format (`D:\...`)

### Path Conversion Examples

| Environment | Input Path | Output Path |
|-------------|------------|-------------|
| WSL | `/mnt/d/projects/art-universe` | `/mnt/d/projects/art-universe` |
| Git Bash | `/d/projects/art-universe` | `D:\projects\art-universe` |
| Windows CMD | `D:\projects\art-universe` | `D:\projects\art-universe` |

## Troubleshooting

### Script Not Found
Make sure you're running from the project root directory:
```bash
# Correct
./scripts/set-project-root.sh

# Incorrect
./set-project-root.sh
```

### Permission Denied (Unix/Linux/macOS)
```bash
chmod +x scripts/set-project-root.sh
```

### WSL Path Issues
The script automatically handles WSL to Windows path conversion. If you encounter issues:
1. Verify you're in the correct directory
2. Check that the path contains `/mnt/` for WSL detection

### IntelliJ IDEA Not Finding Variable
1. Restart IntelliJ IDEA after setting environment variables
2. Check that the `.project-root.env` file was created in project root
3. Verify the path format matches your environment

## Manual Setup

If the scripts don't work, you can manually set the environment variable:

### Windows
```cmd
set PROJECT_ROOT=D:\path\to\art-universe
```

### Unix/Linux/macOS/WSL
```bash
export PROJECT_ROOT=/path/to/art-universe
```

## Files Created

- `.project-root.env` - Contains the PROJECT_ROOT variable for IntelliJ IDEA (created in project root)
- This file is temporary and can be safely deleted/regenerated
