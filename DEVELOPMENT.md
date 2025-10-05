# Art Universe - Development Guide

## Environment Configuration

### Service Ports

| Environment | LastFM Read API | LastFM Write API | Music Data | Music Quiz | UI | Adminer |
|-------------|-----------------|------------------|------------|------------|----|---------| 
| **Local**   | :9084           | :9085            | :9082      | :9083      | :4000 | :9980 |
| **Production** | :8084        | :8085            | :8082      | :8083      | :3000 | :8880 |
| **Development** | :7084       | :7085            | :7082      | :7083      | :5173 | :7798 |

### Individual Module Development

Use `scripts/run-module-dev.sh/.bat <module-path>` to run individual modules:

```bash
# LastFM APIs
./scripts/run-module-dev.sh music:data:raw:lastfm:lastfm-rest-api
./scripts/run-module-dev.sh music:data:raw:lastfm:etl:lastfm-etl-rest-api

# Master data and quiz
./scripts/run-module-dev.sh music:data:master
./scripts/run-module-dev.sh music:quiz
```

**Environment Loading Order:**
1. `env/docker/local/{module-name}.env` - Docker configuration
2. `env/docker/local/{module-name}.secrets.env` - Secrets
3. `{module-path}/dev.override.env` - Development overrides

### Docker Deployment

```bash
# Deploy environment
./env/docker/deploy.sh <local|prod>

# Stop containers (preserve for restart)
./env/docker/stop.sh <local|prod|all>

# Clean containers and images (preserve volumes)
./env/docker/cleanup.sh <local|prod|all>
```

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
./gradlew :music:data:master:build
```

## Module Structure

### Common Modules
- `:common:commons-context` - Spring configuration and ObjectMapper auto-configuration
- `:common:commons-jpa` - JPA utilities, Coded registry, auto-configuration
- `:common:commons-web` - Web utilities and exception handling
- `:common:commons-test*` - Testing utilities and base classes
- `:common:data:raw:*` - Common data collection utilities

### LastFM Data Collection
- `:music:data:raw:lastfm:lastfm-models` - JPA entities and DTOs
- `:music:data:raw:lastfm:lastfm-repositories` - Data access layer
- `:music:data:raw:lastfm:lastfm-rest-api` - Read-only REST API
- `:music:data:raw:lastfm:etl:lastfm-calls-generator` - API call generation
- `:music:data:raw:lastfm:etl:lastfm-calls-performer` - API call execution
- `:music:data:raw:lastfm:etl:lastfm-response-parser` - Response processing
- `:music:data:raw:lastfm:etl:lastfm-etl-rest-api` - Write operations REST API
- `:music:data:raw:lastfm:etl:lastfm-tasks-coordinator` - ETL coordination
- `:music:data:raw:lastfm:migrations:*` - Database migrations
- `:music:data:raw:lastfm:test:*` - Testing utilities

### Master Data & Quiz
- `:music:data:master` - Curated data management and binding
- `:music:quiz` - Quiz generation from approved data
- `:music:ui` - React management interface

## Development Patterns

### Entity Architecture
- All entities extend `BaseEntity` with audit timestamps
- Enums implement `Coded` interface for database storage
- Entity relationships managed through JPA annotations
- Liquibase migrations written in SQL

### Testing Conventions
- **Method Naming**:
  - normal tests: `method_shouldBehavior_whenCondition()`, camel case
  - MVC tests: `HTTPMETHOD_path_shouldBehaviour_whenCondition()`, camel case
- **JpaOnlyTest** - test archetype for persistence layer testing with TestContainers
- **EntityCreationHelper** - creates (without persistence) entities with all required fields
- **DbConsistencyHelper** - creates and persists entities in test database
- **@MockitoBean** is used instead of @MockBean for Spring Boot tests
- **Unit Tests** - Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`
- **MVC Tests** - Use `@WebMvcTest` with `@MockitoBean` for service dependencies
- **JSON Assertions**: Build `expectedJson` with `ObjectMapper` instead of `jsonPath()` assertions
- **Response Validation**: Assert `ResponseWrapper` structure in unit tests
- Integration tests tagged with "integration"
- Test report is placed in {module_root}/build/reports/tests/test/index.html
- Reports for each test class are placed in {module_root}/build/reports/tests/test/classes

### API Standards
- REST endpoints follow `/api/v1/{entity}` pattern
- Consistent `ApiResponse<T>` wrapper for all responses
- CORS configured for development origins

## Database Configuration

### PostgreSQL Setup
- **Local Dev**: Containers via Docker Compose
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
