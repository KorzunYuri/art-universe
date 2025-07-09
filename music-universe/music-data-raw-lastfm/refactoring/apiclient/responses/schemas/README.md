# LastFM API Response Schemas

This directory contains schema definitions for all LastFM API responses used in the music-data-raw-lastfm module. 
Each schema file describes the structure, data extraction points, and processing logic for implementing `LastfmApiResponseProcessor` subclasses.

## Schema Files Overview

### Album Methods
- **album.getInfo.yml** - Detailed album information with tracks, tags, and images
- **album.getTopTags.yml** - Top tags associated with an album

### Artist Methods  
- **artist.getInfo.yml** - Detailed artist information with stats, similar artists, tags, and bio
- **artist.getSimilar.yml** - Artists similar to a given artist with match scores
- **artist.getTopAlbums.yml** - Top albums for an artist with play counts
- **artist.getTopTags.yml** - Top tags associated with an artist
- **artist.getTopTracks.yml** - Top tracks for an artist with play counts and listeners
- **artist.search.yml** - Search results for artists by name

### Tag Methods
- **tag.getInfo.yml** - Detailed tag information with usage statistics and wiki
- **tag.getTopAlbums.yml** - Top albums tagged with a specific tag
- **tag.getTopArtists.yml** - Top artists tagged with a specific tag  
- **tag.getTopTags.yml** - Global list of top tags (entry point for tag discovery)
- **tag.getTopTracks.yml** - Top tracks tagged with a specific tag

### Track Methods
- **track.getInfo.yml** - Detailed track information with artist, album, tags, and wiki
- **track.getTopTags.yml** - Top tags associated with a track

## Schema Structure

Each schema file contains:

### response_structure
- `root_key`: Main JSON key containing the response data
- `main_entity`: Primary entity type and fields to extract
- `nested_entities`: Related entities (tags, images, etc.) with extraction paths
- `attributes`: Metadata and pagination information
- structures/attributes marked with `processing: ignore` should not be processed

## Common Patterns

### Data Type Conversions
- Most numeric fields come as strings from LastFM API
- Convert to appropriate numeric types during processing
- Handle empty/null values gracefully - don't replace existing values with nulls

### Entity Relationships
- Create bidirectional relationships where appropriate (artist similarities)
- Ensure parent entities exist before creating child relationships

## Implementation Guidelines

When implementing `LastfmApiResponseProcessor` subclasses:

1. **Parse JSON** using the root_key to access main data
2. if applicable, get 'scope' entity from Api call to which Api response belongs
3. if applicable, extract main entity with all required fields
4. **Process nested entities** following the defined paths
5. **Create relationships** between entities as specified
6**Store attributes** for SCD2 tracking where applicable
