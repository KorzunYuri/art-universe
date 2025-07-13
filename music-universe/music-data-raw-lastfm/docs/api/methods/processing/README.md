# LastFM API Response Processing Documentation

This directory contains detailed processing documentation for each LastFM API method. Each file describes the schema analysis and step-by-step processing algorithm for implementing `LastfmApiResponseProcessor` classes.

## Currently Implemented Methods ✅

1. **[tag.getTopTags](tag.getTopTags.md)** - Entry point for tag discovery
2. **[tag.getTopArtists](tag.getTopArtists.md)** - Creates tag-artist relationships  
3. **[tag.getTopTracks](tag.getTopTracks.md)** - Creates tag-track relationships
4. **[artist.getInfo](artist.getInfo.md)** - Artist details with stats
5. **[artist.getTopTags](artist.getTopTags.md)** - Creates artist-tag relationships
6. **[artist.getTopTracks](artist.getTopTracks.md)** - Creates artist-track relationships
7. **[artist.getTopAlbums](artist.getTopAlbums.md)** - Creates artist-album relationships
8. **[artist.getSimilar](artist.getSimilar.md)** - Creates artist similarity relationships
9. **[artist.search](artist.search.md)** - Artist discovery

## Not Yet Implemented Methods ❌

10. **[album.getInfo](album.getInfo.md)** - Album details with tracks and tags
11. **[album.getTopTags](album.getTopTags.md)** - Creates album-tag relationships
12. **[track.getInfo](track.getInfo.md)** - Track details with album and tags
13. **[track.getTopTags](track.getTopTags.md)** - Creates track-tag relationships
14. **[tag.getInfo](tag.getInfo.md)** - Tag details with wiki information
15. **[tag.getTopAlbums](tag.getTopAlbums.md)** - Creates tag-album relationships

## Documentation Structure

Each documentation file contains:

1. **Method Information** - API method name, scope entity, response schema
2. **Extractable Entities** - What entities can be created/updated
3. **Extractable Attributes** - What attributes can be extracted for each entity
4. **Extractable Relationships** - What relationships can be created
5. **Relationship Attributes** - What attributes relationships can have
6. **Processing Algorithm** - Step-by-step implementation guide
7. **Implementation Notes** - Important considerations and patterns

## Key Patterns

### Entity Identification
- **Artists**: Identified by `name` (unique key)
- **Albums**: Identified by `url` (unique key)  
- **Tracks**: Identified by `url` (unique key)
- **Tags**: Identified by `name` (unique key)

### Attribute Types
- **SCD2 Attributes**: Track changes over time (most attributes)
- **Snapshot Attributes**: Captured per API call (rank attributes)

### Relationship Types
- **ArtistTag**: Artist-tag associations with usage counts/ranks
- **TrackTag**: Track-tag associations with usage counts/ranks
- **AlbumTag**: Album-tag associations with usage counts/ranks
- **ArtistArtist**: Artist similarity relationships with match scores
- **ArtistAlbum**: Artist-album associations
- **AlbumTrack**: Album-track associations with track positions

## Next Steps

1. Review and refine processing documentation
2. Design utility classes and shared components
3. Plan implementation order based on dependencies
4. Begin implementing new relationship entities and processors
