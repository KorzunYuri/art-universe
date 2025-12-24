# Modules Index

This document is the **single source of truth** for list of modules.


## What Are Modules?

**Modules** describe *where* code lives and provide module-specific context for focused development work. They:
- List features they implement
- Reference patterns they use
- Contain module-specific implementation details
- Provide build, test, and deployment information


## Module Index

> **Note**: For build commands, see [Gradle Commands Reference](kb/guides/gradle-commands.md).

### Common Modules (Shared Libraries)

| Module | Type | Purpose |
|--------|------|---------|
| [`:common:commons-context`](../common/commons-context/README.md) | Library | Spring configuration, ObjectMapper auto-configuration |
| [`:common:commons-jpa`](../common/commons-jpa/README.md) | Library | JPA utilities, Coded registry, JPA auto-configuration |
| [`:common:commons-observability`](../common/commons-observability/README.md) | Library | Monitoring and observability (Actuator, Prometheus metrics) |
| [`:common:commons-web`](../common/commons-web/README.md) | Library | Web utilities, exception handling, CORS configuration |
| [`:common:test:commons-test`](../common/test/commons-test/README.md) | Library | Base testing utilities, test helpers |
| [`:common:test:commons-test-web`](../common/test/commons-test-web/README.md) | Library | Web testing utilities, MockMvc helpers |
| [`:common:test:commons-test-db`](../common/test/commons-test-db/README.md) | Library | Database testing utilities, TestContainers setup |
| [`:common:data:raw:data-raw-commons-jpa`](../common/data/raw/data-raw-commons-jpa/README.md) | Library | Common JPA entities and utilities for raw data collection |
| [`:common:data:raw:data-raw-commons-api-client`](../common/data/raw/data-raw-commons-api-client/README.md) | Library | Common API client utilities (HTTP, rate limiting) |

### LastFM Data Collection Modules

#### Core Data Modules

| Module | Type | Purpose |
|--------|------|---------|
| [`:music:data:raw:lastfm:lastfm-models`](../music/data/raw/lastfm/lastfm-models/README.md) | Library | JPA entities and DTOs for LastFM data |
| [`:music:data:raw:lastfm:lastfm-repositories`](../music/data/raw/lastfm/lastfm-repositories/README.md) | Library | Data access layer for LastFM entities |

#### Service Modules

| Module | Type | Purpose |
|--------|------|---------|
| [`:music:data:raw:lastfm:lastfm-rest-api`](../music/data/raw/lastfm/lastfm-rest-api/README.md) | Service | Read-only REST API for LastFM raw data |
| [`:music:data:raw:lastfm:etl:lastfm-etl-rest-api`](../music/data/raw/lastfm/etl/lastfm-etl-rest-api/README.md) | Service | Write operations REST API for LastFM ETL |
| [`:music:data:raw:lastfm:etl:lastfm-calls-generator`](../music/data/raw/lastfm/etl/lastfm-calls-generator/README.md) | Service | Generates API calls for data collection |
| [`:music:data:raw:lastfm:etl:lastfm-calls-performer`](../music/data/raw/lastfm/etl/lastfm-calls-performer/README.md) | Service | Executes API calls against LastFM API |
| [`:music:data:raw:lastfm:etl:lastfm-response-parser`](../music/data/raw/lastfm/etl/lastfm-response-parser/README.md) | Service | Parses and processes API responses |
| [`:music:data:raw:lastfm:migrations:lastfm-liquibase-service`](../music/data/raw/lastfm/migrations/lastfm-liquibase-service/README.md) | Service | Database migration runner |

#### Infrastructure Modules

| Module | Type | Purpose |
|--------|------|---------|
| [`:music:data:raw:lastfm:etl:lastfm-tasks-coordinator`](../music/data/raw/lastfm/etl/lastfm-tasks-coordinator/README.md) | Library | Distributed task coordination with database-backed state |
| [`:music:data:raw:lastfm:migrations:lastfm-liquibase-resources`](../music/data/raw/lastfm/migrations/lastfm-liquibase-resources/README.md) | Library | Liquibase changelog definitions (SQL) |

#### Test Modules

| Module | Type | Purpose |
|--------|------|---------|
| [`:music:data:raw:lastfm:test:lastfm-commons-test`](../music/data/raw/lastfm/test/lastfm-commons-test/README.md) | Library | Base testing utilities for LastFM modules |
| [`:music:data:raw:lastfm:test:lastfm-commons-test-context`](../music/data/raw/lastfm/test/lastfm-commons-test-context/README.md) | Library | Spring context test configuration |
| [`:music:data:raw:lastfm:test:lastfm-commons-test-jpa`](../music/data/raw/lastfm/test/lastfm-commons-test-jpa/README.md) | Library | JPA testing utilities |
| [`:music:data:raw:lastfm:test:lastfm-commons-test-db`](../music/data/raw/lastfm/test/lastfm-commons-test-db/README.md) | Library | Database testing with TestContainers |
| [`:music:data:raw:lastfm:test:lastfm-commons-test-db-helper`](../music/data/raw/lastfm/test/lastfm-commons-test-db-helper/README.md) | Library | Database consistency helpers (DbConsistencyHelper) |
| [`:music:data:raw:lastfm:test:lastfm-commons-test-web`](../music/data/raw/lastfm/test/lastfm-commons-test-web/README.md) | Library | Web/MVC testing utilities |

### Master Data & Quiz Modules

| Module | Type | Purpose |
|--------|------|---------|
| [`:music:data:master`](../music/data/master/README.md) | Service | Master data management and binding service |
| [`:music:quiz`](../music/quiz/README.md) | Service | Quiz generation from master data |
| [`:music:ui`](../music/ui/README.md) | Service | React-based data management interface |

---

## How to Use Module Documentation

### Starting Work on a Module

1. **Load module context**: Read module's `README.md`
2. **Understand features**: Follow links to metafeatures for cross-cutting features
3. **Review patterns**: Check patterns for implementation standards
4. **Deep dive**: Read module-specific files as needed


## See Also

- **[Patterns Documentation](kb/patterns/README.md)** - Implementation patterns used across modules
- **[Guides](kb/guides/README.md)** - Project-wide development guides
