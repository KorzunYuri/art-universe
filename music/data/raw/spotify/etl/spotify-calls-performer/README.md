# Spotify Calls Performer

The Spotify Calls Performer is **Stage 2** of the [Spotify ETL pipeline](../README.md).

What it does:
- polls for `CREATED` [API call tasks](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiCall.java)
- executes HTTP requests to the Spotify Web API with OAuth2 bearer tokens
- enforces rate limiting via Guava `RateLimiter`
- stores raw JSON [responses](../../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiResponse.java) for processing by [Spotify Response Parser](../spotify-response-parser/README.md)

## Supported API Methods

For list of supported Spotify Web API methods, see [ETL Supported Methods](../README.md#supported-spotify-web-api-methods)

## Implementation Details

### Key Components

- `SpotifyApiCallExecutionScheduler` - Triggers call execution cycle
- `SpotifyCallsOrchestrator` - Rate-limited loop orchestrating individual call execution
- `SpotifyApiCallExecutorImpl` - Executes a single API call: fetches, stores response, updates status
- `SpotifyApiClient` - HTTP client extending `BaseHttpApiClient` with OAuth2 bearer token injection
- `SpotifyOAuth2TokenProvider` - Client credentials flow with token caching and refresh

### Details and Patterns

**URL construction**: The client constructs URLs from the call type's path template and the call's params map:
- Entity GET calls: replaces `{id}` with `spotify_id` param, remaining params become query params
- Search calls: path is `/search`, all params (`q`, `type`, `limit`) become query params

**Error handling**:
- 404: Mark call as `FAILED` (entity not found)
- 429: Log and back off via Spring Retry exponential backoff
- Other HTTP errors: Mark as `FAILED` with error message

**Status flow**: `CREATED` -> `PROCESSING` -> `SUCCESSFUL`/`FAILED`

## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures

## Run Configuration

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_SPOTIFY_CALLS_PERFORMER_ACTUATOR_INTERNAL_PORT` - Actuator port
- `SPOTIFY_CLIENT_ID` - Spotify application client ID
- `SPOTIFY_CLIENT_SECRET` - Spotify application client secret

## Observability

This module is instrumented with an AOP-based observability aspect:

- `ApiCallPerformerObservabilityAspect` — wraps individual API call execution
- **Metric**: `music.data.raw.spotify.api.call.perform` (timer)
- **Tags**: `api_call_type`, `status`

See [OBSERVABILITY.md](../../../../../../docs/OBSERVABILITY.md) for the full picture.

## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiCallStatus` manages call lifecycle
- [Environment Profiles](../../../../../../docs/kb/patterns/backend/configuration/environment-profiles.md) - dev, local, prod profiles

## Related Modules

- [Spotify Models](../../spotify-models/README.md) - Dependency: JPA entities and DTOs
- [Spotify ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [Spotify Modules Overview](../../README.md) - All Spotify modules
