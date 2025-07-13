# artist.getTopTracks - Processing Documentation

## Method Information
- **API Method:** `artist.getTopTracks`
- **Scope Entity:** `LastfmArtist` (the artist for which we're getting top tracks)
- **Response Schema:** `ArtistTopTracksDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.getTopTracks.json`

## Extractable Entities
1. **LastfmTrack** - Tracks by the artist

## Extractable Attributes
### For LastfmTrack:
- `name` - Track name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Track URL (from DTO field `url`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)
- `playCount` - Total play count (from DTO field `playcount`)
- `isStreamable` - Whether track is streamable (from DTO field `streamable`) **[IGNORE - NOT NEEDED]**

## Extractable Relationships
1. **ArtistTrack** - Implicit relationship between artist and track (via track.artist field)

## Relationship Attributes
### For ArtistTrack:
- No specific relationship attributes in this response
- Relationship is established by setting track.artist reference

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistTopTracksDtoRoot`
- Extract list of `ArtistTopTracksTrackDto` from `rootObject.tracks`

### Step 2: Validate Scope Entity
- Retrieve scope artist entity from API call
- Verify artist exists in database
- Throw exception if artist not found

### Step 3: Filter Tracks
- Apply business logic filters (e.g., minimum listeners threshold)
- Remove tracks that don't meet criteria

### Step 4: Process Track Entities
- For each `ArtistTopTracksTrackDto`:
  - Check if track already exists by URL (unique key)
  - Create new `LastfmTrack` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `listeners` → `listenersCount`
    - `playcount` → `playCount`
    - ~~`streamable` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**
  - Set artist reference to scope artist

### Step 5: Extract Track Attributes
- For each track entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable`)~~ **[IGNORE - NOT NEEDED]**

### Step 6: Create Artist-Track Relationships
- Artist-track relationship is implicit via track.artist field
- No separate relationship entity needed
- All tracks get their artist field set to scope artist

### Step 7: Save Data
- Save all new/updated track entities (with artist references)
- Save all attribute history records
- No separate relationship entities to save

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the artist from API call parameters
- Tracks are identified by URL as unique key
- Artist-track relationship is established via track.artist foreign key
- This method discovers tracks for a specific artist
- All tracks will have the same artist (scope entity)
