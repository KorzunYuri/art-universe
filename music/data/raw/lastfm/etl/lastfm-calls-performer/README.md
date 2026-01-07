# LastFM Calls Performer

The LastFM Calls Performer is the second stage of the [Lastfm ETL pipeline](../README.md).

What it does:
- retrieves pending [API call tasks](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/entity/LastfmApiCall.java) created by the [Lastfm Calls Generator](../lastfm-calls-generator/README.md).
- executes them with rate limiting and retry logic
- stores [raw JSON responses](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/entity/LastfmApiResponse.java) for processing by the [Lastfm Response Parser](../../../../../../docs/kb/modules/lastfm-response-parser/README.md).


## Supported API Methods

For list of supported Lastfm public API methods, see [ETL Supported Methods](../README.md#supported-api-methods)


## Implementation Details

### Key Components

- [LastfmApiCallScheduler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/scheduling/LastfmApiCallScheduler.java) - Schedules execution iterations
- [LastfmApiCallServiceImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/service/impl/LastfmApiCallServiceImpl.java)- Orchestrates rate limiting, transaction management
- [LastfmApiClientImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/service/impl/LastfmApiClientImpl.java) - Executes HTTP requests using Spring RestClient
- [LastfmApiResponseServiceImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/service/impl/LastfmApiResponseServiceImpl.java) - Validates JSON responses and creates database records

### Details and Patterns

- API call to Lastfm API are executed with strict rate limiting and exponential backoff
- [LastfmApiCallServiceImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/service/impl/LastfmApiCallServiceImpl.java) uses self-injection pattern for transactional status updates
- Complex CTE-based query with windows functions `LastfmApiCallRepository.findUnexpiredPendingApiCallsByDataSnapshotId()` guarantees fair distribution across call types
- API calls [are not performed during DB mintenance](../README.md#stop-during-maintenance)


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures


## Run Configuration

Apart from [ETL environment variable](../README.md#common-environment-variables), this module requires:

- `MURAW_LASTFM_API_KEY` - LastFM API key
- `MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_INTERNAL_PORT` - Actuator port


## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiCallStatus` and `ApiResponseStatus` manage execution and response lifecycle


## Related Modules

- [API Client](../../../../../../common/data/raw/data-raw-commons-api-client/README.md): HTTP client utilities
- [LastFM Models](../../lastfm-models/README.md) - JPA entities and DTOs. Contains [LastfmApiCall](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/entity/LastfmApiCall.java) and [LastfmApiResponse](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/entity/LastfmApiResponse.java)
- [LastFM ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [LastFM Modules Overview](../../README.md) - All LastFM modules
