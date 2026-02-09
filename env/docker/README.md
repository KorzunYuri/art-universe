# Docker Deployment and Environment Configuration

This directory contains cross-platform scripts for Docker deployment AND the **central environment configuration** used by all deployment modes.

> **Important**: Despite the "docker" name, the `env/docker/` directory is the **configuration hub for ALL environments** - including individual module development (non-Docker), Docker Compose local, and production deployments.

## Quick Start

```bash
# Build images and deploy locally (via orchestrator)
./scripts/deploy.sh docker local

# Or call the Docker deploy script directly
./env/docker/deploy.sh local

# Skip image rebuild if images are already built
./scripts/deploy.sh docker local --skip-build

# Start dev infrastructure (database + observability)
cd env/docker/dev
docker-compose up -d
```

## Scripts

Features:

- **Automatic OS Detection**: Scripts detect the operating system and use appropriate commands
- **Gradle Wrapper Detection**: Automatically uses `gradlew` or `gradlew.bat` based on environment
- **Path Resolution**: Handles different path formats across platforms

### Deploy
Deploys the specified environment (local or production):

- Builds Docker images via `scripts/build-images.sh` (unless `--skip-build`)
- Spring Boot services use layered JARs for optimal Docker layer caching

**Usage:**
```bash
./env/docker/deploy.sh <local|prod> [--skip-build]
```

### Stop
Stops containers for the specified environment without removing them.

**Usage:**
```bash
./env/docker/stop.sh <local|prod|all>
```

### Cleanup
Stops and removes containers, images, and networks for the specified environment.

**Usage:**
```bash
./env/docker/cleanup.sh <local|prod|all>
```


## Environment Configuration System

The `env/docker/` directory is the **central configuration hub** for all deployment modes. This organizational structure ensures consistency across development, local testing, and production.

### Why "docker" for non-Docker configs?

Historical/organizational reasons - the directory was initially for Docker Compose only, but evolved into the central config hub for all modes. The name stuck, but the purpose expanded.

**Key principle**: Environment parity - same configs work across all modes (dev/local/prod).

### File Purposes

| Directory | Purpose | Used By |
|-----------|---------|---------|
| **common/** | Domain/group-specific config shared across ALL modes | Dev, Local, Prod |
| **dev/** | Development environment (databases in Docker, apps on host) | Dev (IntelliJ) |
| **local/** | Docker Compose local deployment settings | Local (Docker) |
| **prod/** | Docker Compose production deployment settings | Prod (Docker) |

### How Each Mode Uses These Files

**Dev Mode**
- Infrastructure (database + observability) runs in Docker
- Services run on host via IDE
- Uses ports 7xxx
- See [DEVELOPMENT.md](../../docs/DEVELOPMENT.md) for details

**Local Mode**:
- All services containerized (watch `env/docker/local/docker-compose.yml`)
- Services connect via container names (e.g., `postgres-lastfm-master`)
- Uses ports 9xxx
- `./scripts/deploy.sh docker local` or `./env/docker/deploy.sh local`
- Images are built automatically (or skip with `--skip-build`)

**Production Mode**:
- Databases on Windows host, services in Docker
- Services connect to databases via `host.docker.internal`
- Uses ports 8xxx
- `./scripts/deploy.sh docker prod` or `./env/docker/deploy.sh prod`
- Images are built automatically (or skip with `--skip-build`)

### Configuration Principles

1. **Separation of Concerns**: Each domain has its own .env files
2. **Environment Parity**: Same config structure across all modes
3. **Layering**: Common → Environment-specific (later overrides earlier)
4. **Security**: Secrets in `.secrets.env` files (git-ignored)
5. **No Duplication**: Common constants defined once in `common/*.env`

## Services and Ports

See **[SERVICES.md](../../docs/SERVICES.md)** for complete service listing.

## Troubleshooting

### Build Failures
If the build fails, check:
- Java 21 is installed and available
- Docker is running
- No port conflicts with existing services

### Dev Environment Issues
If applications can't connect to databases:
1. Verify `docker-dev` stack is running: `docker-compose -f env/docker/dev/docker-compose.yml ps`
2. Check database ports are exposed
3. Verify IntelliJ run configurations load correct env files (see above)

### Permission Issues (Linux/macOS)
Make scripts executable:
```bash
chmod +x env/docker/*.sh
```

### WSL Path Issues
The scripts automatically handle WSL path conversion. If you encounter issues, ensure you're running from the project root directory.

### Docker Layer Caching
First build may take longer but subsequent builds should be faster due to Docker layer caching. Spring Boot services use layered JARs for optimal cache utilization.
```bash
# Monitor image sizes
docker images | grep mu-
```

## Related Documentation

- [DEVELOPMENT.md](../../docs/DEVELOPMENT.md) - Development workflow and IntelliJ setup
- [SERVICES.md](../../docs/SERVICES.md) - Complete service listing with ports
- [Kubernetes Deployment](../k8s/README.md) - Alternative deployment using Kustomize (local and prod overlays)
