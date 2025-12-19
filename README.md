# Art Universe

Data storage system for art-related information designed to create quizzes. Primary focus: "Guess the Track" game where participants listen to tracks and guess artist/title.

## Quick Start

```bash
# Build and deploy local environment
./gradlew build -x test
./env/docker/deploy.sh local
```

## Architecture

### Services 

**[All services & ports →](docs/SERVICES.md)**

### Module Structure

**[Complete module reference →](docs/MODULES.md)**

### Technology Stack
- **Backend**: Spring Boot 3.4.3, PostgreSQL, Liquibase
- **Frontend**: React, TypeScript, Vite
- **Build**: Gradle multi-project
- **Deployment**: Docker, Docker Compose

## Documentation

- **[Services Reference](docs/SERVICES.md)** - All services, ports, and deployment configurations
- **[Modules Reference](docs/MODULES.md)** - Complete module listing and build commands
- **[Development Guide](docs/DEVELOPMENT.md)** - Complete development workflow
- **[Docker Scripts](env/docker/README.md)** - Deployment guide
- **[Architecture Overview](docs/ARCHITECTURE.md)** - System design and relationships
- **[Development Scripts](scripts/README.md)** - Individual module tools

## Development

- **[Environment Setup →](docs/DEVELOPMENT.md#environment-configuration)** - Local development and Docker deployment
- **[Module Development →](docs/DEVELOPMENT.md#individual-module-development)** - Running specific modules
- **[Build & Test →](docs/DEVELOPMENT.md#build-and-testing)** - Gradle commands and testing
- **[IDE Setup →](docs/DEVELOPMENT.md#ide-setup)** - IntelliJ IDEA and VS Code configuration
