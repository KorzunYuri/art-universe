# LastFM ETL REST API

The LastFM ETL REST API module provides HTTP access for manual entity editing and maintenance operations outside of the automated ETL pipeline.

It complements the read-only LastFM REST API by allowing write operations for approval status updates, entity unbinding, and database maintenance.


## Key Components

### Controllers

**Collectable Controllers** (`collectable/controller/`):
- [LastfmArtistController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmArtistController.java) - Artist approval status and search requests
- [LastfmAlbumController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmAlbumController.java) - Album approval status management
- [LastfmTrackController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmTrackController.java) - Track approval status management
- [LastfmTagController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/controller/LastfmTagController.java) - Tag approval status management

**Maintenance Controllers** (`maintenance/controller/`):
- [MaintenanceController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/controller/MaintenanceController.java) - Database maintenance operations

**API Client Controllers** (`api/client/controller/`):
- [LastfmApiResponseController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/controller/LastfmApiResponseController.java) - API response management

**General Controllers**:
- [HealthController.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/controller/HealthController.java) - Health check endpoint

### Services

**Collectable Services** (`collectable/service/`):
- [LastfmArtistService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmArtistService.java) - Artist approval status updates
- [LastfmAlbumService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmAlbumService.java) - Album approval status updates
- [LastfmTrackService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmTrackService.java) - Track approval status updates
- [LastfmTagService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmTagService.java) - Tag approval status updates
- [LastfmArtistSearchRequestService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/LastfmArtistSearchRequestService.java) - Artist search request management
- [EntityStatusEventHandler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/EntityStatusEventHandler.java) - Handles entity status change events

**Maintenance Services** (`maintenance/service/`):
- [DbMaintenanceService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/DbMaintenanceService.java) - Scheduled database cleanup and optimization
- [MusicDataIntegrationService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/MusicDataIntegrationService.java) - Unbinds Lastfm entities from Master entities when Lastfm entities are removed by maintenance job

**API Client Services** (`api/client/service/`):
- [LastfmApiResponseService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/api/client/service/LastfmApiResponseService.java) - Displaying raw JSON data received from Lastfm API


## Special Features

### Asynchronous Child Entities Update

When status of an Artist or Album is changed to `DECLINED` or `IGNORED`, this status is copied to all the child entities:
- Albums and Tracks for Artist
- Tracks for Album

To not block the caller, the updates are done asynchronously:
- [EntityStatusChangedEvent.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/event/EntityStatusChangedEvent.java) is fired
- [EntityStatusEventHandler.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/collectable/service/EntityStatusEventHandler.java) catches the event and updates child entities using per-entity-type queries

### Database Maintenance

**Tasks**:
- Remove data that don't meet quality thresholds
- (planned) Cleanup stale API call records

**Cron**: Daily at 8:00 AM

**Service**: [DbMaintenanceService.java](src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/maintenance/service/DbMaintenanceService.java)


## API Endpoints

| Method | Path                              | Purpose                                                      |
|--------|-----------------------------------|--------------------------------------------------------------|
| PATCH  | `/api/v1/artists/{id}/approval`   | Update artist approval status                                |
| POST   | `/api/v1/artists/search`          | Create artist search request (for `artist.search` discovery) |
| PATCH  | `/api/v1/albums/{id}/approval`    | Update album approval status                                 |
| PATCH  | `/api/v1/tracks/{id}/approval`    | Update track approval status                                 |
| PATCH  | `/api/v1/tags/{id}/approval`      | Update tag approval status                                   |
| GET    | `/api/v1/maintenance/trigger`     | Trigger database maintenance                                 |
| GET    | `/api/v1/api/responses/{id}`      | Get API response by ID                                       |
| GET    | `/api/v1/api/responses/{id}/body` | Get API response body                                        |
| GET    | `/health`                         | Health check endpoint                                        |


## Configuration

### Environment Variables

Apart from [ETL environment variables](../README.md#common-environment-variables), this module requires:

- `MURAW_LASTFM_ETL_REST_API_INTERNAL_PORT` - Application HTTP port
- `MURAW_LASTFM_ETL_REST_API_CORS_ALLOWED_ORIGINS` - CORS allowed origins
- `MU_DATA_APP_HOST` - Music Data Master host
- `MU_DATA_APP_PORT` - Music Data Master port

### Application Properties

```yaml
# Approval thresholds per entity type (used by maintenance task)
lastfm.tasks.maintenance.db.thresholds.{artist|album|track|tag}.{metric-name}: <number>

# Scheduled tasks
lastfm.scheduling.maintenance.db.cron: "<cron-expression>"

# Metrics update intervals (milliseconds)
lastfm.metrics.update.{metric-type}.interval: <milliseconds>

# Master Data integration (for unbinding deleted entities)
master.base-url: ${MU_DATA_APP_HOST}:${MU_DATA_APP_PORT}
lastfm.tasks.maintenance.db.unbind.batch-size: <number>
```


## Build & Deployment

**See**: [Gradle Commands Guide](../../../../../../docs/kb/guides/gradle-commands.md) for standard build/test commands

**See**: [Docker Deployment Guide](../../../../../../env/docker/README.md) for deployment procedures


## Testing

The module includes comprehensive test coverage:

- Controller tests following the [Controller Testing Pattern](../../../../../../docs/kb/patterns/backend/testing/testing-controllers.md):
  - Unit tests for controller logic
  - MVC Tests: Spring Boot MVC tests for HTTP endpoints
- Services unit-tests
- Maintenance Tests: Database maintenance integration and unit tests


## Related Documentation

- [LastFM ETL Pipeline](../README.md) - Parent ETL pipeline overview
- [LastFM Modules Overview](../../README.md) - All LastFM modules
