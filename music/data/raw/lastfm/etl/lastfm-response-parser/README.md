# LastFM Response Parser

The LastFM Response Parser is **Stage 3** (final stage) of the [Lastfm ETL pipeline](../README.md).

What it does:
- parses [raw JSON responses](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java) created by the [Lastfm Calls Performer](../lastfm-calls-performer/README.md)
- extracts entity data (artists, albums, tracks, tags) and persists to database
- tracks attribute history, validates quality thresholds, and creates entity relationships


## Supported API Methods

For list of supported Lastfm public API methods, see [ETL Supported Methods](../README.md#supported-lastfm-public-api-methods)


## Implementation Details

### Key Concepts

Key concepts are described on ETL pipeline level:

- [Scope Entity](../README.md#scope-entity)
- [Entities Blacklist](../README.md#entities-blacklist)
- [Snapshots](../README.md#snapshots)
- [SCD2 Attribute History](../README.md#scd2-attribute-history)

### Key Components

- [LastfmApiResponseProcessingScheduler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiResponseProcessingScheduler.java) - Triggers response parsing every 1 second
- [LastfmApiResponseProcessorsRegistry.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiResponseProcessorsRegistry.java) - Maps DTO classes to processor instances
- [LastfmApiResponseProcessor.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiResponseProcessor.java) - Abstract base for all processors
- [LastfmApiDtoProcessingOrchestrator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/LastfmApiDtoProcessingOrchestrator.java) - Orchestrates DTO-to-entity mapping and saving
- [AbstractEntityRelationService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/service/relationship/AbstractEntityRelationService.java) - Reflection-based UPSERT SQL generation for relationships
- [LastfmAttributeHistoryProcessor.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/response/process/processor/LastfmAttributeHistoryProcessor.java) - Moves staging data to main attribute history table

Having Processors Registry gives us the following benefits:
- Type Safety: Compile-time validation of processor-DTO mappings
- Extensibility: Add new API methods without modifying scheduler
- Separation of Concerns: Each processor handles one API method
- Automatic Discovery: Processors self-register on instantiation

### Details and Patterns

- Response processing uses template method pattern with hierarchical entity factories for DTO-to-entity mapping
- Attribute history tracking uses double-buffered staging tables (`stg_attribute_history_a/b`) to prevent writer/processor conflicts
- Quality validation automatically approves/blacklists entities based on configurable thresholds (listeners, plays, usage counts)
- Relationship services use reflection to generate PostgreSQL UPSERT statements from JPA annotations

### Response Processing Lifecycle

- scheduler is invoked (fixed delay: 1 second between executions)
- LastfmApiResponseProcessingScheduler.processResponses() is invoked
- for each pending response R with status `PENDING`:
  - JSON is deserialized to DTO type T based on API method
  - Registry routes DTO to appropriate processor P
  - Processor extracts entities, validates quality, creates relationships
  - Attribute changes are written to staging tables
  - Response status updated to `COMPLETED` or error status
- Separate scheduler processes staging tables → main attribute history table
- 1 second passes until invoking scheduler again


## Quality Thresholds

Entities are auto-approved if they meet these thresholds (configurable in [application.yml](src/main/resources/application.yml)).
Entities below thresholds are auto-blacklisted and excluded from future API calls.


## Run Configuration

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_LASTFM_RESPONSE_PARSER_ACTUATOR_INTERNAL_PORT` - Actuator port


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures


## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiResponseStatus` manages response parsing lifecycle with state transitions
- [Entity Binding - Backend](../../../../../../docs/kb/features/binding-raw-entities-to-master.md) - Responses bound to entities via `apiCall.entityType` and `apiCall.entityId`
- [Coded Enums](../../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - `LastfmAttribute` enum with integer codes
- [Base Entity](../../../../../../docs/kb/patterns/backend/entities/base-entity.md) - All entities extend base classes
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles


## Related Modules

- [LastFM Models](../../lastfm-models/README.md) - JPA entities and DTOs. Contains [LastfmApiResponse](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java)
- [LastFM ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [LastFM Modules Overview](../../README.md) - All LastFM modules
