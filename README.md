# Art Universe

Data storage system for art-related information designed to create quizzes. Primary focus: "Guess the Track" game where participants listen to tracks and guess artist/title.

## Quick Start

### Build and Deploy
```bash
# Build project
./gradlew build -x test

# Deploy local environment (with containerized databases)
./env/docker/deploy.sh local

# Deploy production environment (external databases)
./env/docker/deploy.sh prod
```

### Available Services

| Environment | LastFM Raw | Music Data | Music Quiz | UI | Adminer |
|-------------|------------|------------|------------|----|---------| 
| **Local**   | :9081      | :9082      | :9083      | :4000 | :9980 |
| **Production** | :8081   | :8082      | :8083      | :3000 | :8880 |

### Individual Module Development
```bash
# Run specific modules locally
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
./scripts/run-module-dev.sh music-universe:music-data
./scripts/run-module-dev.sh music-universe:music-quiz
```

## Documentation

- **[Development Guide](DEVELOPMENT.md)** - Setup, configuration, and development workflow
- **[Architecture Overview](ARCHITECTURE.md)** - System design and module relationships
- **[Docker Scripts](env/docker/README.md)** - Deployment and environment management
- **[Development Scripts](scripts/README.md)** - Individual module development

## Module Structure

- `art-universe-commons` - Shared utilities and base classes
- `music-data-raw-lastfm` - LastFM API data collection
- `music-data` - Curated data management and binding
- `music-quiz` - Quiz generation from approved data
- `music-universe-ui` - React management interface

## Technology Stack

- **Backend**: Spring Boot 3.4.3, PostgreSQL, Liquibase
- **Frontend**: React, TypeScript, Vite
- **Build**: Gradle multi-project
- **Deployment**: Docker, Docker Compose
- **Testing**: JUnit 5, TestContainers, Mockito

