# tag.getTopAlbums - Processing Documentation

## Method Information
- **API Method:** `tag.getTopAlbums`
- **Scope Entity:** `LastfmTag` (the tag for which we're getting top albums)
- **Response Schema:** `TagTopAlbumsDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/tag.getTopAlbums.json` (to be added)

## Extractable Entities
1. **LastfmAlbum** - Albums associated with the tag
2. **LastfmArtist** - Artists of the albums (nested in album data)

## Extractable Attributes
### For LastfmAlbum:
- `name` - Album name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Album URL (from DTO field `url`)
- `playCount` - Total play count (from DTO field `playcount`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)

### For LastfmArtist (nested):
- `name` - Artist name (from DTO field `artist.name`)
- `mbid` - Artist MusicBrainz ID (from DTO field `artist.mbid`)
- `url` - Artist URL (from DTO field `artist.url`)

## Extractable Relationships
1. **AlbumTag** - Relationship between album and tag
2. **ArtistAlbum** - Relationship between artist and album (implicit via album.artist field)

## Relationship Attributes
### For AlbumTag:
- `rank` - Album's rank within this tag (from DTO `@attr.rank`) **[IGNORE - NOT NEEDED]**
- No `usageCount` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TagTopAlbumsDtoRoot`
- Extract list of `TagTopAlbumsAlbumDto` from `rootObject.albums`
- Extract tag information from `rootObject.requestMetadata.tagName`

### Step 2: Validate Scope Entity
- Retrieve scope tag entity from API call
- Verify tag exists in database
- Throw exception if tag not found

### Step 3: Filter Albums
- Apply business logic filters (e.g., minimum play count threshold)
- Remove albums that don't meet criteria

### Step 4: Process Artist Entities (from nested data)
- For each album's artist data:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists
  - Map DTO fields to entity fields:
    - `artist.name` → `name`
    - `artist.mbid` → `mbid`
    - `artist.url` → `url`

### Step 5: Process Album Entities
- For each `TagTopAlbumsAlbumDto`:
  - Check if album already exists by URL (unique key)
  - Create new `LastfmAlbum` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `playcount` → `playCount`
    - `listeners` → `listenersCount`

### Step 6: Extract Attributes
- For each artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `artist.url`)
  - `LastfmAttribute.MBID` (from `artist.mbid`)

- For each album entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)

### Step 7: Create Album-Tag Relationships
- For each album-tag pair:
  - Create `AlbumTag` relationship entity
  - Set `albumId` and `tagId`
  - Set `rank` from DTO `@attr.rank`
  - Set `apiCall` reference

### Step 8: Create Artist-Album Relationships
- For each artist-album pair:
  - Check if `ArtistAlbum` relationship already exists
  - Create new `ArtistAlbum` relationship entity if not exists
  - Set `artistId` and `albumId`
  - Set `apiCall` reference

### Step 9: Save Data
- Save all new/updated artist entities
- Save all new/updated album entities
- Save all attribute history records
- Save all album-tag relationship entities
- Save all artist-album relationship entities

### Step 10: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the tag from API call parameters
- Albums are identified by URL as unique key
- Artists are identified by name as unique key
- Rank attribute comes from `@attr.rank` in the response
- This method creates both AlbumTag relationships and discovers new artists/albums
- Artist-album relationship is implicit via album.artist field
- Complements album.getTopTags by providing the reverse relationship
