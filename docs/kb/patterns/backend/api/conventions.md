# REST API Conventions

## Purpose

Standard patterns and conventions for building REST APIs in Spring Boot modules across the Art Universe project.

## When to Use

- Creating new REST controllers
- Adding new API endpoints
- Reviewing API design
- Ensuring consistency across services

---

## API Endpoint Structure

### Naming Convention

All REST endpoints follow this pattern:

```
/api/v1/{entity}
```

**Examples**:
- `/api/v1/artists`
- `/api/v1/albums`
- `/api/v1/tracks`
- `/api/v1/bindings`

### Versioning

- API version is included in the path (`/v1/`)
- Allows for future API evolution without breaking existing clients
- Currently all services use `v1`

---

## Controller Annotations

### Required Annotations

```java
@RestController              // Marks class as REST controller
@RequestMapping("/api/v1/artists")  // Base path for all endpoints
```

### Optional Annotations

```java
@Validated                   // Enable method-level validation
@CrossOrigin                 // Configure CORS (if needed per-controller)
```

---

## HTTP Method Conventions

| HTTP Method | Endpoint | Purpose | Success Status | Not Found Status |
|-------------|----------|---------|----------------|------------------|
| `GET` | `/{entity}` | Get all entities | 200 OK | - |
| `GET` | `/{entity}/{id}` | Get one entity | 200 OK | 404 Not Found |
| `POST` | `/{entity}` | Create new entity | 201 Created | - |
| `PUT` | `/{entity}/{id}` | Update entity | 200 OK | 404 Not Found |
| `DELETE` | `/{entity}/{id}` | Delete entity | 204 No Content | 404 Not Found |

---

## Request/Response Patterns

### Using DTOs

**Always use DTOs** (Data Transfer Objects) for request/response bodies, not entities:

```java
// ✅ Good
@PostMapping
public ResponseEntity<ArtistDto> createArtist(@RequestBody ArtistDto artistDto)

// ❌ Bad
@PostMapping
public ResponseEntity<Artist> createArtist(@RequestBody Artist artist)
```

### Validation

Use Bean Validation annotations on DTOs:

```java
public class ArtistDto {

    @NotNull(message = "Artist name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotNull(message = "Category is required")
    private Category category;
}
```

Enable validation with `@Valid`:

```java
@PostMapping
public ResponseEntity<ArtistDto> createArtist(@RequestBody @Valid ArtistDto artistDto) {
    // Validation automatically enforced
}
```

---

## ResponseEntity Usage

### Return Appropriate Status Codes

```java
// 200 OK - Successful GET, PUT
return ResponseEntity.ok(data);

// 201 Created - Successful POST
return ResponseEntity.status(HttpStatus.CREATED).body(created);

// 204 No Content - Successful DELETE
return ResponseEntity.noContent().build();

// 404 Not Found - Entity not found
return ResponseEntity.notFound().build();

// 400 Bad Request - Validation error (handled by exception handler)
// Thrown automatically by @Valid annotation
```

### Optional Pattern

Use `Optional` for single-entity lookups:

```java
@GetMapping("/{id}")
public ResponseEntity<ArtistDto> getArtist(@PathVariable Long id) {
    return artistService.findById(id)
        .map(ResponseEntity::ok)                    // 200 if present
        .orElse(ResponseEntity.notFound().build()); // 404 if absent
}
```

---

## Dependency Injection

### Constructor Injection (Preferred)

```java
@RestController
public class ArtistController {

    private final ArtistService artistService;

    // Constructor injection - preferred
    public ArtistController(ArtistService artistService) {
        this.artistService = artistService;
    }
}
```

**Benefits**:
- Immutable dependencies (final fields)
- Easier to test (can inject mocks in tests)
- No reflection magic at runtime
- Explicit dependencies

---

## CORS Configuration

CORS is **enabled** across all services in the Art Universe project.

**Global configuration** is typically handled in Spring configuration classes:

```java
@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
```

---

## Exception Handling

Controllers should **not** handle exceptions directly. Use global exception handlers.

**Common exceptions**:
- `EntityNotFoundException` → 404 Not Found
- `ValidationException` → 400 Bad Request
- `IllegalArgumentException` → 400 Bad Request

Exception handlers are typically imported via base test classes (e.g., `BaseMvcTest`).

---

## Health Check Endpoints

All services expose health check endpoints:

```
/health
```

Provided by Spring Boot Actuator.

---

## Examples in Codebase

### Music Data (Master) Module

Controllers:
```
music/data/master/src/main/java/**/controller/
```

Example:
```
ArtistController.java
AlbumController.java
TrackController.java
BindingController.java
```

### LastFM REST API Module

Controllers:
```
music/data/raw/lastfm/lastfm-rest-api/src/main/java/**/controller/
```

---

## Quick Reference Table

| Pattern | Convention |
|---------|------------|
| **Base path** | `/api/v1/{entity}` |
| **Annotation** | `@RestController` + `@RequestMapping` |
| **Dependency injection** | Constructor injection (final fields) |
| **Request/Response** | Use DTOs, not entities |
| **Validation** | `@Valid` on request body |
| **Status codes** | 200 OK, 201 Created, 204 No Content, 404 Not Found |
| **Optional** | Use `.map()` / `.orElse()` pattern |
| **CORS** | Enabled globally |
| **Exception handling** | Global handlers (not in controllers) |

---

## See Also

- **[Entity Patterns Overview](../entities/overview.md)** - Entity design patterns
- **[Testing Patterns Overview](../testing/overview.md)** - Testing API endpoints
- **[Gradle Commands](../build/gradle-commands.md)** - Building and running services
- **[Architecture Overview](../../../guides/architecture-overview.md)** - System architecture
