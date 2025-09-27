# track.getTopTags - Processing Documentation

## Method Information
- **API Method:** `track.getTopTags`
- **Scope Entity:** `LastfmTrack` (the track for which we're getting top tags)
- **Response Schema:** `TrackTopTagsDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/track.getTopTags.json` (to be added)

## Extractable Entities
1. **LastfmTag** - Tags associated with the track

## Extractable Attributes
### For LastfmTag:
- `name` - Tag name (from DTO field `name`)
- `url` - Tag URL (from DTO field `url`)

## Extractable Relationships
1. **TrackTag** - Relationship between track and tag

## Relationship Attributes
### For TrackTag:
- `usageCount` - How many times this tag was applied to this track (from DTO field `count`)
- No `rank` available in this response

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TrackTopTagsDtoRoot`
- Extract list of `TrackTopTagsTagDto` from `rootObject.tags`

### Step 2: Validate Scope Entity
- Retrieve scope track entity from API call
- Verify track exists in database
- Throw exception if track not found

### Step 3: Filter Tags
- Apply business logic filters (e.g., minimum usage count threshold)
- Remove tags that don't meet criteria

### Step 4: Process Tag Entities
- For each `TrackTopTagsTagDto`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`

### Step 5: Extract Tag Attributes
- For each tag entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)

### Step 6: Create Track-Tag Relationships
- For each track-tag pair:
  - Check if `TrackTag` relationship already exists
  - Create new `TrackTag` relationship entity if not exists
  - Set `trackId` and `tagId`
  - Set `usageCount` from DTO field `count`
  - Set `apiCall` reference

### Step 7: Save Data
- Save all new/updated tag entities
- Save all attribute history records
- Save all track-tag relationship entities

### Step 8: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the track from API call parameters
- Tags are identified by name as unique key
- Usage count represents how many times this tag was applied to this track
- This method complements tag.getTopTracks by providing the reverse relationship
- TrackTag relationships may already exist from tag.getTopTracks calls
- Similar pattern to artist.getTopTags and album.getTopTags but for tracks
