# album.getInfo - Processing Documentation

## Method Information
- **API Method:** `album.getInfo`
- **Scope Entity:** `LastfmAlbum` (the album for which we're getting detailed info)
- **Response Schema:** `AlbumGetInfoDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/album.getInfo.json` (to be added)

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

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `AlbumGetInfoDtoRoot`
- Extract `AlbumGetInfoAlbumDto` from `rootObject.album`

### Step 2: Validate Scope Entity
- Retrieve scope album entity from API call
- Verify album exists in database
- Throw exception if album not found

### Step 3: Process Album Entity
- Update existing album entity with detailed information
- Map DTO fields to entity fields:
  - `name` → `name`
  - `mbid` → `mbid`
  - `url` → `url`
  - `playcount` → `playCount`
  - `listeners` → `listenersCount`
  - `releasedate` → `publishTs`
  - ~~`wiki.content` → `description`~~ **[IGNORE - NOT NEEDED]**

### Step 4: Process Artist Entity
- Extract artist information from `artist` field
- Check if artist already exists by name (unique key)
- Create new `LastfmArtist` entity if not exists
- Update artist attributes if needed

### Step 5: Process Track Entities
- For each track in `tracks.track[]`:
  - Check if track already exists by URL (unique key)
  - Create new `LastfmTrack` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`
    - `duration` → `duration`
  - Set artist reference to album's artist

### Step 6: Process Tag Entities
- For each tag in `tags.tag[]`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`

### Step 7: Extract Attributes
- For album entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)

- For track entities, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.DURATION` (from `duration`)

- For tag entities, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 8: Create Relationships
- Create `ArtistAlbum` relationship between artist and album
- For each track, create `AlbumTrack` relationship:
  - Set `albumId` and `trackId`
  - Set `position` from `@attr.rank`
- For each tag, create `AlbumTag` relationship:
  - Set `albumId` and `tagId`
  - Set `usageCount` from `count`

### Step 9: Save Data
- Save updated album entity
- Save all new/updated track entities
- Save all new/updated tag entities
- Save all attribute history records
- Save all relationship entities

### Step 10: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the album from API call parameters
- This method provides detailed album information including tracklist
- Albums are identified by URL as unique key
- Tracks are identified by URL as unique key
- Tags are identified by name as unique key
- Creates multiple relationship types: ArtistAlbum, AlbumTrack, AlbumTag
- Track positions are important for album tracklist ordering
