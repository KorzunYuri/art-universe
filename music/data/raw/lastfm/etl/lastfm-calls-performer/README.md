# LastFM Calls Performer

The LastFM Calls Performer is the second stage of the [Lastfm ETL pipeline](../README.md).

What it does:
- retrieves pending [API call tasks](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) created by the [Lastfm Calls Generator](../lastfm-calls-generator/README.md).
- executes them with rate limiting and retry logic
- stores [raw JSON responses](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java) for processing by the [Lastfm Response Parser](../lastfm-response-parser/README.md).


## Supported API Methods

For list of supported Lastfm public API methods, see [ETL Supported Methods](../README.md#supported-lastfm-public-api-methods)


## Implementation Details

### Key Components

- [LastfmApiCallExecutionScheduler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/perform/LastfmApiCallExecutionScheduler.java) - Schedules execution iterations
- [LastfmCallsOrchestrator.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/perform/LastfmCallsOrchestrator.java)- Orchestrates api calls batch execution with rate limiting
- [LastfmApiCallExecutorImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/task/call/perform/LastfmApiCallExecutorImpl.java) - Executes HTTP requests using Spring RestClient
- [LastfmApiCallServiceImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/service/impl/LastfmApiCallServiceImpl.java) - Handles api call state
- [LastfmApiResponseServiceImpl.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/service/impl/LastfmApiResponseServiceImpl.java) - Validates JSON responses and creates database records

### Details and Patterns

- API call to Lastfm API are executed with strict rate limiting and exponential backoff
- `LastfmApiCallExecutorImpl.java` uses self-injection pattern for retries management
- Complex CTE-based query with windows functions `LastfmApiCallRepository.findUnexpiredPendingApiCallsByDataSnapshotId()` guarantees fair distribution across call types

## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures


## Run Configuration

Apart from [ETL environment variable](../README.md#common-environment-variables), this module requires:

- `MURAW_LASTFM_API_KEY` - LastFM API key
- `MURAW_LASTFM_CALLS_PERFORMER_ACTUATOR_INTERNAL_PORT` - Actuator port


## Patterns Used

This module follows these project-wide patterns:

- [State Machine](../../../../../../docs/kb/patterns/backend/state-machine.md) - `ApiCallStatus` and `ApiResponseStatus` manage execution and response lifecycle


## Related Modules

- [API Client](../../../../../../common/data/raw/data-raw-commons-api-client/README.md): HTTP client utilities
- [LastFM Models](../../lastfm-models/README.md) - JPA entities and DTOs. Contains [LastfmApiCall](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) and [LastfmApiResponse](../../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java)
- [LastFM ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [LastFM Modules Overview](../../README.md) - All LastFM modules
