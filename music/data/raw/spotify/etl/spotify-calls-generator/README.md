# Spotify Calls Generator

The Spotify Calls Generator is **Stage 1** of the [Spotify ETL pipeline](../README.md).

What it does:
- periodically creates [API call tasks](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiCall.java) for entities needing data collection
- monitors data staleness via due duration mechanism and generates refreshment calls
- queries `mu_view` schema for master entities without Spotify bindings and creates search calls
- stores created API calls for execution by [Spotify Calls Performer](../spotify-calls-performer/README.md)

## Supported API Methods

For list of supported Spotify Web API methods, see [ETL Supported Methods](../README.md#supported-spotify-web-api-methods)

## Implementation Details

### Key Concepts

Key concepts are described on ETL pipeline level:

- [Data Staleness Model](../README.md#data-staleness-model)
- [Search Pipeline](../README.md#search-pipeline)
- [Graph Growth](../README.md#graph-growth)

### Key Components

- [SpotifyApiCallGenerationScheduler](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/call/generate/SpotifyApiCallGenerationScheduler.java) - Triggers API call creation every N seconds
- [SpotifyApiCallGeneratorsRegistry](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/call/generate/SpotifyApiCallGeneratorsRegistry.java) - Generators registry
- [BaseSpotifyApiCallGenerator](src/main/java/yurykorzun/art/universe/music/data/raw/spotify/task/call/generate/BaseSpotifyApiCallGenerator.java) - Abstract class with auto-registration

**Entity GET generators** (query `mu_raw_spotify` entities without active calls):
- `SpotifyArtistGetCallGenerator` - ARTIST_GET calls
- `SpotifyArtistAlbumsCallGenerator` - ARTIST_ALBUMS calls
- `SpotifyAlbumGetCallGenerator` - ALBUM_GET calls
- `SpotifyAlbumTracksCallGenerator` - ALBUM_TRACKS calls
- `SpotifyTrackGetCallGenerator` - TRACK_GET calls

**Search generators** (query `mu_view` for unbound master entities):
- `SpotifySearchArtistCallGenerator` - SEARCH_ARTIST calls, driven by `mu_view.v_artist` + `mu_view.v_artist_binding`
- `SpotifySearchAlbumCallGenerator` - SEARCH_ALBUM calls, driven by `mu_view.v_album` + `mu_view.v_album_binding`
- `SpotifySearchTrackCallGenerator` - SEARCH_TRACK calls, driven by `mu_view.v_track` + `mu_view.v_track_binding`

### Call Generation Lifecycle

- Scheduler is invoked (fixed delay between executions)
- For each registered generator: `generator.createApiCalls()` is called
- **Entity GET generators**: query entities via JPA CriteriaBuilder, create calls with `spotify_id` param
- **Search generators**: query `mu_view` cross-schema via JdbcTemplate, create `search_attempt` + calls with `q`/`type` params
- Configured delay passes until next invocation

## Development Guides

### Adding New Generators

1. Add new value to [SpotifyApiCallType](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiCallType.java)
2. Create generator extending `BaseSpotifyApiCallGenerator`
3. Configure due duration in [application.yml](src/main/resources/application.yml)

The generator automatically registers and gets called by the scheduler.

## Run Configuration

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_SPOTIFY_CALLS_GENERATOR_ACTUATOR_INTERNAL_PORT` - Actuator port
- `SPOTIFY_SEARCH_ENABLED` - Enable search generators (default: `false`)

## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures

## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiCallStatus` manages API call lifecycle
- [Coded Enums](../../../../../../docs/kb/patterns/backend/entities/coded-enums.md) - `SpotifyApiCallType` enum with integer codes
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles

## Related Documentation

- [Spotify Models](../../spotify-models/README.md) - Dependency: JPA entities and DTOs
- [Spotify ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [Spotify Modules Overview](../../README.md) - All Spotify modules
