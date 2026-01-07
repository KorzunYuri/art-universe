# Music Universe - Master Data Management Service

This module is a Spring Boot web application serving as data management service for music master data.


## Entities

- **Core Master Entities**: Artist, Album, Track, Category - the authoritative master data
- **Binding Entities**: External entities (from LastFM, Spotify, etc.) bound to master entities
- **Relation Entities**: Relationships between master entities (ArtistCategory, ArtistTrack, CategoryCategory)
- **Relation Binding Entities**: External entities relations bound to master entities relations

For a detailed entity relations explanation see [Entity Relations Reference](docs/entity-relations.md)

TODO: move external entity binding entities to `entity/binding` package
TODO: move category DAG DTOs and entities to `.../categorydag`


## Endpoints

The module provides the following groups of endpoints:
- endpoints for master entities management
- endpoints to manage relationships between master entities
- endpoints for binding entities and their relations from external sources (LastFM, Spotify, etc.) to master entities and their relations
- endpoints with read access to master entities:
  - paginated search
  - lookup for dropdowns

For the full list of endpoints see [API Reference](docs/api.md)


## Special Features

- [Relations Handling Using Reflection](docs/features/relations-handling-with-reflection.md) - See how reflection allows unified handling of entity relations
- [Lookup Implementation](docs/features/lookup) - See how lookup (used by UI for dropdowns) is implemented
- [Category DAG](docs/features/category-dag.md) - See how categories hierarchy is designed

## Patterns Used

This module follows these project-wide patterns:

| Pattern                                                                                 | Usage in Module |
|-----------------------------------------------------------------------------------------|-----------------|
| [REST API Conventions](../../../docs/kb/patterns/backend/api/conventions.md)            | Standard HTTP methods, response codes, pagination for all endpoints |
| [Search vs Lookup](../../../docs/kb/patterns/backend/api/search-and-lookup.md)          | Two retrieval patterns: full search (paginated) and minimal lookup (dropdowns) |
| [Coded Enums](../../../docs/kb/patterns/backend/entities/coded-enums.md)                | ApprovalStatus, DataSource, EntityType with JPA converters |
| [Testing With Persistence Layer](../../../docs/kb/patterns/backend/testing/testing)     | TestContainers PostgreSQL for integration tests |
| [Controllers Testing](../../../docs/kb/patterns/backend/testing/testing-controllers.md) | MockMvc for controller tests |



## Build & Deployment

**See**: [Gradle Commands Guide](../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../env/docker/README.md) for deployment procedures


## Related Documentation

### Patterns This Module Uses
- [Backend Patterns](../../../docs/kb/patterns/backend/README.md) - TBD

### Other Modules
- **[LastFM REST API](../../../docs/kb/modules/lastfm-rest-api/README.md)**: Provides external entity data for binding
- **[LastFM ETL Pipeline](../../../docs/MODULES.md)**: Provides external relation data
- **[Music Quiz](../../../docs/MODULES.md)**: Consumes master data and relations for quiz generation
- **[Music UI](../../../docs/MODULES.md)**: Management interface for binding operations
- **[Commons Web](../../../common/commons-web/README.md)**: REST API utilities (dependency)
- **[Commons Test](../../../common/test/commons-test-db/README.md)**: Database test utilities (test dependency)

### Other Docs
- [Architecture Overview](../../../docs/kb/guides/README.md) - System architecture and design
- [Project Modules Index](../../../docs/MODULES.md) - Complete modules index
- [Development Workflow](../../../docs/kb/guides/development-workflow.md) - Git workflow, code review, testing
- [Gradle Commands](../../../docs/kb/guides/gradle-commands.md) - Build and test commands
- [Deployment Guide](../../../docs/kb/guides/deployment.md) - Environment setup and deployment
