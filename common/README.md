# Common Modules

This directory contains documentation for shared library modules used across the Art Universe project.

## Module Index

### Core Common Modules

| Module | Gradle Path | Purpose | README |
|--------|-------------|---------|--------|
| Commons Context | `:common:commons-context` | Base utilities and common Spring configuration | [README.md](commons-context/README.md) |
| Commons JPA | `:common:commons-jpa` | JPA utilities, BaseEntity, common entity patterns | [README.md](commons-jpa/README.md) |
| Commons Web | `:common:commons-web` | Web/REST utilities and common controllers | [README.md](commons-web/README.md) |

### Raw Data Common Modules

| Module | Gradle Path | Purpose | README |
|--------|-------------|---------|--------|
| Data Raw Commons JPA | `:common:data:raw:data-raw-commons-jpa` | Raw data JPA commons | [README.md](data/raw/data-raw-commons-jpa/README.md) |
| Data Raw Commons API Client | `:common:data:raw:data-raw-commons-api-client` | API client commons | [README.md](data/raw/data-raw-commons-api-client/README.md) |

### Test Support Modules

| Module | Gradle Path | Purpose | README |
|--------|-------------|---------|--------|
| Commons Test | `:common:test:commons-test` | General test utilities | [README.md](test/commons-test/README.md) |
| Commons Test Web | `:common:test:commons-test-web` | Web/MVC test utilities | [README.md](test/commons-test-web/README.md) |
| Commons Test DB | `:common:test:commons-test-db` | Database test utilities (TestContainers) | [README.md](test/commons-test-db/README.md) |

## Module Characteristics

**Common modules are**:
- Library modules (not runnable applications)
- No Dockerfile or deployment configurations
- Depended upon by other modules
- Contain reusable code and patterns

## See Also

- [Project Modules Index](../docs/MODULES.md) - Return to main modules index
- [Patterns](../docs/kb/patterns/README.md) - Patterns used in common modules
