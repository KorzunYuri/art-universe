# LastFM ETL Pipeline

This directory contains the LastFM ETL pipeline modules that collect, process, and store data from the LastFM API.

## Pipeline Overview

The LastFM ETL pipeline is a **three-stage continuous data collection system** that collects data from LastFM public API and keeps them up-to-date.

**Stage 1: [Calls Generator](lastfm-calls-generator/README.md)** → Identifies stale data and creates API call tasks
- For each [supported method](#supported-lastfm-public-api-methods):
  - Queries database for entities with stale data (based on last API calls performed for method)
  - Creates [API call tasks](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) with `PENDING` [status](../../../../../common/data/raw/data-raw-commons-jpa/src/main/java/yurykorzun/art/universe/data/raw/common/etl/entity/ApiCallStatus.java)

**Stage 2: [Calls Performer](lastfm-calls-performer/README.md)** → Executes API calls and stores raw responses
- Picks up `PENDING` [API call tasks](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java)
- Makes HTTP requests to LastFM API with rate limiting
- [Stores](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiResponse.java) raw JSON responses in database with `PENDING` [status](../../../../../common/data/raw/data-raw-commons-jpa/src/main/java/yurykorzun/art/universe/data/raw/common/etl/entity/ApiResponseStatus.java)
- Updates API call status to `COMPLETED` or `FAILED`

**Stage 3: [Response Parser](lastfm-response-parser/README.md)** → Parses responses and updates entities
- Reads API call responses with `COMPLETED` status
- Extracts entities and attributes from JSON
- Creates/updates entities in database (artists, albums, tracks, tags)
- Updates response status to `COMPLETED` or `VALIDATION_ERROR`/`PROCESSING_ERROR`

### Supporting Modules

- [ETL REST API](lastfm-etl-rest-api/README.md) - REST API for manual ETL control and entity editing (e.g. approval)


## Supported Lastfm public API Methods

The ETL currently supports 10 out of 15 LastFM API methods with configurable due durations:

### Artist Methods

| API Method | Purpose |
|------------|---------|
| `artist.getInfo` | Fetch artist details and metrics |
| `artist.getSimilar` | Fetch similar artists |
| `artist.getTopTags` | Fetch artist's top tags |
| `artist.getTopAlbums` | Fetch artist's top albums |
| `artist.getTopTracks` | Fetch artist's top tracks |
| `artist.search` | Search for artists (on-demand) |

### Album Methods

| API Method | Purpose |
|------------|---------|
| `album.getInfo` | Fetch album details and metrics |

### Track Methods

| API Method | Purpose |
|------------|---------|
| `track.getInfo` | Fetch track details and metrics |

### Tag Methods

| API Method | Purpose                                    |
|------------|--------------------------------------------|
| `tag.getTopTags` | Fetch top tags (global) - discovery method |
| `tag.getTopArtists` | Fetch top artists for a tag                |
| `tag.getTopTracks` | Fetch top tracks for a tag                 |

### Method Details

`tag.getTopTags` is the only non-scoped method: all other methods require specific entity's MBID or name.

`artist.search` and `tag.getTopTags` are the only discovery methods: all other method require a scope entity.

All methods accept pagination parameters. Most methods' output is limited to one page. Some exceptions:
- `tag.topTags`: Limited to 2000 records (LastFM API offset limitation)
- `tag.topTracks`: One 50-item API call per 50,000 tag usages

## Key Concepts

### Scope Entity

Most methods of Lastfm public API are executed in relation to a specific entity, e.g. `artist.getTopTracks` expects artist to be provided.
That is why [API calls](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmApiCall.java) has optional fields `entityType` and `entityId`.

### Entities Blacklist

Entities below certain [threshold](lastfm-response-parser/src/main/resources/application.yml) are blacklisted and no API Call of any method will be created for them until they are restored.
The threshold is applied when entity has been parsed and the attribute to compare against threshold is not empty.

### Snapshots

[LastfmDataSnapshot.java](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmDataSnapshot.java) unites API calls generated in a single iteration for the same entity or, in case of `tag.topTags`, parameters.

**Why we need this**: for some methods we produce a couple of API calls per entity. Examples:
- we request N pages from method M for entity E - this will end up with N API calls 
- we request top-2000 tags from method `tag.topTags`, which result in 40 API calls

In the same way, [LastfmAttributeSnapshot.java](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/etl/entity/LastfmAttributeSnapshot.java) unites attributes extracted from a series of API call belonging to the same snapshot.

### Data Staleness Model

For each LastFM API method there is configured **due duration** parameter that determines how often API calls to this method should be executed _ideally_.

**How it works**:
1. API call generator queries entity E without `PENDING` API calls for method M
2. API call generator creates new API call(s) with scope entity E for method M with parameters P with `due_dttm` equal to `current_timestamp + due_duration`
3. No new API calls for entity E, method M with parameters P will be created until `due_dttm`

**Note** that for `tag.topTags` there is no scope entity, only parameters.

Watch [Calls Generator config](lastfm-calls-generator/src/main/resources/application.yml) for exact values.

### SCD2 Attribute History

ETL maintains history of collected attributes via [LastfmAttributeHistoryRecord.java](../lastfm-models/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/domain/entity/attribute/LastfmAttributeHistoryRecord.java).
The value of attribute can be either number or string.

## Data Flow Example

Let's trace how an artist's data gets refreshed:

```
Day 0: Artist "Radiohead" is created
  └─> artist.getInfo call generated → executed → parsed
  └─> Snapshot created with timestamp = Day 0

Day 1-6: No action (data is fresh, due duration = 7 days)

Day 7: Artist data is now stale
  └─> Calls Generator: Detects stale data (7 days old, due = 7 days)
  └─> Calls Generator: Creates LastfmApiCall(method=artist.getInfo, artist=Radiohead, status=PENDING)

Day 7 + few minutes:
  └─> Calls Performer: Picks up PENDING call
  └─> Calls Performer: Makes HTTP GET to api.last.fm/2.0?method=artist.getInfo&artist=Radiohead
  └─> Calls Performer: Stores raw JSON response, sets status=COMPLETED

Day 7 + few more minutes:
  └─> Response Parser: Reads COMPLETED call
  └─> Response Parser: Parses JSON, updates artist entity
  └─> Response Parser: Creates new snapshot with timestamp = Day 7
  └─> Artist is fresh again for next 7 days
```

## Deployment Architecture

Deployments to Local(test) and Prod environment is orchestrated by Docker Compose. Watch [Docker Deployment Guide](../../../../../env/docker/README.md) for details.

## Monitoring & Observability

### Metrics

[Lastfm ETL REST API](lastfm-etl-rest-api/README.md) exposes custom Spring Boot Actuator metrics related to ETL modules:

**Calls Generator**:
- `lastfm.calls.generated.total` - Total API calls created
- `lastfm.calls.generated.by_method` - Breakdown by API method

**Calls Performer**:
- `lastfm.api.calls.executed.total` - Total API calls executed
- `lastfm.api.calls.failed.total` - Failed API calls
- `lastfm.api.rate_limit.remaining` - Current rate limit headroom

**Response Parser**:
- `lastfm.responses.parsed.total` - Total responses parsed
- `lastfm.entities.created.total` - New entities created
- `lastfm.entities.updated.total` - Existing entities updated

## Configuration

### Common Environment Variables

All ETL modules share these database configuration variables:

- `MURAW_LASTFM_DB_MASTER_HOST` - PostgreSQL master host
- `MURAW_LASTFM_DB_MASTER_PORT` - PostgreSQL master port
- `MURAW_LASTFM_DB_NAME` - Name of the database containing Lastfm data
- `MURAW_LASTFM_DB_SCHEMA` - Corresponding schema name
- `MURAW_LASTFM_DB_WRITER_USERNAME` - Lastfm editor user name
- `MURAW_LASTFM_DB_WRITER_PASSWORD` - Lastfm editor user password
- `ZIPKIN_BASE_URL` - Zipkin URL

### Development Environment Setup

When running ETL modules in dev mode (IntelliJ), environment variables are loaded in this order:
1. `env/docker/common/music-data-raw-lastfm.env` - Lastfm constants
2. `env/docker/dev/common.env` - Dev common settings
3. `env/docker/dev/music-data-raw-lastfm.env` - Dev env variables
4. `env/docker/dev/music-data-raw-lastfm.secrets.env` - Dev secrets (Git-ignored)

**Prerequisites**:
- Dev stack must be running: `docker-compose -f env/docker/dev/docker-compose.yml up -d`
- See [DEVELOPMENT.md](../../../../../docs/DEVELOPMENT.md) for complete dev workflow

### Module-Specific Configuration

Each module has additional configuration - see individual module READMEs:
- [Calls Generator Configuration](lastfm-calls-generator/README.md#run-configuration)
- [Calls Performer Configuration](lastfm-calls-performer/README.md#run-configuration)
- [Response Parser Configuration](lastfm-response-parser/README.md#run-configuration)

## Related Documentation

- [LastFM Modules Overview](../README.md) - Parent directory with all LastFM modules
- [LastFM Models](../lastfm-models/README.md) - JPA entities used by all ETL modules
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
