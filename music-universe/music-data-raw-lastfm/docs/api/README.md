# API Methods Technical Documentation

This document provides detailed technical information about LastFM API methods implementation, including entity selection logic, result filtering, and data collection patterns.

## Implementation Status

### Currently Implemented ✅
1. **tag.getTopTags** - Entry point for tag discovery
2. **tag.getTopArtists** - Creates tag-artist relationships  
3. **tag.getTopTracks** - Creates tag-track relationships
4. **artist.getInfo** - Artist details with stats
5. **artist.getTopTags** - Creates artist-tag relationships
6. **artist.getTopTracks** - Creates artist-track relationships
7. **artist.getTopAlbums** - Creates artist-album relationships
8. **artist.getSimilar** - Creates artist similarity relationships
9. **artist.search** - Artist discovery

### Not Yet Implemented ❌
1. **album.getInfo** - Album details with tracks and tags
2. **album.getTopTags** - Creates album-tag relationships
3. **track.getInfo** - Track details with album and tags
4. **track.getTopTags** - Creates track-tag relationships
5. **tag.getInfo** - Tag details with wiki information
6. **tag.getTopAlbums** - Creates tag-album relationships

## Methods Reference

[Method response schema descriptions](methods/schemas/README.md) | [Implementation guides](methods/implementation/README.md)

| Method | API Call Generation | Result Filtering | Entities & Attributes Collected |
|--------|-------------------|------------------|--------------------------------|
| **tag.getTopTags** | Pagination-based: offsets 0-2000 by 50<br/>Skip existing pending calls<br/>Not entity-scoped | No filtering<br/>All returned tags saved | **Tags**: name, usage_count, usage_users_count<br/>**Attributes**: rank |
| **tag.getTopArtists** | Tags by usage_count DESC<br/>No pending calls | No filtering<br/>All artists saved | **Artists**: name, mbid, url<br/>**Relations**: artist-tag<br/>**Attributes**: rank |
| **tag.getTopTracks** | Tags by usage_count DESC<br/>Dynamic pages by usage/50K ratio<br/>No pending calls | URL validation only<br/>No quality thresholds | **Tracks**: name, mbid, url, duration<br/>**Artists**: name, mbid, url<br/>**Relations**: artist-track |
| **tag.getInfo** | *Not implemented* | *Not implemented* | *Not implemented* |
| **tag.getTopAlbums** | *Not implemented* | *Not implemented* | *Not implemented* |
| **artist.getInfo** | **Priority 1**: Approved artists<br/>**Priority 2**: Top artists missing stats<br/>**Priority 3**: Similar artists missing stats<br/>**Priority 4**: Other pending missing stats<br/>MBID deduplication | No filtering<br/>Self-reference exclusion | **Artists**: name, mbid, url, listeners_count, play_count<br/>**Tags**: name, url<br/>**Artists**: similar artists<br/>**Relations**: artist-tag, artist-artist |
| **artist.getSimilar** | Artists by listeners_count DESC<br/>MBID deduplication<br/>No pending calls | Match coefficient > 0.2<br/>Self-reference exclusion<br/>Artist consistency check | **Artists**: name, mbid, url<br/>**Relations**: artist-artist with match_score |
| **artist.getTopTags** | Artists by listeners_count DESC<br/>MBID deduplication<br/>No pending calls | Tag usage_count >= 10 | **Tags**: name, url<br/>**Relations**: artist-tag with usage_count |
| **artist.getTopAlbums** | Artists by listeners_count DESC<br/>MBID deduplication<br/>No pending calls | Album play_count >= 10,000<br/>Artist consistency check | **Albums**: name, mbid, url, play_count<br/>**Relations**: artist-album |
| **artist.getTopTracks** | Artists by listeners_count DESC<br/>MBID deduplication<br/>No pending calls | Track listeners_count >= 1,000 | **Tracks**: name, mbid, url, listeners_count, play_count<br/>**Artists**: name, mbid, url<br/>**Relations**: artist-track |
| **artist.search** | Unprocessed search requests<br/>processed = false | Name similarity > 0.5 vs search string<br/>Levenshtein distance | **Artists**: name, mbid, url, listeners_count |
| **album.getInfo** | *Not implemented* | *Not implemented* | *Not implemented* |
| **album.getTopTags** | *Not implemented* | *Not implemented* | *Not implemented* |
| **track.getInfo** | *Not implemented* | *Not implemented* | *Not implemented* |
| **track.getTopTags** | *Not implemented* | *Not implemented* | *Not implemented* |

## Entity Attributes Mapping

### Legend
- ✅ **IMPLEMENTED** - Method is implemented and attribute is collected
- 🔄 **AVAILABLE** - Method provides this attribute but we don't collect it
- ❌ **NOT_AVAILABLE** - Method doesn't provide this attribute
- 🚫 **NOT_IMPLEMENTED** - Method is not implemented

### Artist Entity

| Attribute | artist.getInfo | artist.getSimilar | artist.getTopTags | artist.getTopAlbums | artist.getTopTracks | artist.search | tag.getTopArtists | tag.getTopTracks |
|-----------|----------------|-------------------|-------------------|---------------------|---------------------|---------------|-------------------|------------------|
| **name** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| **mbid** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| **url** | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ |
| **listeners_count** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| **play_count** | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

**Primary Sources:**
- **name, mbid, url**: artist.getInfo, artist.getSimilar, artist.search, tag.getTopArtists, tag.getTopTracks
- **listeners_count**: artist.getInfo, artist.search
- **play_count**: artist.getInfo (only source)

### Album Entity

| Attribute | album.getInfo | artist.getTopAlbums | tag.getTopAlbums |
|-----------|---------------|---------------------|------------------|
| **name** | 🚫 | ✅ | 🚫 |
| **mbid** | 🚫 | ✅ | 🚫 |
| **url** | 🚫 | ✅ | 🚫 |
| **listeners_count** | 🚫 | ❌ | 🚫 |
| **play_count** | 🚫 | ✅ | 🚫 |

**Primary Sources:**
- **name, mbid, url, play_count**: artist.getTopAlbums (only implemented source)
- **listeners_count**: NOT AVAILABLE in any implemented method

### Track Entity

| Attribute | track.getInfo | artist.getTopTracks | tag.getTopTracks |
|-----------|---------------|---------------------|------------------|
| **name** | 🚫 | ✅ | ✅ |
| **mbid** | 🚫 | ✅ | ✅ |
| **url** | 🚫 | ✅ | ✅ |
| **duration** | 🚫 | ❌ | ✅ |
| **listeners_count** | 🚫 | ✅ | ❌ |
| **play_count** | 🚫 | ✅ | ❌ |

**Primary Sources:**
- **name, mbid, url**: artist.getTopTracks, tag.getTopTracks
- **listeners_count, play_count**: artist.getTopTracks (only source)
- **duration**: tag.getTopTracks (only source)

### Tag Entity

| Attribute | tag.getInfo | tag.getTopTags | artist.getInfo | artist.getTopTags |
|-----------|-------------|----------------|----------------|-------------------|
| **name** | 🚫 | ✅ | ✅ | ✅ |
| **url** | 🚫 | 🔄 | ✅ | ✅ |
| **usage_count** | 🚫 | ✅ | ❌ | ❌ |
| **usage_users_count** | 🚫 | ✅ | ❌ | ❌ |

**Primary Sources:**
- **name**: tag.getTopTags, artist.getInfo, artist.getTopTags
- **url**: Constructed from name (tag.getTopTags), artist.getInfo, artist.getTopTags
- **usage_count, usage_users_count**: tag.getTopTags (only source)

## Implementation Patterns

### Entity Selection Patterns
- **Standard**: No pending calls + popularity ordering
- **MBID Deduplication**: Artists only, priority by approval_status > listeners_count > id
- **Special Priority**: artist.getInfo uses 4-tier prioritization
- **Pagination**: tag.getTopTags uses offset-based pagination
- **Search-based**: artist.search uses separate request entities

### Filtering Patterns
- **Quality Thresholds**: Numeric minimums for counts/scores
- **Consistency Checks**: Artist matching, self-reference exclusion
- **Similarity Scoring**: String/coefficient-based relevance
- **Basic Validation**: URL/null checks
- **No Filtering**: Some methods save all results

### Data Collection Patterns
- **Primary Entities**: Direct from API response
- **Nested Entities**: Artists from tracks, tags from artists
- **Relationships**: Created between primary and nested entities
- **Attributes**: Numeric values, ranks, scores stored separately

## Critical Data Gaps

### Missing Primary Sources
1. **Album listeners_count** - No implemented method provides this
2. **Track statistics from tags** - tag.getTopTracks doesn't provide listeners/playcount
3. **Album-Track relationships** - album.getInfo not implemented
4. **Track-Tag relationships** - track.getInfo/getTopTags not implemented

### Single Point of Failure
1. **Artist play_count** - Only from artist.getInfo
2. **Album data** - Only from artist.getTopAlbums
3. **Tag usage statistics** - Only from tag.getTopTags
4. **Track listeners/playcount** - Only from artist.getTopTracks

## Configuration

### Thresholds
**Purpose**: Quality control filters that prevent collection of low-quality or irrelevant data. These thresholds ensure we only store entities and relationships that meet minimum popularity, relevance, or accuracy criteria, reducing noise in the dataset and improving overall data quality.

- **`lastfm.client.methods.artist.getSimilar.artistMatchThreshold`** - Minimum similarity coefficient for related artists
- **`lastfm.client.methods.artist.topTags.tagUsageCountThreshold`** - Minimum usage count for artist tags
- **`lastfm.client.methods.artist.topAlbums.albumPlayCountThreshold`** - Minimum play count for artist albums
- **`lastfm.client.methods.artist.topTracks.trackListenersThreshold`** - Minimum listeners count for artist tracks
- **`lastfm.client.methods.artist.search.artistSimilarityThreshold`** - Minimum name similarity for search results

### Due Duration (Refresh Intervals)
**Purpose**: Controls how frequently we refresh data for each API method. Shorter intervals ensure fresher data but increase API usage, while longer intervals reduce API calls but may result in stale data. Different methods have different refresh rates based on how frequently the underlying data changes.

- **`lastfm.client.methods.artist.getInfo.dueDurationDays`** - Artist basic information refresh
- **`lastfm.client.methods.artist.getSimilar.dueDurationDays`** - Artist similarity relationships refresh
- **`lastfm.client.methods.artist.topTags.dueDurationDays`** - Artist tags refresh
- **`lastfm.client.methods.artist.topAlbums.dueDurationDays`** - Artist albums refresh
- **`lastfm.client.methods.artist.topTracks.dueDurationDays`** - Artist tracks refresh
- **`lastfm.client.methods.tag.topTags.dueDurationDays`** - Global tags refresh
- **`lastfm.client.methods.tag.topArtists.dueDurationDays`** - Tag artists refresh
- **`lastfm.client.methods.tag.topTracks.dueDurationDays`** - Tag tracks refresh

## Recommendations

### High Priority Implementations
1. **album.getInfo** - For album-track relationships and complete album data
2. **track.getInfo** - For track-tag relationships and complete track statistics
3. **tag.getInfo** - For tag descriptions and detailed statistics

### Data Quality Improvements
1. Add fallback sources for critical single-source attributes
2. Implement missing relationship endpoints
3. Add data validation across multiple sources
4. Standardize filtering patterns across methods
