# Commons Web

The module provides a Spring Boot auto-configuration with common web/REST beans and utilities.

## Key Components

### Common Jackson serialization rules

**Purpose**: Consistent JSON serialization in REST APIs across the project

**Location**: 
- `config/CommonWebConfig.java` - configuration
- `src/textFixtures/java/yurykorzun/art/universe/common/web/config/WebSerializationTest.java` - text fixture for validation of following serialization rules in consumer modules
- serialization rules are taken from `common:commons-context` module

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
