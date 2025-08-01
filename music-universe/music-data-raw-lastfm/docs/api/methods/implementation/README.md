# LastFM API Response Processing Implementation Guides

This directory contains detailed step-by-step implementation guides for `LastfmApiResponseProcessor` classes. Each file provides the specific processing algorithm needed to implement response processors for LastFM API methods.

> **See also**: [API Methods Technical Documentation](../../README.md) for high-level overview and method status.

## Purpose

These guides are designed for developers implementing new API response processors. Each guide contains:

1. **Method Information** - API method name, scope entity, response schema
2. **Extractable Entities** - What entities can be created/updated from the response
3. **Extractable Attributes** - What attributes can be extracted for each entity
4. **Extractable Relationships** - What relationships can be created between entities
5. **Relationship Attributes** - What attributes relationships can have
6. **Processing Algorithm** - Step-by-step implementation instructions
7. **Implementation Notes** - Important considerations and patterns

## Implementation Guides

### Currently Implemented Methods
- **[tag.getTopTags](tag.getTopTags.md)** - Entry point for tag discovery
- **[tag.getTopArtists](tag.getTopArtists.md)** - Creates tag-artist relationships  
- **[tag.getTopTracks](tag.getTopTracks.md)** - Creates tag-track relationships
- **[artist.getInfo](artist.getInfo.md)** - Artist details with stats
- **[artist.getTopTags](artist.getTopTags.md)** - Creates artist-tag relationships
- **[artist.getTopTracks](artist.getTopTracks.md)** - Creates artist-track relationships
- **[artist.getTopAlbums](artist.getTopAlbums.md)** - Creates artist-album relationships
- **[artist.getSimilar](artist.getSimilar.md)** - Creates artist similarity relationships
- **[artist.search](artist.search.md)** - Artist discovery

### Implementation Templates (Not Yet Implemented)
- **[album.getInfo](album.getInfo.md)** - Album details with tracks and tags
- **[album.getTopTags](album.getTopTags.md)** - Creates album-tag relationships
- **[track.getInfo](track.getInfo.md)** - Track details with album and tags
- **[track.getTopTags](track.getTopTags.md)** - Creates track-tag relationships
- **[tag.getInfo](tag.getInfo.md)** - Tag details with wiki information
- **[tag.getTopAlbums](tag.getTopAlbums.md)** - Creates tag-album relationships

## Key Implementation Patterns

### Entity Identification
- **Artists**: Identified by `name` (unique key)
- **Albums**: Identified by `url` (unique key)  
- **Tracks**: Identified by `url` (unique key)
- **Tags**: Identified by `name` (unique key)

### Attribute Types
- **SCD2 Attributes**: Track changes over time (most attributes)
- **Snapshot Attributes**: Captured per API call (rank attributes)

### Relationship Creation
- **ArtistTag**: Artist-tag associations with usage counts/ranks
- **TrackTag**: Track-tag associations with usage counts/ranks
- **AlbumTag**: Album-tag associations with usage counts/ranks
- **ArtistArtist**: Artist similarity relationships with match scores
- **ArtistAlbum**: Artist-album associations
- **AlbumTrack**: Album-track associations with track positions

## Implementation Workflow

1. **Review the implementation guide** for the specific API method
2. **Create DTO classes** based on the response schema
3. **Implement the response processor** following the processing algorithm
4. **Create unit tests** for the processor
5. **Update the API call generator** if needed
6. **Test with real API responses**
7. **Update documentation** if implementation differs from the guide

## Next Steps for New Implementations

1. Review and refine existing implementation guides
2. Design utility classes and shared components
3. Plan implementation order based on dependencies
4. Begin implementing new relationship entities and processors

Priority order based on data gaps:
1. **album.getInfo** - For album-track relationships
2. **track.getInfo** - For track-tag relationships  
3. **tag.getInfo** - For tag descriptions
