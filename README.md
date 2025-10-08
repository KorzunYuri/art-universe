# Art Universe

Data storage system for art-related information designed to create quizzes. Primary focus: "Guess the Track" game where participants listen to tracks and guess artist/title.

## Quick Start

```bash
# Build and deploy local environment
./gradlew build -x test
./env/docker/deploy.sh local
```

**Services:** LastFM APIs (:9084/:9085) • Music Data (:9082) • Quiz (:9083) • UI (:4000) • [Full port reference →](DEVELOPMENT.md#service-ports)

## Development

- **[Environment Setup →](DEVELOPMENT.md#environment-configuration)** - Local development and Docker deployment
- **[Module Development →](DEVELOPMENT.md#individual-module-development)** - Running specific modules
- **[Build & Test →](DEVELOPMENT.md#build-and-testing)** - Gradle commands and testing
- **[IDE Setup →](DEVELOPMENT.md#ide-setup)** - IntelliJ IDEA and VS Code configuration

## Architecture

### Module Structure
- **Common** - Shared utilities, JPA, web components
- **LastFM Collection** - API data collection with read/write separation and ETL pipeline
- **Master Data** - Curated data management and binding
- **Quiz** - Quiz generation from approved data
- **UI** - React management interface

**[Detailed module descriptions →](DEVELOPMENT.md#module-structure)**

### Technology Stack
- **Backend**: Spring Boot 3.4.3, PostgreSQL, Liquibase
- **Frontend**: React, TypeScript, Vite
- **Build**: Gradle multi-project
- **Deployment**: Docker, Docker Compose

## Documentation

- **[Development Guide](DEVELOPMENT.md)** - Complete development workflow
- **[Architecture Overview](ARCHITECTURE.md)** - System design and relationships
- **[Docker Scripts](env/docker/README.md)** - Deployment management
- **[Development Scripts](scripts/README.md)** - Individual module tools

---

> **Note**: This is a Gradle multi-project build. All commands must be executed from the project root directory.

