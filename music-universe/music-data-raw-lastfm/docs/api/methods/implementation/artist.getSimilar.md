# artist.getSimilar - Processing Documentation

## Method Information
- **API Method:** `artist.getSimilar`
- **Scope Entity:** `LastfmArtist` (the artist for which we're getting similar artists)
- **Response Schema:** `ArtistGetSimilarDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.getSimilar.json`

## Extractable Entities
1. **LastfmArtist** - Similar artists

## Extractable Attributes
### For LastfmArtist:
- `name` - Artist name (from DTO field `name`)
- `mbid` - MusicBrainz ID (from DTO field `mbid`)
- `url` - Artist URL (from DTO field `url`)
- `isStreamable` - Whether artist is streamable (from DTO field `streamable`) **[IGNORE - NOT NEEDED]**

## Extractable Relationships
1. **ArtistArtist** - Similarity relationship between source and target artists

## Relationship Attributes
### For ArtistArtist:
- `matchScore` - Similarity coefficient (from DTO field `match`, converted from percentage to decimal)
- `relationType` - Set to `LastfmEntityRelationType.SIMILARITY`

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistGetSimilarDtoRoot`
- Extract list of `ArtistGetSimilarArtistDto` from `rootObject.artists`

### Step 2: Validate Scope Entity
- Retrieve scope artist entity from API call
- Verify artist exists in database
- Throw exception if artist not found

### Step 3: Filter Similar Artists
- Apply business logic filters (e.g., minimum match score threshold)
- Remove artists that don't meet criteria

### Step 4: Process Similar Artist Entities
- For each `ArtistGetSimilarArtistDto`:
  - Check if artist already exists by name (unique key)
  - Create new `LastfmArtist` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `mbid` → `mbid`
    - `url` → `url`
    - ~~`streamable` → `isStreamable`~~ **[IGNORE - NOT NEEDED]**

### Step 5: Extract Artist Attributes
- For each similar artist entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.MBID` (from `mbid`)
  - ~~`LastfmAttribute.IS_STREAMABLE` (from `streamable`)~~ **[IGNORE - NOT NEEDED]**

### Step 6: Create Artist-Artist Relationships
- For each source-target artist pair:
  - Check if `ArtistArtist` relationship already exists
  - Create new `ArtistArtist` relationship entity if not exists
  - Set `sourceArtistId` to scope artist ID
  - Set `targetArtistId` to similar artist ID
  - Set `matchScore` from DTO field `match` (convert percentage to decimal: match/100)
  - Set `relationType` to `LastfmEntityRelationType.SIMILARITY`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated similar artist entities
- Save all attribute history records
- Save all artist-artist relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the source artist from API call parameters
- Similar artists are identified by name as unique key
- Match score is converted from percentage (0-100) to decimal (0.00-1.00)
- Relationship is directional: source artist → similar artist
- This method creates ArtistArtist similarity relationships
- All relationships will have the same source artist (scope entity)
