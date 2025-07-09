# artist.getInfo - Processing Documentation

## Method Information
- **API Method:** `artist.getInfo`
- **Scope Entity:** None (can be called for any artist by name/mbid)
- **Response Schema:** `ArtistGetInfoDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.getInfo.json`

## Extractable Entities
1. **LastfmArtist** - The artist whose info is being retrieved
2. **LastfmArtist** - Similar artists (from `similar.artist[]`)
3. **LastfmTag** - Artist's tags (from `tags.tag[]`)

## Extractable Attributes
### For Main LastfmArtist:
- `name` - Artist name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Artist URL (from DTO field `url`)
- `listenersCount` - Number of listeners (from DTO field `stats.listeners`)
- `playCount` - Total play count (from DTO field `stats.playcount`)
- `isStreamable` - Whether artist is streamable (from DTO field `streamable`) **[IGNORE - NOT NEEDED]**
- `isOnTour` - Whether artist is on tour (from DTO field `ontour`) **[IGNORE - NOT NEEDED]**

### For Similar LastfmArtist:
- `name` - Artist name (from DTO field `similar.artist[].name`)
- `url` - Artist URL (from DTO field `similar.artist[].url`)

### For LastfmTag:
- `name` - Tag name (from DTO field `tags.tag[].name`)
- `url` - Tag URL (from DTO field `tags.tag[].url`)

## Extractable Relationships
1. **LastfmArtistsRelation** - Similarity relationships between main artist and similar artists
   - `sourceArtist` → Similar artist
   - `targetArtist` → Main artist
   - `relationType` → `SIMILARITY`
   
2. **LastfmArtistTag** - Tag relationships between main artist and tags
   - `artist` → Main artist
   - `tag` → Tag

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistGetInfoDtoRoot`
- Extract `ArtistGetInfoArtistDto` from `rootObject.artist`

### Step 2: Process Main Artist Entity
- Check if artist already exists by name (unique key)
- Create new `LastfmArtist` entity if not exists, otherwise update existing
- Map DTO fields to entity fields:
  - `name` → `name`
  - `mbid` → `mbid`
  - `url` → `url`
  - `stats.listeners` → `listenersCount`
  - `stats.playcount` → `playCount`
  - ~~`streamable` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**
  - ~~`ontour` → `isOnTour`~~ **[IGNORE - NOT NEEDED]**

### Step 3: Process Similar Artists
- Extract similar artists from `similar.artist[]`
- For each similar artist:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists, otherwise update existing
  - Map DTO fields: `name`, `url`

### Step 4: Process Tags
- Extract tags from `tags.tag[]`
- For each tag:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists, otherwise update existing
  - Map DTO fields: `name`, `url`

### Step 5: Extract Attributes
- For the main artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `stats.listeners`)
  - `LastfmAttribute.PLAY_COUNT` (from `stats.playcount`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable`)~~ **[IGNORE - NOT NEEDED]**
  - ~~`LastfmAttribute.IS_ON_TOUR` (from `ontour`)~~ **[IGNORE - NOT NEEDED]**

- For similar artists, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

- For tags, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 6: Create Relationships
- Create `LastfmArtistsRelation` entities for each similar artist:
  - `sourceArtist` → Similar artist
  - `targetArtist` → Main artist
  - `relationType` → `SIMILARITY`
  - `apiCall` → Source API call

- Create `LastfmArtistTag` entities for each tag:
  - `artist` → Main artist
  - `tag` → Tag
  - `apiCall` → Source API call

### Step 7: Save Data
- Save the main artist entity (new or updated)
- Save all similar artist entities (new or updated)
- Save all tag entities (new or updated)
- Save all attribute history records
- Save all artist-artist similarity relationships
- Save all artist-tag relationships

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- No scope entity required - can be called for any artist
- Main artist is identified by name as unique key
- Similar artists are also identified by name as unique key
- Tags are identified by name as unique key
- This method creates a comprehensive artist profile with related entities
- All attributes are SCD2 type (track changes over time)
- Similarity relationships are directional (similar → main)
- Can be used to discover new artists, tags, and their relationships
