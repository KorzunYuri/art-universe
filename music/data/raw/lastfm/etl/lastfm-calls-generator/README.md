# LastFM Calls Generator

The LastFM Calls Generator is **Stage 1** of the [Lastfm ETL pipeline](../README.md).

What it does:
- periodically creates discovery [API call tasks](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) to collect entities
- monitors entity data staleness and generates refreshment API call tasks
- stores created API call tasks for execution by [Lastfm Calls Performer](../lastfm-calls-performer/README.md) 


## Supported API Methods

For list of supported Lastfm public API methods, see [ETL Supported Methods](../README.md#supported-lastfm-public-api-methods)


## Implementation Details

### Key Concepts

Key concepts are described on ETL pipeline level:

- [Scope Entity](../README.md#scope-entity)
- [Entities Blacklist](../README.md#entities-blacklist)
- [Snapshots](../README.md#snapshots)
- [Data Staleness Model](../README.md#data-staleness-model)

### Key Components

- [LastfmApiCallGenerationScheduler](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/LastfmApiCallGenerationScheduler.java) - Triggers API call creation every N seconds
- [LastfmApiCallGeneratorsRegistry.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/LastfmApiCallGeneratorsRegistry.java) - Generators registry. 
- [BaseLastfmApiCallGenerator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/BaseLastfmApiCallGenerator.java) - Abstract class introducing auto-registration in the registry and basic methods
- [EntityScopedApiCallGenerator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/generate/generator/common/EntityScopedApiCallGenerator.java) - Intermediate class that operates on a specific entity type (Artist, Album, Track, Tag) (Example: for `artist.getTags` method Artist is the scope entity). 
- services and repositories for creating snapshots and API calls

Having Generators Registry gives us the following benefits:
- Extensibility: Add new API methods without modifying scheduler
- Separation of Concerns: Each generator handles one API method
- Automatic Discovery: Generators self-register on instantiation
- Type Safety: Compile-time validation of generator types

Benefits or EntityScopedApiCallGenerator:
- Code Reuse: Common staleness logic, common request parameters etc.
- Type Safety: Generic type parameter ensures entity type consistency
- Simplified Implementation: Concrete generators only implement entity-specific logic
- services and repositories for retrieving stale entities

### Call Generation Lifecycle

- scheduler is invoked (fixed delay between executions)
- LastfmApiCallGenerationScheduler.generateApiCalls() is invoked
- for each registered generator G bound to method M generator.createApiCalls() is called. Generator then:
  - queries stale entities / expired API calls, filtering out blacklisted entities
  - for each entity E creates LastfmApiCall object with reference to entity E, method M and request parameters P depending on method
- `lastfm.scheduling.calls-generate.fixed-delay-secs` seconds passes until invoking scheduler again


## Development Guides

### Adding New Generators

To add support for a new LastFM API method:

1. Add new value to [LastfmApiCallType.java](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCallType.java)
2. Create Generator, extending it from one of entity-scoped abstract generators or BaseLastfmApiCallGenerator
3. (optionally) override the method of retrieving missing/stale data
4. Configure Due Duration in the [config](src/main/resources/application.yml)

That's it! The generator automatically registers and gets called by the scheduler.


## Run Configuration

Apart from [ETL environment variable](../README.md#common-environment-variables), this module requires:

- `MURAW_LASTFM_CALLS_GENERATOR_ACTUATOR_INTERNAL_PORT` - Actuator port


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures


## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiCallStatus` manages API call execution lifecycle
- [Coded Enums](../../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - `LastfmApiCallType` enum with integer codes
- [Base Entity](../../../../../../docs/kb/patterns/backend/entities/base-entity.md) - Extends `ApiCall` base class
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles
- [Entity Binding - Backend](../../../../../../docs/kb/features/binding-raw-entities-to-master.md) - API calls bound to entities via `entityType` and `entityId`


## Related Documentation

- [LastFM Models](../../lastfm-models/README.md): Dependency - JPA entities and DTOs
- [LastFM ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [LastFM Modules Overview](../../README.md) - All LastFM modules
