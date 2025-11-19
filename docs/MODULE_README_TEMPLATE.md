# [Module Name]

> **Module Path**: `:[module:gradle:path]`
> **Type**: [Service | Library]
> **Status**: [Active | Planned | Deprecated]

Brief description of what this module does and its role in the Art Universe system.

## Purpose

Detailed explanation of:
- What problem this module solves
- When to use this module
- Key responsibilities and features

## Dependencies

### Module Dependencies
List of other Art Universe modules this module depends on:
- `:path:to:dependency:module` - Why this dependency exists

### External Dependencies
Key external libraries (Spring Boot, specialized libraries, etc.):
- `library-name:version` - What it's used for

### Test Reports
- **Location**: `[module-path]/build/reports/tests/test/index.html`
- **Per-class reports**: `[module-path]/build/reports/tests/test/classes/`

## Running (Services Only)

> **Note**: This section applies only to service modules (Spring Boot applications).


### Environment Variables
Environment loading order:
1. `env/docker/local/[service-name].env` - Docker configuration
2. `env/docker/local/[service-name].secrets.env` - Secrets (git-ignored)
3. `[module-path]/dev.override.env` - Development overrides

### Required Environment Variables
| Variable | Description | Example |
|----------|-------------|---------|
| `VARIABLE_NAME` | What it configures | `example-value` |

### Ports
See [SERVICES.md](../../SERVICES.md) for complete port reference.

| Environment | Main Port | Actuator Port |
|-------------|-----------|---------------|
| Development | XXXX      | XXXX          |
| Local       | XXXX      | XXXX          |
| Production  | XXXX      | XXXX          |

## API Endpoints (REST Services Only)

> **Note**: This section applies only to REST API services.

### Base URL
- **Development**: `http://localhost:[dev-port]/api/v1`
- **Local**: `http://localhost:[local-port]/api/v1`
- **Production**: `http://localhost:[prod-port]/api/v1`

### Endpoints

#### [Resource Name]

**GET** `/[resources]`
List resources with pagination.

**Query Parameters:**
- `page` (int, default: 0) - Page number
- `size` (int, default: 20) - Page size
- `sort` (string) - Sort field and direction

**Response:**
```json
{
  "success": true,
  "message": "Resources retrieved successfully",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

**GET** `/[resources]/{id}`
Get single resource by ID.

**Response:**
```json
{
  "success": true,
  "message": "Resource retrieved successfully",
  "data": {
    "id": 1,
    ...
  }
}
```

**POST** `/[resources]`
Create new resource.

**Request Body:**
```json
{
  "field1": "value",
  ...
}
```

**PATCH** `/[resources]/{id}`
Update existing resource.

**DELETE** `/[resources]/{id}`
Delete resource.

### Actuator Endpoints (if actuator port exposed)
- **Health**: `http://localhost:[actuator-port]/actuator/health`
- **Metrics**: `http://localhost:[actuator-port]/actuator/metrics`
- **Prometheus**: `http://localhost:[actuator-port]/actuator/prometheus`

## Database (Modules with Persistence)

> **Note**: This section applies only to modules that interact with a database.

### Schema
- **Database**: `database_name`
- **Schema**: `schema_name`
- **Port (Local)**: XXXX

### Migrations
- **Tool**: Liquibase
- **Changelog Location**: `src/main/resources/db/changelog/`
- **Migration Runner**: [Link to Liquibase service module if separate]

### Key Entities
Brief description of main database entities:
- **EntityName** - Purpose and key fields
- **AnotherEntity** - Purpose and relationships

## Configuration

### Application Properties
Key configuration from `application.yml`:

```yaml
# Example configuration sections
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${DB_URL}
```

### Profiles
- **default** - Standard configuration
- **dev** - Development overrides
- **local** - Local Docker deployment
- **prod** - Production configuration

## Architecture Notes

### Design Patterns
Document any significant architectural decisions:
- Pattern used and why
- Key abstractions
- Important design constraints

### Key Components
Brief overview of main packages and their responsibilities:
- `package.name` - What it contains

## Testing Strategy

### Test Categories
- **Unit Tests** - What they cover, naming conventions
- **Integration Tests** - What they test, required infrastructure
- **Test Utilities** - Available helpers (EntityCreationHelper, DbConsistencyHelper, etc.)

### Test Data
How test data is managed:
- In-memory data setup
- Fixtures or factories used
- Database state management

## Monitoring (Services Only)

> **Note**: This section applies only to service modules.

### Metrics
Key metrics exposed via actuator:
- Custom application metrics
- JVM metrics
- Database connection pool metrics

### Health Checks
Health check endpoints and what they verify:
- Database connectivity
- External service dependencies
- Custom health indicators

## Development Guidelines

### Code Style
- Follow project-wide conventions (see [DEVELOPMENT.md](../../DEVELOPMENT.md))
- Specific conventions for this module (if any)

### Adding Features
Steps for adding new functionality:
1. Update entities/models
2. Add migrations if needed
3. Implement business logic
4. Add tests
5. Update API documentation

## Troubleshooting

### Common Issues

#### Issue 1: [Problem Description]
**Symptom**: What you see when this happens

**Cause**: Why it happens

**Solution**: How to fix it

## See Also

- [SERVICES.md](../../SERVICES.md) - Service ports and deployment
- [MODULES.md](../../MODULES.md) - All modules and build commands
- [DEVELOPMENT.md](../../DEVELOPMENT.md) - Development workflow
- [ARCHITECTURE.md](../../ARCHITECTURE.md) - System architecture
- [Related Module](../path/to/related/README.md) - If applicable

---

> **Template Version**: 1.0
> **Last Updated**: 2025-11-19
