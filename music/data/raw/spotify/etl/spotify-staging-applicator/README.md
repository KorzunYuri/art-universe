# Spotify Staging Applicator

The Spotify Staging Applicator is **Stage 4** of the [Spotify ETL pipeline](../README.md).

What it does:
- picks up `SEALED` [staging iterations](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/StagingIteration.java)
- applies staged data to target tables in FK-dependency order with UPSERT semantics
- resolves synthetic IDs between staging tables
- reconciles matched search attempts by creating bindings in music-data-master via internal API
- cleans up completed/failed iteration tables after configurable retention period

## Implementation Details

### Key Concepts

Key concepts are described on ETL pipeline level:

- [Staging Layer](../README.md#staging-layer)
- [Synthetic ID Resolution](../README.md#synthetic-id-resolution)
- [Search Pipeline](../README.md#search-pipeline)

### Key Components

- `StagingApplicationScheduler` - Triggers staging application cycle
- `StagingApplicationServiceImpl` - Finds sealed iterations and delegates to applicator
- `StagingIterationApplicator` - Core JDBC logic: upserts + ID resolution in a `REQUIRES_NEW` transaction
- `SearchReconciliationService` - Processes MATCHED search attempts, creates bindings via HTTP
- `MasterDataBindingClient` - RestClient-based HTTP client for music-data-master internal API

### Application Order

The applicator processes each iteration in strict dependency order:

1. **Upsert artists** from `stg_artist_{N}`
2. **Resolve artist IDs** in `stg_album_{N}` and `stg_entity_relation_{N}`
3. **Upsert albums** from `stg_album_{N}`
4. **Resolve album IDs** in `stg_track_{N}` and `stg_entity_relation_{N}`
5. **Resolve artist IDs** in `stg_track_{N}`
6. **Upsert tracks** from `stg_track_{N}`
7. **Resolve track IDs** in `stg_entity_relation_{N}`
8. **Upsert entity relations** from `stg_entity_relation_{N}` (only fully resolved rows)

All upserts use `INSERT ... ON CONFLICT DO UPDATE` with COALESCE to preserve existing values when new data is null.

### Search Reconciliation

After applying iterations, the scheduler triggers reconciliation:

1. Finds all `search_attempt` records with status=MATCHED
2. For each: looks up the raw entity by `matched_spotify_id`
3. If entity exists: calls `POST /api/v1/internal/{entities}/bind/existing/spotify/{rawEntityId}` with `{masterId}` and `approvalStatus=pending`
4. Updates search_attempt status to BOUND
5. On failure: leaves as MATCHED for retry next cycle

### Iteration Lifecycle

`SEALED` -> `APPLYING` -> `COMPLETED`/`FAILED`

Each iteration is applied in a separate `REQUIRES_NEW` transaction. If application fails partway, the iteration is marked FAILED with error message. UPSERT semantics make retries safe.

## Run Configuration

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_SPOTIFY_STAGING_APPLICATOR_ACTUATOR_INTERNAL_PORT` - Actuator port
- `MUSIC_DATA_MASTER_INTERNAL_URL` - Base URL for music-data-master internal API

## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures

## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `StagingIterationStatus` manages iteration lifecycle
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles

## Related Modules

- [Spotify Models](../../spotify-models/README.md) - Dependency: JPA entities and DTOs
- [Spotify ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [Spotify Modules Overview](../../README.md) - All Spotify modules
