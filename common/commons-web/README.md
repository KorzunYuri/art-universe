# Commons Web

The module provides common web/REST utilities and standardized exception handling for Spring Boot REST APIs.

## Key Components

### Custom Exception Types

**Location**: `exception/`

| Exception | HTTP Status | Purpose |
|-----------|-------------|---------|
| `CustomEntityNotFoundException` | 404 | Entity not found errors |
| `DataFetchException` | 500 | Data retrieval failures |
| `DataUpdateException` | 500 | Data write/update failures |
| `ExposedException` | 500 | Exceptions safe to expose to clients |

### Consistent Exception Handling

**Purpose**: Centralized @RestControllerAdvice for handling exceptions across all REST controllers with standardized error responses

**Location**: `exception/CommonGlobalExceptionHandler.java`

### ErrorResponse

**Purpose**: Standard error response DTO with timestamp, status, message, and request path

**Location**: `exception/ErrorResponse.java`

### Configuration for importing

**Purpose**: single-shot importing of common web-related Spring beans

**Class**: `config/CommonWebConfig.java`

**How to use**: Add @Import annotation with `CommonWebConfig.java` to Spring @Configuration class.

## Documentation

**For detailed exception handling patterns**: See [exception-handling.md](./docs/exception-handling.md)

**For web configuration details**: See [configuration.md](./docs/configuration.md)

**For module-specific extension examples**: See [extending-handler.md](./docs/extending-handler.md)
