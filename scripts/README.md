# Scripts Directory

This directory contains utility scripts for the Art Universe project.

## run-module-dev.sh / run-module-dev.bat

Cross-platform scripts for running individual modules locally with proper environment variable loading.

> **Note**: For development, **IntelliJ IDEA run configurations are preferred** (see [DEVELOPMENT.md](../docs/DEVELOPMENT.md)). These scripts are provided for edge cases or non-IntelliJ workflows.

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

1. **Common configuration**: `env/docker/common/*.env` - Domain/group-specific shared config (DB names, schemas)
3. **Dev common**: `env/docker/dev/common.env` - Dev common settings
4. **Dev overrides**: `env/docker/dev/[module-name].env` - Dev-specific variables (localhost hosts, external ports)
2. **Local secrets**: `env/docker/dev/[module-name].secrets.env` - Secrets (git-ignored)

> **Note**: The loading order matches IntelliJ run configurations. See [env/docker/README.md](../env/docker/README.md) for complete details.

### Configuration Files Mapping

The script automatically derives the module name from the module path and looks for corresponding configuration files.

**File Patterns**:
- `env/docker/common/*.env` - Loaded for all modules (constants)
- `env/docker/local/<module-name>.secrets.env` - Module-specific secrets
- `env/docker/dev/common.env` - Dev common settings
- `env/docker/dev/<module-name>.env` - Module-specific dev overrides

**Examples**:

**Module: `music:data:master`** → Module name: `music-data`
1. Common: ALL files in `env/docker/common/`
2. Secrets: `env/docker/local/music-data.secrets.env`
3. Dev common: `env/docker/dev/common.env`
4. Dev overrides: `env/docker/dev/music-data.env`

**Module: `music:data:raw:lastfm:lastfm-rest-api`** → Module name: `lastfm-rest-api`
1. Common: `env/docker/common/music-data-raw-lastfm.env` (DB config)
2. Secrets: `env/docker/local/music-data-raw-lastfm.secrets.env`
3. Dev common: `env/docker/dev/common.env`
4. Dev overrides: `env/docker/dev/music-data-raw-lastfm.env`

### Prerequisites

Before running modules in dev mode:

1. **Start dev stack** (databases + observability in Docker):
   ```bash
   cd env/docker/dev
   docker-compose up -d
   ```
   Or use IntelliJ run configuration: `docker-dev`

2. **Run migrations** (first time or after schema changes):
   ```bash
   cd music/data/raw/lastfm/migrations/lastfm-liquibase-service
   ./gradlew bootRun
   ```
   Or use IntelliJ run configuration: `LiquibaseMigrationService - DEV`

The dev stack provides:
- LastFM Master Database: `localhost:7799`
- LastFM Replica Database: `localhost:7798`
- MU Data Database: `localhost:7789`
- Prometheus: `localhost:7090`
- Grafana: `localhost:7000`
- Zipkin: `localhost:7411`

### Features

- **Cross-platform compatibility**: Works on Windows, macOS, Linux, WSL, and Git Bash
- **Automatic environment loading**: Loads configuration files in the correct order matching IntelliJ
- **Error handling**: Provides clear feedback about missing configuration files
- **Spring profile activation**: Automatically sets `spring.profiles.active=dev`

### Requirements

- Must be run from the project root directory (where `gradlew` is located)
- Dev stack must be running (databases in Docker)
- Module must have a valid Gradle configuration with `bootRun` task
- At least one configuration file should exist (common, secrets, or dev)

## set-project-root.sh / set-project-root.bat

**DEPRECATED**: The PROJECT_ROOT environment variable is no longer required for modules. This script is kept for backward compatibility but is not actively used.

Environment configurations now use relative paths from `env/docker/` or are automatically resolved by Gradle.

## Related Documentation

- [DEVELOPMENT.md](../docs/DEVELOPMENT.md) - Complete development guide with IntelliJ setup
- [env/docker/README.md](../env/docker/README.md) - Environment configuration structure and loading order
- [SERVICES.md](../docs/SERVICES.md) - Service ports and configurations
- [MODULES.md](../docs/MODULES.md) - Module listing and build commands
