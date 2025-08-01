# artist.search - Processing Documentation

## Method Information
- **API Method:** `artist.search`
- **Scope Entity:** None (search method for artist discovery)
- **Response Schema:** `ArtistSearchDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.search.json`

## Extractable Entities
1. **LastfmArtist** - Artists found in search results

## Extractable Attributes
### For LastfmArtist:
- `name` - Artist name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Artist URL (from DTO field `url`)
- `listenersCount` - Number of listeners (from DTO field `listeners`)
- `isStreamable` - Whether artist is streamable (from DTO field `streamable`) **[IGNORE - NOT NEEDED]**

## Extractable Relationships
None - this is a discovery method that finds artists.

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistSearchDtoRoot`
- Extract list of `ArtistSearchArtistDto` from `rootObject.artistMatches.artists`
- Extract search metadata from `rootObject.requestMetadata`

### Step 2: Filter Artists
- Apply business logic filters (e.g., minimum listeners threshold)
- Remove artists that don't meet criteria
- Consider search relevance/match quality

### Step 3: Process Artist Entities
- For each `ArtistSearchArtistDto`:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - `listeners` → `listenersCount`
    - ~~`streamable` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**

### Step 4: Extract Artist Attributes
- For each artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - `LastfmAttribute.LISTENERS_COUNT` (from `listeners`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable`)~~ **[IGNORE - NOT NEEDED]**

### Step 5: Update Search Request Status
- Mark corresponding `LastfmArtistSearchRequest` as processed
- Update search request entity in database

### Step 6: Save Data
- Save all new/updated artist entities
- Save all attribute history records
- No relationships to save

### Step 7: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- No scope entity required - this is a discovery method
- Artists are identified by name as unique key
- This method is used to discover new artists based on search terms
- Search requests are tracked via `LastfmArtistSearchRequest` entities
- Results may include artists already known to the system
- Consider implementing relevance scoring for search results
