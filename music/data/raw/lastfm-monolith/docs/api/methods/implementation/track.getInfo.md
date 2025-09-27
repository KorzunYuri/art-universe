# track.getInfo - Processing Documentation

## Method Information
- **API Method:** `track.getInfo`
- **Scope Entity:** `LastfmTrack` (the track for which we're getting detailed info)
- **Response Schema:** `TrackGetInfoDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/track.getInfo.json`
- **Implementation Status:** ✅ Implemented

## Extractable Entities
1. **LastfmTrack** - The track whose info is being retrieved
2. **LastfmArtist** - Track artist (from track.artist field)
3. **LastfmAlbum** - Track album (from track.album field, if present)
4. **LastfmTag** - Tags associated with the track

## Extractable Attributes
### For LastfmTrack:
- `name` - Track name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Track URL (from DTO field `url`)
- `duration` - Track duration in seconds (from DTO field `duration`)
- `playCount` - Total play count (from DTO field `playcount`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)

### For LastfmArtist (from track.artist):
- `name` - Artist name (from DTO field `artist.name`)
- `mbid` - Artist MusicBrainz ID (from DTO field `artist.mbid`)
- `url` - Artist URL (from DTO field `artist.url`)

### For LastfmAlbum (from track.album, if present):
- `name` - Album name (from DTO field `album.title`)
- `mbid` - Album MusicBrainz ID (from DTO field `album.mbid`)
- `url` - Album URL (from DTO field `album.url`)

### For LastfmTag (from track tags):
- `name` - Tag name (from DTO field `toptags.tag[].name`)
- `url` - Tag URL (from DTO field `toptags.tag[].url`)

## Extractable Relationships
1. **ArtistTrack** - Relationship between artist and track
2. **AlbumTrack** - Relationship between album and track (if album present)
3. **TrackTag** - Relationship between track and tags

## Relationship Attributes
### For AlbumTrack:
- `position` - Track position in album (from DTO field `album.position`, if available)

## Implementation Details

### API Call Generation
The `LastfmTrackGetInfoApiCallGenerator` implements the following selection logic:
1. **Priority 1**: Tracks with missing statistics (null playCount or listenersCount)
2. **Priority 2**: Tracks from popular artists (ordered by artist's listenersCount)

The generator ensures no duplicate API calls are created for the same track by checking for existing pending calls.

### Response Processing
The `LastfmTrackGetInfoResponseProcessor` implements the following workflow:

1. Process artist from track.artist field
2. Process track with artist reference
3. Create artist-track relationship
4. Process album if present and create album-track relationship
5. Process tags and create track-tag relationships
6. Extract and save all entity attributes

### Entity Factories
- `LastfmTrackGetInfoTrackFactory` - Creates/updates track entities with artist reference
- `LastfmTrackGetInfoArtistFactory` - Creates/updates artist entities
- `LastfmTrackGetInfoAlbumFactory` - Creates/updates album entities
- `LastfmTrackGetInfoTagFactory` - Creates/updates tag entities

## Testing
The implementation includes comprehensive tests:
- `LastfmTrackGetInfoResponseProcessorTest` - Tests response processing logic
- `LastfmTrackGetInfoApiCallGeneratorTest` - Tests API call generation logic
- `TrackGetInfoDtoMappingTest` - Tests DTO mapping from JSON

## Configuration
- **Refresh Interval**: `lastfm.client.methods.track.getInfo.dueDurationDays` (default: 28)
- **Batch Size**: Configurable via `batchSize` property

## Implementation Notes
- Scope entity is the track from API call parameters
- This method provides detailed track information including artist, album, and tags
- Tracks are identified by URL as unique key
- Artists are identified by name as unique key
- Albums are identified by URL as unique key (if present)
- Tags are identified by name as unique key
- Artist-track relationship is established via track.artist foreign key
- Album information may not always be present in the response
- The processor handles both new entity creation and updating existing entities
- Attribute history is maintained for all tracked attributes
