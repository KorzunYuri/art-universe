# Docker Management Scripts

This directory contains cross-platform scripts for managing Docker environments for the Art Universe project.

## Scripts

### deploy-local.sh / deploy-local.bat
Deploys the local development environment with all services running in Docker containers with local databases.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/deploy-local.sh

# Windows Command Prompt
env\docker\deploy-local.bat
```

### deploy-prod.sh / deploy-prod.bat
Deploys the production environment where applications run in Docker containers but connect to external databases on the host machine.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/deploy-prod.sh

# Windows Command Prompt
env\docker\deploy-prod.bat
```

### cleanup.sh / cleanup.bat
Stops and removes all Art Universe containers, images, and volumes.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/cleanup.sh

# Windows Command Prompt
env\docker\cleanup.bat
```

## Cross-Platform Compatibility

All scripts are designed to work across different environments:

- **Unix/Linux/macOS**: Use `.sh` scripts
- **Windows WSL**: Use `.sh` scripts (automatically detects WSL)
- **Windows Git Bash**: Use `.sh` scripts (automatically detects Windows)
- **Windows Command Prompt**: Use `.bat` scripts

### Features

- **Automatic OS Detection**: Scripts detect the operating system and use appropriate commands
- **Gradle Wrapper Detection**: Automatically uses `gradlew` or `gradlew.bat` based on environment
- **Path Resolution**: Handles different path formats across platforms
- **Error Handling**: Proper error checking and user feedback
- **Colored Output**: Uses emojis and formatting for better user experience

## Environment Configuration

The environment files have been separated to ensure proper isolation between services:

- `env/docker/local/music-data.env` - Configuration for music-data service in local environment
- `env/docker/local/music-quiz.env` - Configuration for music-quiz service in local environment
- `env/docker/prod/music-data.env` - Configuration for music-data service in production environment
- `env/docker/prod/music-quiz.env` - Configuration for music-quiz service in production environment

This separation ensures that music-data and music-quiz services don't have knowledge of each other's configuration details.

## Services and Ports

After successful deployment, the following services will be available:

| Service | Local Port | Description |
|---------|------------|-------------|
| LastFM Raw Data | 8081 | Raw data collection from LastFM API |
| Music Data | 8082 | Curated music data management |
| Music Quiz | 8083 | Quiz generation service |
| UI | 3000 | React frontend application |
| Adminer | 9980 | Database administration tool |

## Troubleshooting

### Build Failures
If the build fails, check:
- Java 17 is installed and available
- Docker is running
- No port conflicts with existing services

### Permission Issues (Linux/macOS)
Make scripts executable:
```bash
chmod +x env/docker/*.sh
```

### WSL Path Issues
The scripts automatically handle WSL path conversion. If you encounter issues, ensure you're running from the project root directory.
