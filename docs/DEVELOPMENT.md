# Art Universe - Development Guide

## Environment Modes

The Art Universe project supports three different ways to run services, each suited for different development scenarios:

| Mode | Description | Uses Docker?| Config Source| When to Use |
|------|-------------|-------------|--------------|-------------|
| **Dev (IDE/Script)** | Individual services via IDE or `run-module-dev.sh` | DB only| `env/docker/common/`, `env/docker/local/`, `dev.override.env` | Working on a single service, fast iteration, debugging      |
| **Local (Docker)** | Full stack via Docker Compose | Yes (all containerized) | `env/docker/common/`, `env/docker/local/`| Integration testing, full stack development                 |
| **Production (Docker)** | Production deployment via Docker Compose | Yes (external DB)     | `env/docker/common/`, `env/docker/prod/`| Actual deployment                                           |

### Why Dev Mode Uses `env/docker/` Files

Despite dev mode not using Docker for services, it reuses the same configuration files from `env/docker/` for **environment parity** - ensuring the same database configs, common settings, and service configurations work consistently across all modes.

The `env/docker/` directory is the **central configuration hub** for ALL environments, not just Docker deployments.

> **See**: [Environment Configuration](../env/docker/README.md) for complete directory structure and file purposes.

### Service Ports

All service ports are documented in **[SERVICES.md](SERVICES.md)** - the single source of truth for service configurations.

## Individual Module Development (Dev Mode)

Run individual services in dev mode (non-Docker) for fast iteration and debugging.

### Running via IDE

Configure your IDE run configuration to load the environment files:

1. `.project-root.env` See [PROJECT_ROOT_SETUP](../scripts/PROJECT_ROOT_SETUP.md) for details
2. `env/docker/common/*.env` (depends on the module)
3. `env/docker/local/{module-name}.env`
4. `env/docker/local/{module-name}.secrets.env`
5. `{module-path}/dev.override.env`

> **See**: [Environment Configuration](../env/docker/README.md) for complete details on environment file loading and structure.

### Running via Script

```bash
./scripts/run-module-dev.sh <module-path>

# Examples:
./scripts/run-module-dev.sh music:data:master
./scripts/run-module-dev.sh music:quiz
./scripts/run-module-dev.sh music:data:raw:lastfm:lastfm-rest-api
```

**Prerequisites:**
- `.project-root.env` must exist (create with `./scripts/set-project-root.sh` or `scripts\set-project-root.bat`)
- See **[MODULES.md](kb/guides/gradle-commands)** for complete list of module paths

## Docker Deployment (Local & Production Modes)

> **See**: [Docker Scripts](../env/docker/README.md) for complete deployment guide and troubleshooting.

## Build and Testing

> **Important**: All Gradle commands must be executed from the project root directory where the Gradle Wrapper (`gradlew`/`gradlew.bat`) is located.

```bash
# Build all modules
./gradlew build -x test

# Run all tests
./gradlew test

# Run only integration tests
./gradlew integrationTest

# Exclude integration tests
./gradlew test -PexcludeIntegrationTests

# Build specific module
./gradlew :<module-path>:build
```

See **[MODULES.md](kb/guides/gradle-commands)** for module-specific build commands and complete module listing.

## Module Structure

See **[MODULES.md](kb/guides/gradle-commands)** for complete module reference, dependency graph, and build commands.

## Development Patterns

For complete development patterns, see **[patterns reference](kb/patterns/README.md)**.

## Database Configuration

### PostgreSQL Setup
- **Dev & Local(test)**: Containers via Docker Compose
- **Production**: External host databases
- **Schemas**: `mu_raw_lastfm`, `mu`, `mu_quiz`
- **Migration**: Liquibase with XML changelogs

### Connection Patterns
- HikariCP connection pooling
- Schema-specific users with appropriate permissions
- Automatic schema creation and user setup

## IDE Setup

### IntelliJ IDEA
1. Set PROJECT_ROOT environment variable: `./scripts/set-project-root.sh`
2. Import as Gradle project
3. Configure Java 21 SDK
4. Enable annotation processing for Lombok

### VS Code
1. Install Java Extension Pack
2. Install Gradle for Java extension
3. Configure Java 21 in settings
4. Install React/TypeScript extensions for UI module
