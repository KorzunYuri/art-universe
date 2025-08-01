# artist.getTopTags - Processing Documentation

## Method Information
- **API Method:** `artist.getTopTags`
- **Scope Entity:** `LastfmArtist` (the artist for which we're getting top tags)
- **Response Schema:** `ArtistTopTagsDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/artist.getTopTags.json`

## Extractable Entities
1. **LastfmTag** - Tags associated with the artist

## Extractable Attributes
### For LastfmTag:
- `name` - Tag name (from DTO field `name`)
- `url` - Tag URL (from DTO field `url`)

## Extractable Relationships
1. **ArtistTag** - Relationship between artist and tag

## Relationship Attributes
### For ArtistTag:
- `usageCount` - How many times this tag was applied to this artist (from DTO field `count`)
- No `rank` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `ArtistTopTagsDtoRoot`
- Extract list of `ArtistTopTagsTagDto` from `rootObject.tags`

### Step 2: Validate Scope Entity
- Retrieve scope artist entity from API call
- Verify artist exists in database
- Throw exception if artist not found

### Step 3: Filter Tags
- Apply business logic filters (e.g., minimum usage count threshold)
- Remove tags that don't meet criteria

### Step 4: Process Tag Entities
- For each `ArtistTopTagsTagDto`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`

### Step 5: Extract Tag Attributes
- For each tag entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 6: Create Artist-Tag Relationships
- For each artist-tag pair:
  - Check if `ArtistTag` relationship already exists
  - Create new `ArtistTag` relationship entity if not exists
  - Set `artistId` and `tagId`
  - Set `usageCount` from DTO field `count`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated tag entities
- Save all attribute history records
- Save all artist-tag relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the artist from API call parameters
- Tags are identified by name as unique key
- Usage count represents how many times this tag was applied to this artist
- This method complements tag.getTopArtists by providing the reverse relationship
- ArtistTag relationships may already exist from tag.getTopArtists calls
