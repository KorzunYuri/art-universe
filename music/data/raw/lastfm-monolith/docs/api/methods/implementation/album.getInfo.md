# album.getInfo - Processing Documentation

## Method Information
- **API Method:** `album.getInfo`
- **Scope Entity:** `LastfmAlbum` (the album for which we're getting detailed info)
- **Response Schema:** `AlbumGetInfoDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/album.getInfo.json`
- **Implementation Status:** ✅ Implemented

## Extractable Entities
1. **LastfmAlbum** - The album whose info is being retrieved
2. **LastfmTrack** - Tracks on the album (from tracklist)
3. **LastfmArtist** - Album artist (from album.artist field)
4. **LastfmTag** - Tags associated with the album

## Extractable Attributes
### For LastfmAlbum:
- `name` - Album name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Album URL (from DTO field `url`)
- `playCount` - Total play count (from DTO field `playcount`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)
- `publishTs` - Release date (from DTO field `releasedate`
- `description` - Album description/wiki (from DTO field `wiki.content`) **[IGNORE - NOT NEEDED]**

### For LastfmTrack (from tracklist):
- `name` - Track name (from DTO field `tracks.track[].name`)
- `url` - Track URL (from DTO field `tracks.track[].url`)
- `duration` - Track duration (from DTO field `tracks.track[].duration`)

### For LastfmTag (from album tags):
- `name` - Tag name (from DTO field `tags.tag[].name`)
- `url` - Tag URL (from DTO field `tags.tag[].url`)

## Extractable Relationships
1. **ArtistAlbum** - Relationship between artist and album
2. **AlbumTrack** - Relationship between album and tracks
3. **AlbumTag** - Relationship between album and tags

## Relationship Attributes
### For AlbumTrack:
- `position` - Track position in album (from DTO field `tracks.track[].@attr.rank`)

### For AlbumTag:
- `usageCount` - Tag usage count (from DTO field `tags.tag[].count`)

## Implementation Details

### API Call Generation
The `LastfmAlbumGetInfoApiCallGenerator` implements the standard `findAllUnprocessed` method with a due duration of 28 days. It uses MBID if available, otherwise it uses album name and artist name for API calls.

### Response Processing
The `LastfmAlbumGetInfoResponseProcessor` implements the following workflow:

1. Process artists from tracks and album artist field
2. Process album (without explicitly setting artist to preserve existing reference)
3. Process tracks with artist references
4. Create artist-track relationships
5. Create album-track relationships with positions
6. Process tags and create album-tag relationships

### Entity Factories
- `LastfmAlbumGetInfoAlbumFactory` - Updates album entities
- `LastfmAlbumGetInfoTrackFactory` - Creates/updates track entities with artist references
- `LastfmAlbumGetInfoTagFactory` - Creates/updates tag entities

## Testing
The implementation includes comprehensive tests:
- `AlbumGetInfoDtoMappingTest` - Tests DTO mapping from JSON
- `LastfmAlbumGetInfoApiCallGeneratorTest` - Tests API call generation logic
- `LastfmAlbumGetInfoResponseProcessorTest` - Tests response processing logic

## Configuration
- **Refresh Interval**: `lastfm.client.methods.album.getInfo.dueDurationDays` (default: 28)
- **Batch Size**: Configurable via `batchSize` property

## Implementation Notes
- Scope entity is the album from API call parameters
- This method provides detailed album information including tracklist and tags
- Albums are identified by URL as unique key
- Artists are identified by name as unique key
- Tracks are identified by URL as unique key
- Tags are identified by name as unique key
- The processor handles both new entity creation and updating existing entities
- Attribute history is maintained for all tracked attributes
- Album-track relationships include position information for proper track ordering
