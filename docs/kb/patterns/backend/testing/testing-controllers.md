# Controller Testing Pattern

## Overview

For each REST controller, we maintain **two types of tests** to ensure comprehensive coverage:

1. **Unit Tests** (`*ControllerTest.java`) - Test controller logic in isolation
2. **MVC Tests** (`*ControllerMvcTest.java`) - Test HTTP endpoints with Spring MVC layer

## Unit Tests

**Purpose**: Verify controller logic without Spring context overhead

**Characteristics**:
- Annotated with `@ExtendWith(MockitoExtension.class)`
- Mock dependencies using `@Mock`
- Inject controller with `@InjectMocks`
- Test controller methods directly (no HTTP requests)
- Use standard JUnit assertions (`assertEquals`, `assertNotNull`, etc.)

**Example**: [LastfmArtistControllerTest.java](../../../../../music/data/raw/lastfm/etl/lastfm-etl-rest-api/src/test/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmArtistControllerTest.java)

## MVC Tests

**Purpose**: Verify HTTP endpoints with full Spring MVC request/response cycle

**Characteristics**:
- Annotated with `@WebMvcTest(ControllerClass.class)`
- Extend `BaseMvcTest` base class
- Use `MockMvc` to perform HTTP requests
- Mock service dependencies with `@MockitoBean`
- Test actual HTTP method, path, request body, response status, and response body

### Strict JSON Assertions

MVC tests use **ObjectMapper-based JSON assertions** to verify complete response structure and values:

```java
// Given
LastfmArtistResponseDto expectedDto = LastfmArtistResponseDto.from(mockArtist);
String expectedJson = objectMapper.writeValueAsString(expectedDto);

// When & Then
mockMvc.perform(patch("/api/v1/artists/{id}/approval", artistId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
    .andExpect(status().isOk())
    .andExpect(content().json(expectedJson));  // Strict comparison
```

**Benefits**:
- Tests entire JSON structure in single assertion
- Catches serialization issues
- Ensures DTOs match API contract
- Type-safe (uses actual DTO classes)

**Example**: [LastfmArtistControllerMvcTest.java](../../../../../music/data/raw/lastfm/etl/lastfm-etl-rest-api/src/test/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmArtistControllerMvcTest.java)

## When to Use Each Test Type

| Test Type | Use When |
|-----------|----------|
| **Unit Test** | Testing controller logic, parameter mapping, error handling without HTTP overhead |
| **MVC Test** | Testing HTTP endpoint behavior, request/response serialization, status codes, full integration |

## Related Patterns

- [Testing Patterns Overview](overview.md) - Test naming conventions and base classes
- [API Conventions](../../api/conventions.md) - REST API conventions and DTO usage
