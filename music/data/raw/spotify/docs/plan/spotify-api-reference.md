# Spotify Web API Reference (Post-February 2026)

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md)

This document consolidates the current state of the Spotify Web API as of March 2026, after the sweeping changes introduced in February 2026. It serves as a reference for designing our data collection pipeline.

## Authentication

### Client Credentials Flow

This is the flow we'll use — it provides access to public data without user interaction.

```
POST https://accounts.spotify.com/api/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id={CLIENT_ID}
&client_secret={CLIENT_SECRET}
```

Returns an access token valid for 1 hour. Must be refreshed before expiry.

**Development Mode Restrictions** (applies to all new apps):
- App owner must have active Spotify Premium subscription
- Max 1 Client ID per developer account
- Max 5 authenticated users per app (irrelevant for client credentials flow)

**Extended Quota Mode**: Requires 250,000 MAU, registered business entity, active launched service. Not applicable for our use case.

## Rate Limits

- **250 requests per 30 seconds** per `client_id` (~8.3 requests/second)
- Rate limit is a rolling window
- On exceeding: HTTP 429 with `Retry-After` header (value in seconds)
- Recommendation: target 7 req/s to leave headroom

## Available Endpoints (Relevant to Data Collection)

### GET /artists/{id}

Returns a single artist.

**Response fields** (post-Feb 2026):
```json
{
  "id": "0OdUWJ0sBjDrqHygGUXeCF",
  "name": "Band of Horses",
  "type": "artist",
  "uri": "spotify:artist:0OdUWJ0sBjDrqHygGUXeCF",
  "external_urls": {
    "spotify": "https://open.spotify.com/artist/0OdUWJ0sBjDrqHygGUXeCF"
  },
  "genres": ["indie folk", "indie rock", "stomp and holler"],
  "images": [
    {"url": "https://i.scdn.co/image/...", "height": 640, "width": 640},
    {"url": "https://i.scdn.co/image/...", "height": 320, "width": 320},
    {"url": "https://i.scdn.co/image/...", "height": 160, "width": 160}
  ],
  "href": "https://api.spotify.com/v1/artists/0OdUWJ0sBjDrqHygGUXeCF"
}
```

**Removed fields**: `popularity` (INTEGER 0-100), `followers` (object with `total`).

### GET /artists/{id}/albums

Returns paginated list of an artist's albums.

**Parameters**:
- `include_groups`: Filter by album type. Comma-separated: `album`, `single`, `appears_on`, `compilation`. Default: all.
- `limit`: Max 50 (unchanged), default 20
- `offset`: Pagination offset

**Response**: Paginated object with `items[]` array of simplified album objects.

Each album item:
```json
{
  "id": "1kEoKGWmKRMfBSbDmVQxe9",
  "name": "Everything All the Time",
  "album_type": "album",
  "total_tracks": 10,
  "release_date": "2006-03-21",
  "release_date_precision": "day",
  "images": [...],
  "external_urls": {...},
  "uri": "spotify:album:...",
  "artists": [
    {"id": "0OdUWJ0sBjDrqHygGUXeCF", "name": "Band of Horses", ...}
  ]
}
```

**Removed fields from album**: `album_group` (was only in artist's albums context), `available_markets`, `external_ids`, `label`, `popularity`.

### GET /albums/{id}

Returns a single album with full details including track listing.

**Response fields**:
```json
{
  "id": "...",
  "name": "...",
  "album_type": "album",
  "total_tracks": 10,
  "release_date": "2006-03-21",
  "release_date_precision": "day",
  "images": [...],
  "external_urls": {...},
  "uri": "spotify:album:...",
  "artists": [{...}],
  "tracks": {
    "items": [
      {
        "id": "...",
        "name": "The First Song",
        "track_number": 1,
        "disc_number": 1,
        "duration_ms": 234567,
        "explicit": false,
        "artists": [{...}],
        ...
      }
    ],
    "limit": 50,
    "offset": 0,
    "total": 10
  },
  "copyrights": [{...}],
  "genres": []
}
```

**Note**: Albums include a `tracks` paging object with the first page of tracks. For albums with >50 tracks, pagination via `GET /albums/{id}/tracks` is needed.

### GET /albums/{id}/tracks

Returns paginated tracks for an album.

**Parameters**:
- `limit`: Max 50, default 20
- `offset`: Pagination offset

**Response**: Paginated simplified track objects (no album reference — implied by the endpoint).

### GET /tracks/{id}

Returns a single track.

**Response fields**:
```json
{
  "id": "...",
  "name": "The Funeral",
  "duration_ms": 325000,
  "track_number": 3,
  "disc_number": 1,
  "explicit": false,
  "is_playable": true,
  "preview_url": "https://p.scdn.co/mp3-preview/...",
  "external_urls": {...},
  "uri": "spotify:track:...",
  "artists": [
    {"id": "...", "name": "Band of Horses", ...}
  ],
  "album": {
    "id": "...",
    "name": "Everything All the Time",
    ...
  }
}
```

**Removed fields**: `popularity`, `external_ids` (ISRC, EAN, UPC), `available_markets`, `linked_from`.

### GET /search

Search the Spotify catalog.

**Parameters**:
- `q`: Search query. Supports field filters: `artist:`, `album:`, `track:`, `year:`, `genre:`, `isrc:`, `upc:`, `tag:hipster`, `tag:new`
- `type`: Required. Comma-separated: `artist`, `album`, `track`, `playlist`, `show`, `episode`, `audiobook`
- `limit`: **Max 10** (reduced from 50), default 5
- `offset`: Max 1000

**Response**: Object with result arrays per type.

```json
{
  "artists": {
    "items": [...],
    "limit": 10,
    "offset": 0,
    "total": 42
  }
}
```

**Important**: The limit reduction to 10 means we need more pagination requests for broad searches. For targeted artist lookups by name, the first few results are usually sufficient.

## Removed Endpoints (Previously Useful for Data Collection)

| Endpoint | Removed When | Impact |
|----------|-------------|--------|
| `GET /artists/{id}/related-artists` | Nov 2024 | **Critical** — was the primary graph growth vector |
| `GET /artists/{id}/top-tracks` | Feb 2026 | Cannot get an artist's most popular tracks |
| `GET /recommendations` | Nov 2024 | Cannot use seed-based recommendations |
| `GET /artists` (batch) | Feb 2026 | Must fetch artists one-by-one |
| `GET /albums` (batch) | Feb 2026 | Must fetch albums one-by-one |
| `GET /tracks` (batch) | Feb 2026 | Must fetch tracks one-by-one |
| `GET /browse/new-releases` | Feb 2026 | Cannot discover new releases |
| `GET /browse/categories` | Feb 2026 | Cannot browse genre categories |
| `GET /audio-features/{id}` | Nov 2024 | Cannot get audio analysis data |
| `GET /audio-analysis/{id}` | Nov 2024 | Cannot get detailed audio analysis |
| `GET /markets` | Feb 2026 | Cannot list available markets |

## Removed Fields Summary

| Entity | Removed Fields | Impact |
|--------|---------------|--------|
| Artist | `popularity`, `followers` | No quality/ranking signal from Spotify |
| Album | `popularity`, `label`, `available_markets`, `album_group`, `external_ids` | No popularity, no label info, no market data |
| Track | `popularity`, `external_ids` (ISRC), `available_markets`, `linked_from` | No popularity, no ISRC cross-referencing |
| User (`/me`) | `country`, `email`, `followers`, `product`, `explicit_content` | N/A for our pipeline |

## Remaining Value Proposition

Despite the restrictions, Spotify still provides unique data not available from LastFM:

| Data | Value |
|------|-------|
| **Genres** (on artist) | LastFM uses tags (user-generated, noisy). Spotify genres are curated. |
| **Album structure** | Album type (album/single/compilation), release date with precision, total tracks |
| **Track metadata** | Duration, track/disc number, explicit flag, preview URL |
| **Images** | High-quality artist and album images in multiple sizes |
| **Spotify IDs** | Enable binding to the Spotify ecosystem for future integrations |
| **Artist → Album → Track graph** | Complete discography structure |
| **Featured/Contributing artists** | Collaboration graph (who appears on whose tracks) |

## Pagination Strategy

Since batch endpoints are removed and search is limited to 10/page:

**For artist albums** (`GET /artists/{id}/albums`):
- Limit is still 50 per page
- Most artists have <200 albums (including singles) → 4 pages max
- Budget: 1 + ceil(total/50) calls per artist for full discography

**For album tracks** (`GET /albums/{id}/tracks`):
- Limit is still 50 per page
- Most albums have <50 tracks → 1 page
- Budget: usually included in `GET /albums/{id}` response

**For search** (`GET /search`):
- Limit reduced to 10 per page
- For artist name lookup: first page (10 results) is usually sufficient
- For broader discovery: up to offset 1000 → max 100 pages

## Sources

- [Spotify Web API Reference](https://developer.spotify.com/documentation/web-api)
- [February 2026 Changelog](https://developer.spotify.com/documentation/web-api/references/changes/february-2026)
- [February 2026 Migration Guide](https://developer.spotify.com/documentation/web-api/tutorials/february-2026-migration-guide)
- [Rate Limits](https://developer.spotify.com/documentation/web-api/concepts/rate-limits)
- [Quota Modes](https://developer.spotify.com/documentation/web-api/concepts/quota-modes)
- [API Calls Concepts](https://developer.spotify.com/documentation/web-api/concepts/api-calls)
