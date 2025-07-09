# Art Universe - Development Guide

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
- **@MockitoBean** is used instead of @MockBean for Spring Boot tests
- **Unit Tests** - Use `@ExtendWith(MockitoExtension.class)` with `@Mock` and `@InjectMocks`
- **MVC Tests** - Use `@WebMvcTest` with `@MockitoBean` for service dependencies
- **JSON Assertions**: Build `expectedJson` with `ObjectMapper` instead of `jsonPath()` assertions
- **Response Validation**: Assert `ResponseWrapper` structure in unit tests
- Integration tests tagged with "integration".
- Test report is placed in {module_root}/build/reports/tests/test/index.html
- Reports for each test class are placed in {module_root}/build/reports/tests/test/classes

### API Standards
- REST endpoints follow `/api/v1/{entity}` pattern
- Consistent `ApiResponse<T>` wrapper for all responses
- CORS configured for development origins

## Environment Configuration

### Local Development
Use `scripts/run-module-dev.sh/.bat <module-path>` to run individual modules:

```bash
# Examples
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
./scripts/run-module-dev.sh music-universe:music-data
./scripts/run-module-dev.sh music-universe:music-quiz
```

**Environment Loading Order:**
1. `env/docker/local/{module-name}.env` - Docker configuration
2. `env/docker/local/{module-name}.secrets.env` - Secrets
3. `{module-path}/dev.override.env` - Development overrides

**Development Ports:**
- LastFM Raw Data: 7081
- Music Data: 7082  
- Music Quiz: 7083
- UI: 5173 (npm run dev)

### Docker Deployment

**Commands:**
```bash
# Deploy environment
./env/docker/deploy.sh <local|prod>

# Stop containers (preserve for restart)
./env/docker/stop.sh <local|prod|all>

# Clean containers and images (preserve volumes)
./env/docker/cleanup.sh <local|prod|all>
```

**Local Environment Ports:**
- LastFM Raw Data: 9081
- Music Data: 9082
- Music Quiz: 9083
- UI: 4000
- Adminer: 9980

**Production Environment Ports:**
- LastFM Raw Data: 8081
- Music Data: 8082
- Music Quiz: 8083
- UI: 3000
- Adminer: 8880

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

## Build and Testing

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
./gradlew :music-universe:music-data:build
```

## Module Structure

### Backend Modules
- `art-universe-commons` - Shared utilities and base classes
- `music-data-raw-lastfm` - LastFM API data collection
- `music-data` - Curated data management and binding
- `music-quiz` - Quiz generation from approved data

### Frontend Module
- `music-universe-ui` - React application for data management

## IDE Setup

### IntelliJ IDEA
1. Set PROJECT_ROOT environment variable: `./scripts/set-project-root.sh`
2. Import as Gradle project
3. Configure Java 17 SDK
4. Enable annotation processing for Lombok

### VS Code
1. Install Java Extension Pack
2. Install Gradle for Java extension
3. Configure Java 17 in settings
4. Install React/TypeScript extensions for UI module
