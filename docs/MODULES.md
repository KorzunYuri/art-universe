# Art Universe - Modules Reference

This document is the **single source of truth** for all Gradle modules, their purposes, and build commands.

## Quick Commands

> **Note**: All Gradle commands must be executed from the project root directory.
> 
Use these command templates with any module path from the tables below:

```bash
# Run module in development mode
./scripts/run-module-dev.sh <module-path>

# Build specific module
./gradlew :<module-path>:build

# Test specific module
./gradlew :<module-path>:test

# Build module without tests
./gradlew :<module-path>:build -x test

# Run integration tests only
./gradlew :<module-path>:integrationTest
```

**Example:**
```bash
./scripts/run-module-dev.sh music:data:master
./gradlew :music:data:master:build
./gradlew :music:data:master:test
```

## Module Categories

### Common Modules (Shared Libraries)

| Module | Type | Purpose | Key Dependencies |
|--------|------|---------|------------------|
| `:common:commons-context` | Library | Spring configuration, ObjectMapper auto-configuration | Spring Boot, Jackson |
| `:common:commons-jpa` | Library | JPA utilities, Coded registry, JPA auto-configuration | Spring Data JPA, Hibernate |
| `:common:commons-web` | Library | Web utilities, exception handling, CORS configuration | Spring Web, commons-context, commons-jpa |
| `:common:test:commons-test` | Library | Base testing utilities, test helpers | JUnit 5, Mockito |
| `:common:test:commons-test-web` | Library | Web testing utilities, MockMvc helpers | Spring Test, commons-test |
| `:common:test:commons-test-db` | Library | Database testing utilities, TestContainers setup | TestContainers, commons-test |
| `:common:data:raw:data-raw-commons-jpa` | Library | Common JPA entities and utilities for raw data collection | commons-jpa |
| `:common:data:raw:data-raw-commons-api-client` | Library | Common API client utilities (HTTP, rate limiting) | Spring WebClient |

### LastFM Data Collection Modules

#### Core Data Modules

| Module | Type | Purpose | Key Dependencies |
|--------|------|---------|------------------|
| `:music:data:raw:lastfm:lastfm-models` | Library | JPA entities and DTOs for LastFM data | commons-jpa, data-raw-commons-jpa |
| `:music:data:raw:lastfm:lastfm-repositories` | Library | Data access layer for LastFM entities | Spring Data JPA, lastfm-models |

#### Service Modules

| Module | Type | Purpose |
|--------|------|---------|
| `:music:data:raw:lastfm:lastfm-rest-api` | Service | Read-only REST API for LastFM raw data |
| `:music:data:raw:lastfm:etl:lastfm-etl-rest-api` | Service | Write operations REST API for LastFM ETL |
| `:music:data:raw:lastfm:etl:lastfm-calls-generator` | Service | Generates API calls for data collection |
| `:music:data:raw:lastfm:etl:lastfm-calls-performer` | Service | Executes API calls against LastFM API |
| `:music:data:raw:lastfm:etl:lastfm-response-parser` | Service | Parses and processes API responses |
| `:music:data:raw:lastfm:migrations:lastfm-liquibase-service` | Service | Database migration runner |

#### Infrastructure Modules

| Module | Type | Purpose | Key Dependencies |
|--------|------|---------|------------------|
| `:music:data:raw:lastfm:etl:lastfm-tasks-coordinator` | Library | ETL task coordination and orchestration | Spring Boot |
| `:music:data:raw:lastfm:migrations:lastfm-liquibase-resources` | Library | Liquibase changelog definitions (SQL) | Liquibase |

#### Test Modules

| Module | Type | Purpose | Key Dependencies |
|--------|------|---------|------------------|
| `:music:data:raw:lastfm:test:lastfm-commons-test` | Library | Base testing utilities for LastFM modules | commons-test |
| `:music:data:raw:lastfm:test:lastfm-commons-test-context` | Library | Spring context test configuration | commons-test, Spring Test |
| `:music:data:raw:lastfm:test:lastfm-commons-test-jpa` | Library | JPA testing utilities | commons-test-db, lastfm-models |
| `:music:data:raw:lastfm:test:lastfm-commons-test-db` | Library | Database testing with TestContainers | TestContainers, lastfm-commons-test |
| `:music:data:raw:lastfm:test:lastfm-commons-test-db-helper` | Library | Database consistency helpers (DbConsistencyHelper) | lastfm-commons-test-db |
| `:music:data:raw:lastfm:test:lastfm-commons-test-web` | Library | Web/MVC testing utilities | commons-test-web, lastfm-commons-test |

### Master Data & Quiz Modules

| Module | Type | Purpose |
|--------|------|---------|
| `:music:data:master` | Service | Curated data management and binding service |
| `:music:quiz` | Service | Quiz generation from approved data |
| `:music:ui` | Service | React management interface |

### Messaging Modules (Deferred)

| Module | Type | Purpose | Status |
|--------|------|---------|--------|
| `:messaging:messaging-impl-kafka` | Library | Kafka messaging implementation | Planned - Not currently active |

## Build Patterns

### Build All Modules
```bash
./gradlew build -x test          # Build without tests
./gradlew build                  # Build with tests
./gradlew test                   # Run all tests
./gradlew integrationTest        # Run integration tests only
```

### Build by Category
```bash
# All common modules
./gradlew :common:build

# All LastFM modules
./gradlew :music:data:raw:lastfm:build

# All music modules (master + quiz + ui)
./gradlew :music:build
```

### Test Patterns
```bash
# Run tests excluding integration tests
./gradlew test -PexcludeIntegrationTests

# Run tests for specific module
./gradlew :music:data:master:test

# Run integration tests for specific module
./gradlew :music:data:master:integrationTest
```

## Module Documentation Standards

Each service module should include a README.md following the [Module README Template](MODULE_README_TEMPLATE.md).

## Technology Stack

### Backend (Java Modules)
- **Java**: 21
- **Spring Boot**: 3.4.3
- **Spring Data JPA**: Included in Spring Boot
- **Liquibase**: 4.15.0
- **Lombok**: 1.18.30
- **TestContainers**: 1.20.4
- **PostgreSQL Driver**: Latest (from Spring Boot BOM)

### Frontend (UI Module)
- **React**: 19.1.0
- **TypeScript**: 5.8.3
- **Vite**: 6.3.5
- **React Router**: 6.30.0
- **TanStack Query**: 5.28.0
- **Axios**: 1.12.0

### Build Tools
- **Gradle**: 8.10
- **Gradle Wrapper**: Included (use `./gradlew`)

## See Also

- [SERVICES.md](SERVICES.md) - All services with ports and deployment info
- [DEVELOPMENT.md](DEVELOPMENT.md) - Development workflow and environment setup
- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and design patterns
- [MODULE_README_TEMPLATE.md](MODULE_README_TEMPLATE.md) - Standard template for module documentation
