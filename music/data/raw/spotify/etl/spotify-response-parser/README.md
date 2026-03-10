# Spotify Response Parser

The Spotify Response Parser is **Stage 3** of the [Spotify ETL pipeline](../README.md).

What it does:
- reads [API responses](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiResponse.java) with `PENDING` status
- parses JSON into DTOs and writes records into per-iteration staging tables
- manages staging iteration lifecycle (open/seal based on time and record count)
- for search responses: scores match quality using Jaro-Winkler similarity and updates `search_attempt`

## Supported API Methods

For list of supported Spotify Web API methods, see [ETL Supported Methods](../README.md#supported-spotify-web-api-methods)

## Implementation Details

### Key Concepts

Key concepts are described on ETL pipeline level:

- [Staging Layer](../README.md#staging-layer)
- [Synthetic ID Resolution](../README.md#synthetic-id-resolution)
- [Search Pipeline](../README.md#search-pipeline)

### Key Components

- `SpotifyApiResponseProcessingScheduler` - Triggers response processing cycle
- `SpotifyApiResponseProcessorsRegistry` - Processors registry (one processor per call type)
- `BaseSpotifyApiResponseProcessor` - Abstract class with auto-registration
- `StagingIterationService` - Manages staging iteration lifecycle (create, seal, find open)
- `StagingWriter` - JdbcTemplate-based writer that creates per-iteration tables and inserts records
- `SyntheticIdResolutionService` - Resolves or generates synthetic IDs for referenced entities
- `SearchMatchScoringService` - Jaro-Winkler scoring for search match evaluation

**Entity GET processors**:
- `SpotifyArtistGetResponseProcessor` - Stages artist from `ARTIST_GET` response
- `SpotifyArtistAlbumsResponseProcessor` - Stages albums + ARTIST_ALBUM relations
- `SpotifyAlbumGetResponseProcessor` - Stages album with embedded artists and tracks
- `SpotifyAlbumTracksResponseProcessor` - Stages tracks + ALBUM_TRACK and TRACK_ARTIST relations
- `SpotifyTrackGetResponseProcessor` - Stages track with embedded album

**Search processors** (stage results + score matches):
- `SpotifySearchArtistResponseProcessor` - Stages artists from search, scores best match
- `SpotifySearchAlbumResponseProcessor` - Stages albums with artists and relations, scores best match
- `SpotifySearchTrackResponseProcessor` - Stages tracks with albums and relations, scores best match

### Response Processing Lifecycle

- Scheduler is invoked (fixed delay between executions)
- Orchestrator finds/creates an OPEN staging iteration
- For each PENDING response:
  - Looks up the appropriate processor from the registry
  - Processor parses JSON, writes to staging tables via `StagingWriter`
  - For search responses: calls `SearchMatchScoringService` to update `search_attempt`
- When iteration exceeds max records or max open time: iteration is sealed
- Response status: `PENDING` -> `COMPLETED` or `PROCESSING_ERROR`

## Run Configuration

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_SPOTIFY_RESPONSE_PARSER_ACTUATOR_INTERNAL_PORT` - Actuator port
- `SPOTIFY_SEARCH_MATCH_THRESHOLD` - Minimum Jaro-Winkler score for a match (default: `0.85`)
- `SPOTIFY_SEARCH_GRACE_PERIOD_DAYS` - Days before retrying NO_MATCH (default: `30`)

## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures

## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiResponseStatus` manages response lifecycle
- [Coded Enums](../../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - `SpotifyApiCallType` for processor registry
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles

## Related Modules

- [Spotify Models](../../spotify-models/README.md) - Dependency: JPA entities and DTOs
- [Spotify ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [Spotify Modules Overview](../../README.md) - All Spotify modules
