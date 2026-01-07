# Docker Deployment and Environment Configuration

This directory contains cross-platform scripts for Docker deployment AND the **central environment configuration** used by all deployment modes.

> **Important**: Despite the "docker" name, the `env/docker/` directory is the **configuration hub for ALL environments** - including individual module development (non-Docker), Docker Compose local, and production deployments.

## Quick Start

```bash
# Deploy full stack locally (Docker Compose)
./gradlew build -x test
./env/docker/deploy.sh local

# Run individual service in dev mode (non-Docker)
./scripts/run-module-dev.sh music:data:master
```

## Scripts

Features:

- **Automatic OS Detection**: Scripts detect the operating system and use appropriate commands
- **Gradle Wrapper Detection**: Automatically uses `gradlew` or `gradlew.bat` based on environment
- **Path Resolution**: Handles different path formats across platforms

### Deploy
Deploys the specified environment (local or production) with optimized build strategies:

- **Local environment**: Pre-builds the project with Gradle, then uses `Dockerfile.local` for fast container builds
- **Production environment**: Skips Gradle build and uses `Dockerfile.prod` with multi-stage build inside Docker
- Both envs use docker layers caching with the help of Spring Boot layered jar

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/deploy.sh <local|prod>

# Windows Command Prompt
env\docker\deploy.bat <local|prod>
```

### Stop
Stops containers for the specified environment without removing them.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/stop.sh <local|prod>

# Windows Command Prompt
env\docker\stop.bat <local|prod>
```

### Cleanup
Stops and removes containers, images, and networks for the specified environment.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/cleanup.sh <local|prod|all>

# Windows Command Prompt
env\docker\cleanup.bat <local|prod|all>
```


## Environment Configuration System

The `env/docker/` directory is the **central configuration hub** for all deployment modes. This organizational structure ensures consistency across development, local testing, and production.

### Why "docker" for non-Docker configs?

Historical/organizational reasons - the directory was initially for Docker Compose only, but evolved into the central config hub for all modes. The name stuck, but the purpose expanded.

**Key principle**: Environment parity - same configs work across all modes (dev/local/prod).

### File Purposes

| Directory | Purpose | Used By | Example Variables |
|-----------|---------|---------|-------------------|
| **common/** | Domain/group-specific config shared across ALL modes | Dev, Local, Prod | `MURAW_LASTFM_DB_SCHEMA=mu_raw_lastfm` |
| **local/** | Docker Compose local deployment settings | Local (Docker), Dev (as base) | `MU_DATA_APP_EXTERNAL_PORT=9082` |
| **prod/** | Docker Compose production deployment settings | Prod (Docker) | `MU_DATA_APP_EXTERNAL_PORT=8082` |
| **dev/** | Development monitoring settings | Dev (non-Docker) | `PROMETHEUS_PORT=7090` |
| **Module dev.override.env** | Module-specific overrides for dev mode | Dev (non-Docker) | `MU_DATA_APP_EXTERNAL_PORT=7082` |

### How Each Mode Uses These Files

**Dev Mode (Individual services via IDE or script):**
- Loads: `common/*.env` → `local/*.env` → `local/*.secrets.env` → module `dev.override.env`
- Services run on host (not containerized)
- Uses ports 7xxx
- See [DEVELOPMENT.md](../../docs/DEVELOPMENT.md) for details

**Local Mode (Docker Compose):**
- Loads: `common/*.env` → `local/*.env` → `local/*.secrets.env`
- All services containerized
- Uses ports 9xxx
- Uses Docker Compose with `local/docker-compose.yml`

**Production Mode (Docker Compose):**
- Loads: `common/*.env` → `prod/*.env` → `prod/*.secrets.env`
- All services containerized, external databases
- Uses ports 8xxx
- Uses Docker Compose with `prod/docker-compose.yml`

### Configuration Principles

1. **Separation of Concerns**: Services don't see each other's configs (separate .env files)
2. **Environment Parity**: Same config structure across all modes
3. **Layering**: Common → Environment-specific → Module-specific (later overrides earlier)
4. **Security**: Secrets in `.secrets.env` files (git-ignored)

## Services and Ports

After successful deployment, services will be available at configured ports.

See **[SERVICES.md](../../docs/SERVICES.md)** for complete service listing with all ports (main and actuator).

## Troubleshooting

### Build Failures
If the build fails, check:
- Java 21 is installed and available
- Docker is running
- No port conflicts with existing services

### Permission Issues (Linux/macOS)
Make scripts executable:
```bash
chmod +x env/docker/*.sh
```

### WSL Path Issues
The scripts automatically handle WSL path conversion. If you encounter issues, ensure you're running from the project root directory.

### Docker Layer Caching
For production builds, first build may take longer but subsequent builds should be faster due to layer caching. To verify layers:
```bash
# Check if layered JAR is properly configured
java -Djarmode=layered -jar build/libs/mu-data-*.jar list

# Monitor image sizes
docker images | grep mu-
```
