# track.getInfo - Processing Documentation

## Method Information
- **API Method:** `track.getInfo`
- **Scope Entity:** `LastfmTrack` (the track for which we're getting detailed info)
- **Response Schema:** `TrackGetInfoDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/track.getInfo.json` (to be added)

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
- `isStreamable` - Whether track is streamable (from DTO field `streamable.fulltrack`) **[IGNORE - NOT NEEDED]**

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
1. **ArtistTrack** - Relationship between artist and track (implicit via track.artist field)
2. **AlbumTrack** - Relationship between album and track (if album present)
3. **TrackTag** - Relationship between track and tags

## Relationship Attributes
### For AlbumTrack:
- `position` - Track position in album (from DTO field `album.@attr.position`, if available)

### For TrackTag:
- `usageCount` - Tag usage count (from DTO field `toptags.tag[].count`)

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TrackGetInfoDtoRoot`
- Extract `TrackGetInfoTrackDto` from `rootObject.track`

### Step 2: Validate Scope Entity
- Retrieve scope track entity from API call
- Verify track exists in database
- Throw exception if track not found

### Step 3: Process Artist Entity
- Extract artist information from `artist` field
- Check if artist already exists by name (unique key)
- Create new `LastfmArtist` entity if not exists
- Update artist attributes if needed

### Step 4: Process Album Entity (if present)
- If `album` field is present:
  - Check if album already exists by URL (unique key)
  - Create new `LastfmAlbum` entity if not exists
  - Map DTO fields to entity fields:
    - `album.title` → `name`
    - `album.mbid` → `mbid`
    - `album.url` → `url`

### Step 5: Process Track Entity
- Update existing track entity with detailed information
- Map DTO fields to entity fields:
  - `name` → `name`
  - `mbid` → `mbid`
  - `url` → `url`
  - `duration` → `duration`
  - `playcount` → `playCount`
  - `listeners` → `listenersCount`
  - ~~`streamable.fulltrack` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**
- Set artist reference to processed artist

### Step 6: Process Tag Entities
- For each tag in `toptags.tag[]`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`

### Step 7: Extract Attributes
- For track entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.DURATION` (from `duration`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable.fulltrack`)~~ **[IGNORE - NOT NEEDED]**

- For artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `artist.url`)
  - `LastfmAttribute.MBID` (from `artist.mbid`)

- For album entity (if present), extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `album.url`)
  - `LastfmAttribute.MBID` (from `album.mbid`)

- For tag entities, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 8: Create Relationships
- Artist-track relationship is implicit via track.artist field
- If album is present, create `AlbumTrack` relationship:
  - Set `albumId` and `trackId`
  - Set `position` from `album.@attr.position` (if available)
- For each tag, create `TrackTag` relationship:
  - Set `trackId` and `tagId`
  - Set `usageCount` from `count`

### Step 9: Save Data
- Save updated track entity (with artist reference)
- Save all new/updated artist entities
- Save all new/updated album entities (if any)
- Save all new/updated tag entities
- Save all attribute history records
- Save all relationship entities

### Step 10: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the track from API call parameters
- This method provides detailed track information including artist, album, and tags
- Tracks are identified by URL as unique key
- Artists are identified by name as unique key
- Albums are identified by URL as unique key (if present)
- Tags are identified by name as unique key
- Artist-track relationship is established via track.artist foreign key
- Album information may not always be present in the response
