# Spotify Data Collection: Implementation Plan

## Table of Contents

1. [Context & Constraints](#1-context--constraints) → [Spotify API Reference](spotify-api-reference.md)
2. [Architecture Overview](#2-architecture-overview)
3. [Module Structure](#3-module-structure)
4. [Staging Layer Design](#4-staging-layer-design) → [detailed doc](staging-layer-design.md)
5. [Synthetic ID Generation](#5-synthetic-id-generation) → [detailed doc](synthetic-ids.md)
6. [Kafka Integration](#6-kafka-integration) → [detailed doc](kafka-integration.md)
7. [Graph Growth Strategy](#7-graph-growth-strategy)
8. [Database Schema](#8-database-schema) → [detailed doc](database-schema.md)
9. [Differences from LastFM Pipeline](#9-differences-from-lastfm-pipeline)
10. [Implementation Phases](#10-implementation-phases)

---

## 1. Context & Constraints

### Spotify API State (Post-February 2026)

The Spotify Web API underwent massive restrictions in February 2026. The available endpoints relevant to data collection are:

| Endpoint | Method | Notes |
|----------|--------|-------|
| `GET /artists/{id}` | Get Artist | **Fields removed**: `popularity`, `followers` |
| `GET /artists/{id}/albums` | Get Artist's Albums | **Fields removed**: `album_group` |
| `GET /albums/{id}` | Get Album | **Fields removed**: `popularity`, `label`, `available_markets` |
| `GET /albums/{id}/tracks` | Get Album Tracks | Returns simplified track objects |
| `GET /tracks/{id}` | Get Track | **Fields removed**: `popularity`, `external_ids`, `available_markets` |
| `GET /search` | Search | **Max limit: 10 per page** (was 50), pagination required |

**Removed endpoints** (no longer available):
- `GET /artists/{id}/related-artists` — **critical loss**, was the primary graph growth vector
- `GET /artists/{id}/top-tracks` — no longer available
- `GET /artists`, `GET /albums`, `GET /tracks` — batch endpoints removed, must fetch individually
- All browse/discovery endpoints (`new-releases`, `categories`)
- Recommendations endpoint

**Rate limits**: ~250 requests per 30 seconds (client credentials flow), ~8.3 req/s.

**Authentication**: Client Credentials flow (no user interaction needed for public data). Development mode allows only 5 authenticated users and requires Premium. Extended quota requires 250K MAU — unreachable for our use case.

### Implications for Design

1. **No organic graph growth** — without related-artists, we cannot discover new artists from Spotify alone. We must seed from external sources (LastFM, manual input).
2. **Individual fetches only** — no batch endpoints means one HTTP call per entity. Rate budget management is critical.
3. **Reduced metadata** — missing popularity/followers means Spotify is primarily a source for: genres, images, Spotify IDs (for binding), album/track structure, and audio features (if available).
4. **Search is throttled** — 10 results per page makes search-based discovery slow but still viable for targeted lookups.

---

## 2. Architecture Overview

```
                                 ┌─────────────────────────────────┐
                                 │      Manual / External Seed     │
                                 │  (LastFM bindings, user input)  │
                                 └────────────┬────────────────────┘
                                              │
                                              ▼
┌──────────────────────┐   Kafka    ┌──────────────────────┐
│  Calls Generator     │──────────→ │  Calls Performer     │
│                      │  (topics)  │                      │
│ - Scans DB for stale │            │ - Consumes from topic│
│   entities           │            │ - Executes HTTP calls│
│ - Scans seed queue   │            │ - Rate-limited       │
│ - Deduplicates       │            │ - Stores responses   │
│ - Produces to Kafka  │            │   in api_response    │
└──────────────────────┘            └──────────┬───────────┘
                                               │
                                               ▼
                                    ┌──────────────────────┐
                                    │  Response Parser      │
                                    │                       │
                                    │ - Parses JSON → DTOs  │
                                    │ - Writes to STAGING   │
                                    │   tables (hot/cold)   │
                                    │ - Synthetic IDs for   │
                                    │   new entities        │
                                    └──────────┬────────────┘
                                               │
                                               ▼
                                    ┌──────────────────────┐
                                    │  Staging Applicator   │
                                    │                       │
                                    │ - Swaps hot/cold      │
                                    │ - Applies staged data │
                                    │   to target tables    │
                                    │ - Resolves synthetic  │
                                    │   IDs → real IDs      │
                                    └──────────────────────┘
```

### Key Architectural Decisions

**Decision 1: Four services instead of three.**

LastFM merges response parsing and data application into a single service. For Spotify we separate them — the **Response Parser** writes to staging tables, and a new **Staging Applicator** service applies staged data to target tables. This provides:
- Clear separation of concerns (parsing vs applying)
- The applicator can run independently, retry failed applications
- Staging tables act as a buffer / audit trail

**Decision 2: Kafka for call distribution (hybrid approach).**

The Calls Generator still uses the database as the source of truth for "what needs to be fetched" (deduplication, staleness). But instead of the Performer polling the `api_call` table, the Generator publishes call requests to Kafka topics. This enables:
- Horizontal scaling of Performers (multiple consumers)
- Backpressure handling via consumer lag
- Priority topics (seed requests vs refresh requests)
- Decoupling generator throughput from performer rate limits

**Decision 3: Staging layer for all entity types, not just attribute history.**

Unlike LastFM where only attribute_history uses staging, Spotify will stage ALL writes (entities, relations, attributes). This provides:
- Atomic batch application (all-or-nothing per staging cycle)
- Ability to validate the full batch before committing
- Clean rollback if application fails partway through
- Audit trail of what was received vs what was applied

---

## 3. Module Structure

```
music/data/raw/spotify/
├── spotify-models/                    # Entities, DTOs, enums
│   └── src/main/java/.../
│       ├── entity/                    # JPA entities (SpotifyApiCall, SpotifyArtist, etc.)
│       ├── dto/                       # API response DTOs (deserialized Spotify JSON)
│       ├── enums/                     # SpotifyApiCallType, SpotifyEntityType
│       └── staging/                   # Staging record DTOs
│
├── spotify-repositories/              # Spring Data JPA repositories
│
├── etl/
│   ├── spotify-calls-generator/       # Service: generates API calls, publishes to Kafka
│   ├── spotify-calls-performer/       # Service: consumes Kafka, executes HTTP, stores responses
│   ├── spotify-response-parser/       # Service: parses responses, writes to staging tables
│   └── spotify-staging-applicator/    # Service: applies staging → target tables
│
├── spotify-rest-api/                  # REST API for UI/management (seed endpoints, status)
│
├── migrations/
│   └── spotify-liquibase-resources/   # Liquibase changesets for mu_raw_spotify schema
│
└── docs/
    └── plan/                          # This documentation
```

### Dependency Graph

```
spotify-models ← spotify-repositories ← etl services
      ↑                                      ↑
data-raw-commons-jpa               data-raw-commons-api-client
```

All ETL services depend on `spotify-repositories` and `commons-observability`.
The `spotify-calls-performer` additionally depends on `data-raw-commons-api-client`.
All ETL services depend on a shared Kafka library (Spring Kafka or a thin commons wrapper).

---

## 4. Staging Layer Design

See [staging-layer-design.md](staging-layer-design.md) for the full design.

**Summary**: For each target table (artist, album, track, entity_relation, attribute_history) there are two staging tables (A and B). While the Applicator reads from set A and applies to targets, the Parser writes new data to set B. On the next cycle they swap. Staging tables mirror target table columns plus metadata (batch_id, synthetic_entity_id, status).

---

## 5. Synthetic ID Generation

See [synthetic-ids.md](synthetic-ids.md) for the full design.

**Summary**: When the Response Parser encounters an entity not yet in the database, it generates a deterministic synthetic ID based on (entity_type, spotify_id) using a hash function mapped to the negative ID space. This allows staging records to reference each other before real IDs are assigned. The Applicator resolves synthetic → real IDs during application.

---

## 6. Kafka Integration

See [kafka-integration.md](kafka-integration.md) for the full design.

**Summary**: Two topic families — `spotify.calls.seed` (priority, for newly discovered entities) and `spotify.calls.refresh` (lower priority, for staleness-based refreshes). The Performer consumes with priority from seed topic. Rate limiter (token bucket at ~8 req/s) gates actual HTTP calls regardless of consumption speed.

---

## 7. Graph Growth Strategy

Since `related-artists` is gone, we need alternative discovery paths:

### 7.1 Cross-Source Seeding (Primary)

Our LastFM database already contains thousands of artists. For each LastFM artist:
1. **Search Spotify** for the artist by name → obtain Spotify ID
2. **Bind** Spotify ID to master entity via ArtistBinding (data_source=SPOTIFY)
3. **Fetch** artist details, albums, tracks from Spotify

This is the primary growth vector. The Calls Generator should have a "seed from LastFM" mode that:
- Queries master ArtistBinding for artists that have LASTFM binding but no SPOTIFY binding
- Generates `SEARCH_ARTIST` API calls for those artists
- On successful match, creates a binding and enqueues further calls (albums, tracks)

### 7.2 Album-Based Discovery (Secondary)

When fetching an artist's albums and then album tracks, we discover **featured artists** on tracks (the `artists` array on track objects includes all contributing artists). These are new artist Spotify IDs we can follow up on.

Flow: `Artist → Albums → Tracks → Featured Artists (new) → their Albums → ...`

This creates organic graph growth similar to the old related-artists endpoint, but through the collaboration graph rather than the listening similarity graph.

### 7.3 Manual Seeding (Tertiary)

REST API endpoint to accept Spotify IDs directly:
- `POST /api/v1/spotify/seed/artist/{spotifyId}`
- `POST /api/v1/spotify/seed/album/{spotifyId}`

### 7.4 Discovery Priority

```
Priority 1: Cross-source seeding (LastFM → Spotify search)
Priority 2: Featured artist discovery (from album tracks)
Priority 3: Manual seeding
Priority 4: Staleness refresh (re-fetch existing entities)
```

Rate budget allocation (of ~8.3 req/s):
- 60% for new entity discovery (Priority 1-3)
- 40% for staleness refresh (Priority 4)
- Configurable via application properties

---

## 8. Database Schema

See [database-schema.md](database-schema.md) for the full schema.

**Summary**: New `mu_raw_spotify` database/schema with tables for: artist, album, track, api_call, api_response, data_snapshot, entity_relation, attribute_history_current/archive, plus staging table pairs (A/B) for each target table. Separate from LastFM to avoid schema coupling.

---

## 9. Differences from LastFM Pipeline

| Aspect | LastFM | Spotify | Rationale |
|--------|--------|---------|-----------|
| **Call distribution** | DB polling | Kafka topics | Enables horizontal scaling of performers |
| **Staging scope** | Attribute history only | All entity types | Atomic batch application, audit trail |
| **ID generation** | DB sequences on insert | Synthetic IDs in staging, resolved on apply | Entities must reference each other in staging before they exist in target tables |
| **Response parsing + application** | Single service | Two services (parser + applicator) | Separation of concerns; applicator can retry independently |
| **Graph discovery** | API provides similar artists, top tags→artists | Cross-source seeding + album collaboration graph | Spotify removed discovery endpoints |
| **Rate limiting** | Simple delay between calls | Token bucket with priority queues via Kafka | More precise budget control, priority support |
| **Entity deduplication** | Name-based (complex) | Spotify ID-based (deterministic) | Spotify IDs are globally unique, no ambiguity |
| **Batch fetching** | N/A (LastFM has no batch) | N/A (Spotify removed batch) | Both require individual fetches |
| **Blacklisting** | Threshold-based (listeners, plays) | Minimal — no popularity/follower data available | Cannot quality-filter on missing fields |

### Lessons Learned from LastFM (Critical Improvements)

1. **Generator registry pattern** — Keep it. Self-registering generators via Spring beans is clean and extensible.

2. **Snapshot mechanism** — Simplify it. LastFM snapshots serve dual purpose (grouping + conflict prevention) which makes them hard to reason about. For Spotify, staging tables naturally provide grouping (batch_id) and conflict prevention (staging isolation). We can drop snapshots entirely or reduce them to simple batch metadata.

3. **Deduplication** — Much simpler with Spotify IDs. LastFM deduplication is complex because artists can be identified by name, MBID, or URL with priority logic. Spotify IDs are canonical — deduplication is a simple `spotify_id` uniqueness check.

4. **Reflection-based SQL generation** — Replace with explicit, typed SQL builders or jOOQ. The AbstractEntityRelationService approach of generating SQL from JPA annotations at runtime is clever but opaque. For Spotify, prefer explicit SQL/repository methods that are easier to debug and test.

5. **Attribute history processing** — Reuse the hot/cold CTE approach (it's genuinely well-designed) but generalize it. The PostgreSQL CTE pipeline for detecting changes, archiving old values, and inserting new ones is efficient. Package it as a reusable component rather than embedding it in a single service.

6. **Error tracking** — LastFM marks responses as PROCESSING_ERROR but doesn't track which specific entity/attribute failed. Spotify should have granular error tracking at the staging record level (status column per staging row).

7. **Quality validation** — LastFM uses threshold-based blacklisting (listeners_count < 1000 → blacklist). Since Spotify removed popularity/followers fields, we need different quality signals. Options:
   - Album count (artists with 0 albums are likely noise)
   - Genre presence (artists without genres may be incomplete)
   - Cross-reference with LastFM data quality
   - Manual approval workflow

---

## 10. Implementation Phases

### Phase 1: Foundation (Database + Models + Seeding)

**Goal**: Database schema, entity models, and manual seeding capability.

- Create `mu_raw_spotify` database and Liquibase migrations
- Implement `spotify-models` (entities, enums, DTOs)
- Implement `spotify-repositories`
- Implement `spotify-rest-api` with seed endpoints
- Basic Spotify HTTP client (extends `BaseHttpApiClient`) with OAuth2 client credentials
- No Kafka yet — direct DB-based flow for initial testing

### Phase 2: ETL Pipeline (Generator + Performer + Parser)

**Goal**: Working three-service pipeline with DB-based call distribution (Kafka comes later).

- Implement `spotify-calls-generator` with LastFM cross-source seeding
- Implement `spotify-calls-performer` with rate limiting
- Implement `spotify-response-parser` writing to staging tables
- Implement `spotify-staging-applicator` with hot/cold swap and synthetic ID resolution

### Phase 3: Kafka Integration

**Goal**: Replace DB polling with Kafka-based call distribution.

- Add Kafka infrastructure (topics, producers, consumers)
- Migrate Performer from DB polling to Kafka consumption
- Add priority queue logic (seed vs refresh)
- Add monitoring (consumer lag, throughput metrics)

### Phase 4: Graph Growth

**Goal**: Automated discovery beyond manual seeding.

- Implement featured-artist discovery from album tracks
- Implement automatic LastFM → Spotify search pipeline
- Add Spotify → master binding automation
- Add configurable rate budget allocation

### Phase 5: Observability & Hardening

**Goal**: Production readiness.

- Grafana dashboards for pipeline metrics
- Dead letter queue for failed processing
- Alerting on pipeline stalls
- Integration tests with WireMock for Spotify API
