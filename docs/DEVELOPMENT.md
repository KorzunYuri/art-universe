# Art Universe - Development Guide

## Environment Modes

The Art Universe project supports three different ways to run services, each suited for different development scenarios:

| Mode | Description | Uses Docker?| Config Source| When to Use |
|------|-------------|-------------|--------------|-------------|
| **Dev (IntelliJ)** | Databases in Docker, applications in IntelliJ | DB only| `env/docker/dev/docker-compose.yml` + IntelliJ env files | Working on services, fast iteration, debugging      |
| **Local (Docker)** | Full stack via Docker Compose | Yes (all containerized) | `env/docker/local/docker-compose.yml`| Integration testing, full stack development                 |
| **Production (Docker)** | Production deployment via Docker Compose | Yes (external DB)     | `env/docker/prod/docker-compose.yml`| Actual deployment                                           |

### Why Dev Mode Uses `env/docker/` Files

Despite dev mode not using Docker for services, it reuses the same configuration files from `env/docker/` for **environment parity** - ensuring the same database configs, common settings, and service configurations work consistently across all modes.

The `env/docker/` directory is the **central configuration hub** for ALL environments, not just Docker deployments.

> **See**: [Environment Configuration](../env/docker/README.md) for complete directory structure and file purposes.

### Service Ports

All service ports are documented in **[SERVICES.md](SERVICES.md)** - the single source of truth for service configurations.

## Development Mode

Run services in dev mode with databases and observability tools in Docker, applications in IntelliJ for fast iteration and debugging.

### Prerequisites

1. **Docker running** - Dev stack requires Docker for databases
2. **Java 21** - For running Spring Boot applications
3. **Node.js** (optional) - For UI development

### Starting Development Environment

#### Step 1: Start Docker Stack

Start the dev stack (databases + observability):
```bash
cd env/docker/dev
docker-compose up -d
```

#### Step 2: Run Applications in IntelliJ

Each run configuration loads environment files in a specific order (later files override earlier):
```
1. (if relevant) env/docker/common/{domain/app}.env - Constants (DB name, schema, usernames)
3. env/docker/dev/common.env                        - Dev variables shared across the stack
4. env/docker/dev/{domain/app}.env                  - App/domain specific variables
2. env/docker/dev/{domain/app}.secrets.env          - App/domain specific secrets
```

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
- **Deployment**:
  - **Dev**: Containers via `env/docker/dev/docker-compose.yml`
  - **Local**: Containers via `env/docker/local/docker-compose.yml`
  - **Production**: External host databases
- **Schemas**: `mu_raw_lastfm`, `mu`, `mu_quiz`
- **Migration**: Liquibase with XML changelogs

### Connection Patterns
- HikariCP connection pooling
- Schema-specific users with appropriate permissions
- Automatic schema creation and user setup (Dev & Local)
- Streaming replication configured via docker-compose (Dev & Local)

## IDE Setup

### IntelliJ IDEA
1. Import as Gradle project
2. Configure Java 21 SDK
3. Enable annotation processing for Lombok
4. Run configurations are pre-configured in `intellij.configurations.xml`
5. Use `docker-dev` to start databases, then run individual services

## Related Documentation

- [Environment Configuration](../env/docker/README.md) - Complete env file structure and loading order
- [Services](SERVICES.md) - Service ports and configurations
- [Modules](kb/guides/gradle-commands) - Module listing and build commands
- [Patterns](kb/patterns/README.md) - Development patterns and best practices
