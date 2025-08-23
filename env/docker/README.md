# Docker Management Scripts

This directory contains cross-platform scripts for managing Docker environments for the Art Universe project.

## Scripts

### deploy.sh / deploy.bat
Deploys the specified environment (local or production) with optimized build strategies:

- **Local environment**: Pre-builds the project with Gradle, then uses `Dockerfile.local` for fast container builds
- **Production environment**: Skips Gradle build and uses `Dockerfile.prod` with multi-stage build inside Docker
- Both envs use docker layers caching with the help of Spring Boot layered jar

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/deploy.sh local
./env/docker/deploy.sh prod

# Windows Command Prompt
env\docker\deploy.bat local
env\docker\deploy.bat prod
```

### stop.sh / stop.bat
Stops containers for the specified environment without removing them.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/stop.sh local
./env/docker/stop.sh prod
./env/docker/stop.sh all

# Windows Command Prompt
env\docker\stop.bat local
env\docker\stop.bat prod
env\docker\stop.bat all
```

### cleanup.sh / cleanup.bat
Stops and removes containers, images, and networks for the specified environment.

**Usage:**
```bash
# Unix/Linux/macOS/WSL/Git Bash
./env/docker/cleanup.sh local
./env/docker/cleanup.sh prod
./env/docker/cleanup.sh all

# Windows Command Prompt
env\docker\cleanup.bat local
env\docker\cleanup.bat prod
env\docker\cleanup.bat all
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

| Environment | LastFM Raw | Music Data | Music Quiz | UI | Adminer |
|-------------|------------|------------|------------|----|---------| 
| **Local**   | :9081      | :9082      | :9083      | :4000 | :9980 |
| **Production** | :8081   | :8082      | :8083      | :3000 | :8880 |

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
