# Backend Testing Patterns - Index

**Purpose**: Quick reference guide for backend testing patterns in the Art Universe project.

This document provides an overview of testing conventions and patterns used across Spring Boot modules. For detailed deep-dives, see individual pattern files.

---

## Test Categories

| Test Type | Purpose | Base Class | Speed |
|-----------|---------|------------|-------|
| **Repository Tests** | Test JPA entities and database operations | `BaseMasterDataJpaTest` or `BaseJpaTest` | Fast (TestContainers) |
| **Service Tests** | Test business logic | No base class (use Mockito) | Very Fast (no DB) |
| **Controller Tests** | Test REST API endpoints | `BaseMasterDataMvcTest` or `BaseMvcTest` | Fast (MockMvc) |
| **Integration Tests** | Test full application stack | `@SpringBootTest` | Slower (full context) |

---

## Test Base Classes

### JPA/Repository Tests

For testing with persistence layer see [Testing With Persistence Layer](testing-with-persistence-layer.md)

### Controller Tests

For testing controllers (Unit & Integration) see [Testing Controllers](testing-controllers.md)

### Service Tests

- Unit tests for services typically don't need a base class
- Integration tests might require extending a base class for persistence layer bootstrapping - see [Testing With Persistence Layer](testing-with-persistence-layer.md)


## Testing Conventions

### Test Naming

**Unit tests** (service/logic):
```
method_shouldBehavior_whenCondition()
```

Example:
```java
void createArtist_shouldSaveArtist_whenValidData() { }
void createArtist_shouldThrowException_whenNameIsNull() { }
```

**MVC tests** (controllers):
```
HTTPMETHOD_path_shouldBehavior_whenCondition()
```

Example:
```java
void GET_artists_shouldReturnList_whenArtistsExist() { }
void POST_artists_shouldReturn201_whenValidArtist() { }
void GET_artistsById_shouldReturn404_whenArtistNotFound() { }
```

### Mockito Usage

**Use `@MockitoBean` instead of `@MockBean`**:

```java
@MockitoBean
private ArtistRepository artistRepository;
```

**For mocking nullable types**:

```java
when(repository.findById(1L)).thenReturn(nullable(Artist.class));
```

### Integration Test Tagging

Mark integration tests with `"integration"` tag:

```java
@Tag("integration")
@SpringBootTest
class ArtistIntegrationTest {
    // Full integration test
}
```

This allows running unit tests separately from integration tests.


## Running Tests

See [Gradle Commands](../../../guides/gradle-commands.md#test-patterns) for complete command reference.


## TestContainers

The project uses **TestContainers** for PostgreSQL in repository tests. See [Testing With Persistence Layer](testing-with-persistence-layer.md).

**Benefits**:
- Real PostgreSQL database (not embedded H2)
- Tests run against same DB as production
- Automatic container lifecycle management
- Isolated test environment

**Configuration**:
- Base test classes handle TestContainers setup
- PostgreSQL 14 container is used
- Containers are reused within test session


## Common Test Dependencies

Modules typically depend on test commons:

- **`commons-test`** - General test utilities
- **`commons-test-web`** - Web/MVC test utilities
- **`commons-test-db`** - Database test utilities (TestContainers)

Check `build.gradle` for test dependencies.


## Examples in Codebase

**Repository test**: music/data/master/src/test/java/**/repository/*RepositoryTest.java
**Controller test**: music/data/master/src/test/java/**/controller/*ControllerTest.java
**Service test**: music/data/master/src/test/java/**/service/*ServiceTest.java


## See Also

- **[Gradle Commands](../../../guides/gradle-commands.md)** - Running tests with Gradle
- **[Entity Patterns Overview](../entities/overview.md)** - Entity patterns being tested
- **[API Conventions](../api/conventions.md)** - API conventions for controller tests
- **[Development Tasks Guide](../../../guides/development-tasks.md)** - Development workflows including testing
