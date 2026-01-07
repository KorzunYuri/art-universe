# Standard Project Structure

## Purpose

Standard directory structure for Spring Boot modules in the Art Universe project.

## When to Use

- Creating new Spring Boot module
- Understanding existing module organization
- Finding files in a module
- Using Glob/Grep tools to search code

---

## Directory Mapping

| Component | Location | Glob Pattern | Base Class/Convention |
|-----------|----------|--------------|----------------------|
| **Controllers** | `src/main/java/**/controller/` | `**/controller/**/*.java` | `@RestController`, extends base MVC test |
| **Services** | `src/main/java/**/service/` | `**/service/**/*.java` | `@Service` |
| **Entities** | `src/main/java/**/entity/` | `**/entity/**/*.java` | Extend `BaseEntity` |
| **Repositories** | `src/main/java/**/repository/` | `**/repository/**/*.java` | `@Repository`, extend `JpaRepository` |
| **DTOs** | `src/main/java/**/dto/` | `**/dto/**/*.java` | Request/response objects |
| **Config** | `src/main/java/**/config/` | `**/config/**/*.java` | `@Configuration` classes |
| **Main Class** | `src/main/java/**/*Application.java` | `**/*Application.java` | `@SpringBootApplication` |
| **Application Config** | `src/main/resources/application*.yml` | `**/application*.yml` | Spring profiles |
| **Liquibase Migrations** | `src/main/resources/db/migration/` | `**/db/migration/**/*.sql`, `**/*.yaml` | Database migrations |
| **Controller Tests** | `src/test/java/**/controller/` | `**/src/test/**/controller/**/*.java` | Extend `BaseMvcTest` |
| **Service Tests** | `src/test/java/**/service/` | `**/src/test/**/service/**/*.java` | Use `@MockitoBean` |
| **Repository Tests** | `src/test/java/**/repository/` | `**/src/test/**/repository/**/*.java` | Extend `BaseJpaTest` |
| **Test Utilities** | `src/test/java/**/common/` | `**/src/test/**/common/**/*.java` | Fixtures, helpers |
| **Test Config** | `src/test/resources/application-test.yml` | `**/src/test/**/application-test.yml` | Test-specific config |
| **Build Config** | `build.gradle` | `**/build.gradle` | Gradle configuration |
| **Docker** | `Dockerfile*` | `**/Dockerfile*` | Only for runnable services |

---

## Naming Conventions

### Package Names
**Format**: `yurykorzun.art.universe.{domain}.{subdomain}.{module}`

**Examples**:
- Music Data Master: `yurykorzun.art.universe.music.data.master`
- Music Quiz: `yurykorzun.art.universe.music.quiz`
- LastFM Calls Generator: `yurykorzun.art.universe.music.data.raw.lastfm.etl.callsgenerator`

### Main Application Class
**Format**: `{ModuleName}Application.java`

**Location**: Root of module package

**Examples**:
- `MusicDataMasterApplication.java`
- `MusicQuizApplication.java`
- `LastfmCallsGeneratorApplication.java`

### Test Class Names
- Repository tests: `{Entity}RepositoryTest.java`
- Service tests: `{Service}Test.java`
- Controller tests: `{Controller}MvcTest.java`

See [Testing Patterns](../testing/overview.md) for complete conventions.

---

## Configuration Files

### Spring Profile Files

| File | Purpose | When Loaded |
|------|---------|-------------|
| `application.yml` | Base configuration with placeholders | Always (all profiles) |
| `application-dev.yml` | Development overrides | Dev mode only |
| `application-local.yml` | Local Docker overrides | Local Docker deployment |
| `application-prod.yml` | Production overrides | Production deployment |
| `application-test.yml` | Test configuration | During tests |

**Configuration loading order**: Base → Profile-specific → Environment variables

See [Environment Profiles Configuration](configuration/environment-profiles.md) for details.

---

## Examples from Actual Codebase

### Music Data Master (`:music:data:master`)

```
Main class:
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/MusicDataMasterApplication.java

Controllers:
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/controller/ArtistController.java
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/controller/AlbumController.java

Entities:
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Artist.java
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/Album.java

Services:
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/service/ArtistService.java

Repositories:
music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/repository/ArtistRepository.java

Tests:
music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/controller/ArtistControllerMvcTest.java
music/data/master/src/test/java/yurykorzun/art/universe/music/data/master/repository/ArtistRepositoryTest.java
```

### Music Quiz (`:music:quiz`)

```
Main class:
music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/MusicQuizApplication.java

Controllers:
music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/controller/GameController.java

Entities:
music/quiz/src/main/java/yurykorzun/art/universe/music/quiz/entity/PipelineExecution.java
```

### LastFM Response Parser (`:music:data:raw:lastfm:etl:lastfm-response-parser`)

```
Main class:
music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/responseparser/LastfmResponseParserApplication.java

Services:
music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/attribute/LastfmAttributeHistoryService.java
```

---

## Module Type Differences

### Runnable Service Modules
**Have**:
- `*Application.java` main class
- `Dockerfile` for containerization
- `application.yml` with server port configuration
- Typically include `controller/` package

**Examples**: `:music:data:master`, `:music:quiz`, `:music:data:raw:lastfm:lastfm-rest-api`

### Library Modules
**Do NOT have**:
- No `*Application.java`
- No `Dockerfile`
- No server configuration

**Have**:
- Reusable classes (entities, repositories, utilities)
- Spring auto-configuration classes (optional)

**Examples**: `:common:commons-jpa`, `:music:data:raw:lastfm:lastfm-models`

---

## Key Rules

### Source Code
- All entities MUST extend `BaseEntity`
- All repositories MUST extend `JpaRepository`
- Controllers MUST use `@RestController` and `@RequestMapping("/api/v1/{entity}")`
- Services MUST use `@Service`
- Use constructor injection (not `@Autowired` on fields)

### Testing
- Repository tests MUST extend appropriate `BaseJpaTest`
- Controller tests MUST extend appropriate `BaseMvcTest`
- Service tests use `@MockitoBean` for dependencies
- Test naming: `method_shouldBehavior_whenCondition()`
- MVC test naming: `HTTPMETHOD_path_shouldBehavior_whenCondition()`

### Configuration
- Use placeholders in `application.yml`: `${ENV_VAR}`
- Profile-specific files override base configuration
- Never commit secrets (use `*.secrets.env` files)

---

## Build Configuration

### Standard Dependencies (Most Modules)

```gradle
dependencies {
    // Common libraries
    implementation project(':common:commons-context')
    implementation project(':common:commons-jpa')
    implementation project(':common:commons-web')

    // Test dependencies
    testImplementation project(':common:test:commons-test')
    testImplementation project(':common:test:commons-test-web')
    testImplementation project(':common:test:commons-test-db')
}
```

### Only for Library Modules
```gradle
// No Spring Boot plugin
plugins {
    id 'java'
}
```

### Only for Service Modules
```gradle
// Include Spring Boot plugin
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.3'
}
```

---

## See Also

- **[Gradle Commands](build/gradle-commands.md)** - Building and testing modules
- **[Entity Patterns](entities/overview.md)** - Entity design patterns
- **[API Conventions](api/conventions.md)** - Controller and REST patterns
- **[Testing Patterns](testing/overview.md)** - Test organization and naming
- **[Liquibase Pattern](database/liquibase.md)** - Database migrations
- **[Environment Profiles](configuration/environment-profiles.md)** - Configuration management
- **[Project Modules Index](../../../MODULES.md)** - All modules listing
