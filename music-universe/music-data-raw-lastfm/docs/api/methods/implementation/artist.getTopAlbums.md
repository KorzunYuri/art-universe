# artist.getTopAlbums - Processing Documentation

## Method Information
- **API Method:** `artist.getTopAlbums`
- **Scope Entity:** `LastfmArtist` (the artist for which we're getting top albums)
- **Response Schema:** `ArtistTopAlbumsDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.getTopAlbums.json`

## Extractable Entities
1. **LastfmAlbum** - Albums by the artist

## Extractable Attributes
### For LastfmAlbum:
- `name` - Album name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Album URL (from DTO field `url`)
- `playCount` - Total play count (from DTO field `playcount`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)

## Extractable Relationships
1. **ArtistAlbum** - Relationship between artist and album

## Relationship Attributes
### For ArtistAlbum:
- No specific relationship attributes in this response
- Simple relationship without additional data

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistTopAlbumsDtoRoot`
- Extract list of `ArtistTopAlbumsAlbumDto` from `rootObject.albums`

### Step 2: Validate Scope Entity
- Retrieve scope artist entity from API call
- Verify artist exists in database
- Throw exception if artist not found

### Step 3: Filter Albums
- Apply business logic filters (e.g., minimum play count threshold)
- Remove albums that don't meet criteria

### Step 4: Process Album Entities
- For each `ArtistTopAlbumsAlbumDto`:
  - Check if album already exists by URL (unique key)
  - Create new `LastfmAlbum` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `playcount` → `playCount`
    - `listeners` → `listenersCount`

### Step 5: Extract Album Attributes
- For each album entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)

### Step 6: Create Artist-Album Relationships
- For each artist-album pair:
  - Check if `ArtistAlbum` relationship already exists
  - Create new `ArtistAlbum` relationship entity if not exists
  - Set `artistId` and `albumId`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated album entities
- Save all attribute history records
- Save all artist-album relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the artist from API call parameters
- Albums are identified by URL as unique key
- ArtistAlbum is a simple relationship without additional attributes
- This method discovers albums for a specific artist
- All albums will be related to the same artist (scope entity)
