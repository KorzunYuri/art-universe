# Music Data Raw LastFM - Entity and Relationship Refactoring Plan

## Current State Analysis

### API Methods

#### Currently Implemented
1. **tag.getTopTags** ✅ - Entry point for tag discovery
2. **tag.getTopArtists** ✅ - Creates tag-artist relationships
3. **tag.getTopTracks** ✅ - Creates tag-track relationships
4. **artist.getInfo** ✅ - Artist details with stats
5. **artist.getTopTags** ✅ - Creates artist-tag relationships
6. **artist.getTopTracks** ✅ - Creates artist-track relationships
7. **artist.getTopAlbums** ✅ - Creates artist-album relationships
8. **artist.getSimilar** ✅ - Creates artist similarity relationships
9. **artist.search** ✅ - Artist discovery

#### Not Yet Implemented
1. **album.getInfo** ❌ - Album details with tracks and tags
2. **album.getTopTags** ❌ - Creates album-tag relationships
3. **track.getInfo** ❌ - Track details with album and tags
4. **track.getTopTags** ❌ - Creates track-tag relationships
5. **tag.getInfo** ❌ - Tag details with wiki information
6. **tag.getTopAlbums** ❌ - Creates tag-album relationships

#### Resources:
- Response schemas: refactoring/apiclient/responses/schemas.
- Response examples: src/test/resources/apiclient/responses.
