# LastFM Data Collector

A Spring Boot application that collects music data from LastFM API for the Art Universe project.

## What it does

- **Discovers music entities** - Artists, albums, tracks, and tags from LastFM API
- **Builds relationships** - Creates connections between artists, tracks, tags, and albums
- **Change tracking** - Maintains attribute history over time (SCD2 pattern)

## Key Features

- **Rate-limited API client** - 1 call/sec with exponential backoff retry
- **Intelligent prioritization** - Approved entities first, then by popularity
- **Quality filtering** - Configurable thresholds for listeners, play counts, similarity scores
- **RESTful management API** - Search, filter, and approve entities via REST endpoints

## Data Collection Flow

1. **Tag Discovery** - Start with `tag.getTopTags` to find popular music tags
2. **Entity Discovery** - Use tags to find artists (`tag.getTopArtists`) and tracks (`tag.getTopTracks`)
3. **Entity Enrichment** - Get detailed info for artists (`artist.getInfo`, `artist.getTopAlbums`, etc.)
4. **Relationship Building** - Create connections between artists, tracks, tags, and albums
5. **Quality Control** - Manual approval workflow for collected entities

## Documenation

See **[Technical Documentation](docs/README.md)** for details

## Quick Start

### Prerequisites
- Java 17+
- Docker (for database and testing)
- LastFM API key

### Environment Setup

Create env files with secrets:
* POSTGRES_USER - postgres main user
* POSTGRES_PASSWORD - password of postgres main user
* MURAW_LASTFM_DB_PASSWORD - password of module's main user in postgres  
* MURAW_LASTFM_API_KEY - this is crucial

Secrets should be saved under the following paths:
* env/docker/local/music-data-raw-lastfm.secrets.env - dev & local env
* env/docker/prod/music-data-raw-lastfm.secrets.env - prod env


### Run Locally
```bash
# From project root directory
./scripts/run-module-dev.sh music-universe:music-data-raw-lastfm
# Application runs on http://localhost:7081
```

or, create an IDE run configuration that uses the following env files:
* .project-root.env
* ./env/docker/local/music-data-raw-lastfm.env
* ./env/docker/local/music-data-raw-lastfm.secrets.env
* ./music-universe/music-data-raw-lastfm/dev.override.env

### Docker Deployment
Deploy the whole stack using the following command.

```bash
# From project root directory
./env/docker/deploy.sh local    # Port 9081
./env/docker/deploy.sh prod     # Port 8081
```

## Management API

- **Artists**: `GET /api/v1/artists` - Search and filter artists
- **Tracks**: `GET /api/v1/tracks` - Search and filter tracks  
- **Tags**: `GET /api/v1/tags` - Search and filter tags
- **Approval**: `PATCH /api/v1/{entity}/{id}/approval` - Update approval status

## Configuration

Key configuration properties:
- **API Rate Limiting**: `lastfm.client.callsPerSec` (default: 1.0)
- **Quality Thresholds**: `lastfm.client.methods.*.threshold` 
- **Refresh Intervals**: `lastfm.client.methods.*.dueDurationDays`
- **Batch Sizes**: Various limits for API call generation

See [Configuration Reference](docs/api/README.md#configuration) for complete details.

## Integration

This module provides raw data for:
- **Music Data Module** - Consumes approved entities for binding
- **Music Universe UI** - Management interface for data approval
- **Music Quiz Module** - Uses approved data for quiz generation
