# tag.getTopArtists - Processing Documentation

## Method Information
- **API Method:** `tag.getTopArtists`
- **Scope Entity:** `LastfmTag` (the tag for which we're getting top artists)
- **Response Schema:** `TagTopArtistsDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/tag.getTopArtists.json`

## Extractable Entities
1. **LastfmArtist** - Artists associated with the tag

## Extractable Attributes
### For LastfmArtist:
- `name` - Artist name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Artist URL (from DTO field `url`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)
- `playCount` - Total play count (from DTO field `playcount`)
- `isStreamable` - Whether artist is streamable (from DTO field `streamable`) **[IGNORE - NOT NEEDED]**

## Extractable Relationships
1. **ArtistTag** - Relationship between artist and tag

## Relationship Attributes
### For ArtistTag:
- `rank` - Artist's rank within this tag (from DTO `@attr.rank`) **[IGNORE - NOT NEEDED]**
- No `usageCount` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TagTopArtistsDtoRoot`
- Extract list of `TagTopArtistsArtistDto` from `rootObject.artists`
- Extract tag information from `rootObject.requestMetadata.tagName`

### Step 2: Validate Scope Entity
- Retrieve scope tag entity from API call
- Verify tag exists in database
- Throw exception if tag not found

### Step 3: Filter Artists
- Apply business logic filters (e.g., minimum listeners threshold)
- Remove artists that don't meet criteria

### Step 4: Process Artist Entities
- For each `TagTopArtistsArtistDto`:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `listeners` → `listenersCount`
    - `playcount` → `playCount`
    - ~~`streamable` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**

### Step 5: Extract Artist Attributes
- For each artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)
  - `LastfmAttribute.PLAY_COUNT` (from `playcount`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable`)~~ **[IGNORE - NOT NEEDED]**

### Step 6: Create Artist-Tag Relationships
- For each artist-tag pair:
  - Create `ArtistTag` relationship entity
  - Set `artistId` and `tagId`
  - Set `rank` from DTO `@attr.rank`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated artist entities
- Save all attribute history records
- Save all artist-tag relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the tag from API call parameters
- Artists are identified by name as unique key
- Rank attribute comes from `@attr.rank` in the response
- This method creates the primary ArtistTag relationships
