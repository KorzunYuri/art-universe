# Spotify ETL Pipeline

This directory contains the Spotify ETL pipeline modules that collect, process, and store data from the Spotify Web API.

## Pipeline Overview

The Spotify ETL pipeline is a **four-stage continuous data collection system** that collects data from Spotify Web API, stages it into per-iteration tables, and applies it to target tables.

**Stage 1: [Calls Generator](spotify-calls-generator/README.md)** -> Identifies entities needing data and creates API call tasks
- For each [supported method](#supported-spotify-web-api-methods):
  - Queries database for entities without active API calls (based on due duration)
  - Creates [API call tasks](../spotify-models/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/etl/entity/SpotifyApiCall.java) with `CREATED` status
- For [search methods](#search-methods):
  - Queries `mu_view` schema for master entities without Spotify bindings
  - Creates `search_attempt` records for deduplication and grace period tracking

**Stage 2: [Calls Performer](spotify-calls-performer/README.md)** -> Executes API calls and stores raw responses
- Picks up `CREATED` API call tasks
- Makes HTTP requests to Spotify Web API with OAuth2 client credentials and rate limiting
- Stores raw JSON responses in database with `PENDING` status
- Updates API call status to `SUCCESSFUL` or `FAILED`

**Stage 3: [Response Parser](spotify-response-parser/README.md)** -> Parses responses, writes to staging tables
- Reads API responses with `PENDING` status
- Extracts entities from JSON into per-iteration staging tables (`stg_artist_N`, `stg_album_N`, etc.)
- Manages staging iterations (open -> sealed lifecycle)
- For search responses: scores matches using Jaro-Winkler similarity and updates `search_attempt`

**Stage 4: [Staging Applicator](spotify-staging-applicator/README.md)** -> Applies staging data to target tables
- Picks up `SEALED` staging iterations
- Applies data in dependency order: artists -> albums -> tracks -> relations
- Resolves synthetic IDs between staging tables
- Reconciles matched search attempts by creating bindings in music-data-master via internal API

## Supported Spotify Web API Methods

### Entity GET Methods

| API Method | Call Type | Endpoint | Purpose |
|------------|-----------|----------|---------|
| `artist.get` | `ARTIST_GET` | `GET /artists/{id}` | Fetch full artist details |
| `artist.albums` | `ARTIST_ALBUMS` | `GET /artists/{id}/albums` | Fetch artist's albums |
| `album.get` | `ALBUM_GET` | `GET /albums/{id}` | Fetch full album details with embedded tracks |
| `album.tracks` | `ALBUM_TRACKS` | `GET /albums/{id}/tracks` | Fetch album's tracks |
| `track.get` | `TRACK_GET` | `GET /tracks/{id}` | Fetch full track details with embedded album |

### Search Methods

| API Method | Call Type | Endpoint | Purpose |
|------------|-----------|----------|---------|
| `search.artist` | `SEARCH_ARTIST` | `GET /search?type=artist` | Search for artists by name |
| `search.album` | `SEARCH_ALBUM` | `GET /search?type=album` | Search for albums by name |
| `search.track` | `SEARCH_TRACK` | `GET /search?type=track` | Search for tracks by name |

Search methods are driven by master entities (from `mu` schema) that lack Spotify bindings. The generator queries `mu_view` read-only views to discover them.

## Key Concepts

### Staging Layer

Unlike LastFM which updates entities in-place, Spotify uses an intermediate **staging layer**. Each parser cycle writes into per-iteration staging tables cloned from templates:

- `stg_artist_{N}` - staged artist records
- `stg_album_{N}` - staged album records
- `stg_track_{N}` - staged track records
- `stg_entity_relation_{N}` - staged entity relation records

Iterations have a lifecycle: `OPEN` -> `SEALED` -> `APPLYING` -> `COMPLETED`/`FAILED`.

### Synthetic ID Resolution

When the parser stages a record referencing an entity that doesn't yet exist in the database (e.g., an album references an artist not yet inserted), it generates a negative **synthetic ID** via `SyntheticIdGenerator`. The applicator later resolves these to real database IDs after upserting the referenced entities.

### Data Staleness Model

For each Spotify API method there is a configured **due duration** parameter that determines how often API calls should be executed.

1. Generator finds entities without active (unexpired) API calls of a given type
2. Creates new API calls with `due_dttm = now() + due_duration`
3. No new calls are created for that entity/method until `due_dttm` expires

Watch [Calls Generator config](spotify-calls-generator/src/main/resources/application.yml) for exact values.

### Search Pipeline

The search pipeline bridges the Spotify raw domain with the master domain:

1. Generator queries `mu_view.v_artist` / `mu_view.v_album` / `mu_view.v_track` for master entities without Spotify bindings (`mu_view.v_{entity}_binding` with `data_source_id = 2`)
2. Creates `search_attempt` records (status=PENDING) and corresponding search API calls
3. Performer calls `GET /search?q=...&type=...`
4. Parser stages results and scores matches using Jaro-Winkler string similarity
5. If best match >= threshold: `search_attempt.status = MATCHED`
6. Applicator upserts staged entities, then reconciles MATCHED attempts by calling music-data-master internal API to create bindings with `origin=ETL, approval_status=PENDING`

### Graph Growth

The Spotify entity graph grows organically through response parsing:
- `ALBUM_GET` responses contain embedded `artists[]` and `tracks.items[]`
- `TRACK_GET` responses contain embedded `album` object
- All responses stage simplified artist records for featured/collaborating artists
- The generator discovers new entities (without active calls) on each cycle

## Data Flow Example

Let's trace how a search-driven binding gets created:

```
Day 0: Master artist "Radiohead" exists in mu.artist but has no Spotify binding
  |-> Calls Generator: Queries mu_view.v_artist LEFT JOIN mu_view.v_artist_binding
  |-> Calls Generator: Creates search_attempt(entity_type=ARTIST, master_entity_id=42, status=PENDING)
  |-> Calls Generator: Creates api_call(type=SEARCH_ARTIST, params={q:"Radiohead", type:"artist"})

  |-> Calls Performer: Picks up CREATED call
  |-> Calls Performer: Makes GET /search?q=Radiohead&type=artist&limit=20
  |-> Calls Performer: Stores JSON response, sets status=SUCCESSFUL

  |-> Response Parser: Reads PENDING response
  |-> Response Parser: Stages 20 artists into stg_artist_1
  |-> Response Parser: Scores Jaro-Winkler("radiohead", "Radiohead") = 1.0 >= 0.85
  |-> Response Parser: Updates search_attempt(status=MATCHED, matched_spotify_id="4Z8W4fKeB5YxbusRsdQVPb")

  |-> Staging Applicator: Applies SEALED iteration -> upserts artists into target table
  |-> Staging Applicator: Reconciles MATCHED search_attempt
  |-> Staging Applicator: Calls POST /api/v1/internal/artists/bind/existing/spotify/{id}
  |-> Result: mu.artist_binding created (origin=ETL, approval_status=PENDING)
```

## Configuration

### Common Environment Variables

All ETL modules share these database configuration variables:

- `AU_DB_MASTER_HOST` - PostgreSQL master host
- `AU_DB_MASTER_PORT` - PostgreSQL master port
- `AU_DB_NAME` - Database name (`art_universe`)
- `MURAW_SPOTIFY_DB_SCHEMA` - Schema name (`mu_raw_spotify`)
- `MURAW_SPOTIFY_DB_WRITER_USERNAME` - Spotify schema editor user
- `MURAW_SPOTIFY_DB_WRITER_PASSWORD` - Spotify schema editor password

### Search Pipeline Variables

- `SPOTIFY_SEARCH_ENABLED` - Enable/disable search generators (default: `false`)
- `SPOTIFY_SEARCH_BATCH_SIZE` - Max unbound entities per generator cycle (default: `100`)
- `SPOTIFY_SEARCH_GRACE_PERIOD_DAYS` - Days before retrying a NO_MATCH search (default: `30`)
- `SPOTIFY_SEARCH_MATCH_THRESHOLD` - Minimum Jaro-Winkler score for a match (default: `0.85`)
- `MUSIC_DATA_MASTER_INTERNAL_URL` - URL for music-data-master internal API

### Development Environment Setup

When running ETL modules in dev mode (IntelliJ), environment variables are loaded in this order:
1. `env/docker/common/music-data-raw-spotify.env` - Spotify constants
2. `env/docker/common/music-data-raw-spotify-etl.env` - ETL-specific constants
3. `env/docker/dev/common.env` - Dev common settings
4. `env/docker/dev/music-data-raw-spotify.secrets.env` - Dev secrets (Git-ignored)

### Module-Specific Configuration

Each module has additional configuration - see individual module READMEs:
- [Calls Generator Configuration](spotify-calls-generator/README.md#run-configuration)
- [Calls Performer Configuration](spotify-calls-performer/README.md#run-configuration)
- [Response Parser Configuration](spotify-response-parser/README.md#run-configuration)
- [Staging Applicator Configuration](spotify-staging-applicator/README.md#run-configuration)

## Related Documentation

- [Spotify Modules Overview](../README.md) - Parent directory with all Spotify modules
- [Spotify Models](../spotify-models/README.md) - JPA entities used by all ETL modules
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
