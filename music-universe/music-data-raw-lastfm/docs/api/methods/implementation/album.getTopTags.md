# album.getTopTags - Processing Documentation

## Method Information
- **API Method:** `album.getTopTags`
- **Scope Entity:** `LastfmAlbum` (the album for which we're getting top tags)
- **Response Schema:** `AlbumTopTagsDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/album.getTopTags.json` (to be added)

## Extractable Entities
1. **LastfmTag** - Tags associated with the album

## Extractable Attributes
### For LastfmTag:
- `name` - Tag name (from DTO field `name`)
- `url` - Tag URL (from DTO field `url`)

## Extractable Relationships
1. **AlbumTag** - Relationship between album and tag

## Relationship Attributes
### For AlbumTag:
- `usageCount` - How many times this tag was applied to this album (from DTO field `count`)
- No `rank` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `AlbumTopTagsDtoRoot`
- Extract list of `AlbumTopTagsTagDto` from `rootObject.tags`

### Step 2: Validate Scope Entity
- Retrieve scope album entity from API call
- Verify album exists in database
- Throw exception if album not found

### Step 3: Filter Tags
- Apply business logic filters (e.g., minimum usage count threshold)
- Remove tags that don't meet criteria

### Step 4: Process Tag Entities
- For each `AlbumTopTagsTagDto`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`

### Step 5: Extract Tag Attributes
- For each tag entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 6: Create Album-Tag Relationships
- For each album-tag pair:
  - Check if `AlbumTag` relationship already exists
  - Create new `AlbumTag` relationship entity if not exists
  - Set `albumId` and `tagId`
  - Set `usageCount` from DTO field `count`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated tag entities
- Save all attribute history records
- Save all album-tag relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the album from API call parameters
- Tags are identified by name as unique key
- Usage count represents how many times this tag was applied to this album
- This method complements tag.getTopAlbums by providing the reverse relationship
- AlbumTag relationships may already exist from tag.getTopAlbums calls (when implemented)
- Similar pattern to artist.getTopTags but for albums
