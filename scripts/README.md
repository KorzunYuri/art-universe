# Scripts Directory

This directory contains utility scripts for the Art Universe project.

## run-module-dev.sh / run-module-dev.bat

Cross-platform scripts for running individual modules locally with proper environment variable loading.

### Usage

```bash
# Unix/Linux/macOS/WSL/Git Bash
./scripts/run-module-dev.sh <module-path> [additional-gradle-args]

# Windows Command Prompt
scripts\run-module-dev.bat <module-path> [additional-gradle-args]
```

### Examples

```bash
./scripts/run-module-dev.sh music:data:raw:lastfm:lastfm-rest-api
./scripts/run-module-dev.sh music:data:master
./scripts/run-module-dev.sh music:quiz --debug
```

### Available Modules

See **[SERVICES.md](../docs/SERVICES.md)** for service ports in development environment.

See **[MODULES.md](../docs/MODULES.md)** for complete list of modules (some of them can be run using this script).

### Environment Variable Loading Order

The script loads environment variables in the following order (later values override earlier ones):

1. **Project root** (REQUIRED): `.project-root.env` - Contains PROJECT_ROOT variable
2. **Common configuration** (optional): `env/docker/common/*.env` - ALL `.env` files in common directory (domain/group-specific shared config)
3. **Local environment** (optional): `env/docker/local/[module-name].env` - Local Docker configuration
4. **Local secrets** (optional): `env/docker/local/[module-name].secrets.env` - Secrets (git-ignored)
5. **Development overrides** (optional): `[module-path]/dev.override.env` - Module-specific dev settings

> **Important**: The `.project-root.env` file is **required**. If missing, the script will fail with an error. Run `./scripts/set-project-root.sh` or `scripts\set-project-root.bat` to create it.

> **Note**: All files in `env/docker/common/` are loaded (e.g., `music-data-raw-lastfm.env`). These contain domain/group-specific variables that are shared across multiple modules.

### Configuration Files Mapping

The script automatically derives the module name from the module path and looks for corresponding configuration files.

**File Patterns**:
- `.project-root.env` (project root) - REQUIRED
- `env/docker/common/*.env` (all .env files in common directory) - Loaded for all modules
- `env/docker/local/<module-name>.env` - Module-specific local config
- `env/docker/local/<module-name>.secrets.env` - Module-specific secrets
- `<module-directory>/dev.override.env` - Module-specific dev overrides

**Examples**:

**Module: `music:data:master`** → Module name: `music-data`
- Project root: `.project-root.env`
- Common: ALL files in `env/docker/common/` (e.g., `music-data-raw-lastfm.env`)
- Local: `env/docker/local/music-data.env`
- Secrets: `env/docker/local/music-data.secrets.env`
- Override: `music/data/master/dev.override.env`

**Module: `music:data:raw:lastfm:lastfm-rest-api`** → Module name: `lastfm-rest-api`
- Project root: `.project-root.env`
- Common: ALL files in `env/docker/common/` (includes `music-data-raw-lastfm.env` with LastFM DB config)
- Local: `env/docker/local/lastfm-rest-api.env`
- Secrets: `env/docker/local/lastfm-rest-api.secrets.env`
- Override: `music/data/raw/lastfm/lastfm-rest-api/dev.override.env`

### Development Override Files

Each module can have a `dev.override.env` file in its directory that overrides Docker configuration for local development.

**Location Pattern**: `<module-directory>/dev.override.env`

These files typically configure:
- Database connections to localhost instead of Docker containers
- Different port numbers to avoid conflicts
- Development-specific settings

### Features

- **Cross-platform compatibility**: Works on Windows, macOS, Linux, WSL, and Git Bash
- **Automatic environment loading**: Loads configuration files in the correct order
- **Error handling**: Provides clear feedback about missing configuration files
- **Spring profile activation**: Automatically sets `spring.profiles.active=dev`
- **PROJECT_ROOT support**: Sets the PROJECT_ROOT environment variable for modules that need it
- **Fixed Windows compatibility**: Proper argument handling and Gradle command formation

### Requirements

- Must be run from the project root directory (where `gradlew` is located)
- `.project-root.env` file must exist (create with `./scripts/set-project-root.sh` or `scripts\set-project-root.bat`)
- Module must have a valid Gradle configuration with `bootRun` task
- At least one configuration file should exist (common, local, secrets, or override)

### Troubleshooting

#### "Script must be run from the project root!"
Make sure you're running the script from the directory containing `gradlew`.

#### "Error: .project-root.env file not found!"
The script requires the `.project-root.env` file to be present in the project root.

**Solution:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./scripts/set-project-root.sh

# Windows Command Prompt
scripts\set-project-root.bat
```

See [PROJECT_ROOT_SETUP.md](PROJECT_ROOT_SETUP.md) for more details.

#### "No common/local .env file found"
This is a warning, not an error. The script will continue with available files. Only `.project-root.env` is required.

#### Module not found
Check that the module path is correct using:
```bash
./gradlew projects
```

#### Port conflicts
If you get port binding errors, make sure no other services are running on the same ports, or modify the port configuration in the `.env` files.

#### Windows-specific issues
- Ensure you're using `scripts\run-module-dev.bat` (not `.sh`) in Command Prompt
- The script automatically handles argument parsing and Gradle command formation
- If you see Gradle task errors, verify the module path matches the Gradle module structure (use `./gradlew projects` to list all modules)
