# tag.getTopTracks - Processing Documentation

## Method Information
- **API Method:** `tag.getTopTracks`
- **Scope Entity:** `LastfmTag` (the tag for which we're getting top tracks)
- **Response Schema:** `TagTopTracksDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/tag.getTopTracks.json`

## Extractable Entities
1. **LastfmTrack** - Tracks associated with the tag
2. **LastfmArtist** - Artists of the tracks (nested in track data)

## Extractable Attributes
### For LastfmTrack:
- `name` - Track name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Track URL (from DTO field `url`)
- `duration` - Track duration in seconds (from DTO field `duration`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)
- `playCount` - Total play count (from DTO field `playcount`)
- `isStreamable` - Whether track is streamable (from DTO field `streamable.fulltrack`) **[IGNORE - NOT NEEDED]**

### For LastfmArtist (nested):
- `name` - Artist name (from DTO field `artist.name`)
- `mbid` - Artist MusicBrainz ID (from DTO field `artist.mbid`)
- `url` - Artist URL (from DTO field `artist.url`)

## Extractable Relationships
1. **TrackTag** - Relationship between track and tag
2. **ArtistTrack** - Relationship between artist and track (implicit via track.artist field)

## Relationship Attributes
### For TrackTag:
- `rank` - Track's rank within this tag (from DTO `@attr.rank`) **[IGNORE - NOT NEEDED]**
- No `usageCount` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TagTopTracksDtoRoot`
- Extract list of `TagTopTracksTrackDto` from `rootObject.tracks`
- Extract tag information from `rootObject.requestMetadata.tagName`

### Step 2: Validate Scope Entity
- Retrieve scope tag entity from API call
- Verify tag exists in database
- Throw exception if tag not found

### Step 3: Filter Tracks
- Apply business logic filters (e.g., minimum listeners threshold)
- Remove tracks that don't meet criteria

### Step 4: Process Artist Entities (from nested data)
- For each track's artist data:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists
  - Map DTO fields to entity fields:
    - `artist.name` → `name`
    - `artist.mbid` → `mbid`
    - `artist.url` → `url`

### Step 5: Process Track Entities
- For each `TagTopTracksTrackDto`:
  - Check if track already exists by URL (unique key)
  - Create new `LastfmTrack` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `duration` → `duration`
    - `listeners` → `listenersCount`
    - `playcount` → `playCount`
    - ~~`streamable.fulltrack` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**
  - Set artist reference from processed artists

### Step 6: Extract Attributes
- For each artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `artist.url`)
  - `LastfmAttribute.MBID` (from `artist.mbid`)

- For each track entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.DURATION` (from `duration`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable.fulltrack`)~~ **[IGNORE - NOT NEEDED]**

### Step 7: Create Track-Tag Relationships
- For each track-tag pair:
  - Create `TrackTag` relationship entity
  - Set `trackId` and `tagId`
  - Set `rank` from DTO `@attr.rank`
  - Set `apiCall` reference

### Step 8: Save Data
- Save all new/updated artist entities
- Save all new/updated track entities
- Save all attribute history records
- Save all track-tag relationship entities

### Step 9: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the tag from API call parameters
- Tracks are identified by URL as unique key
- Artists are identified by name as unique key
- Rank attribute comes from `@attr.rank` in the response
- This method creates both TrackTag relationships and discovers new artists
- Artist-track relationship is implicit via track.artist field
