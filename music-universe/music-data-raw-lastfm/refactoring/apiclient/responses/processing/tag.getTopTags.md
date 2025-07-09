# tag.getTopTags - Processing Documentation

## Method Information
- **API Method:** `tag.getTopTags`
- **Scope Entity:** None (entry point method)
- **Response Schema:** `TagTopTagsDtoRoot`
- **Response Example:** `src/test/resources/apiclient/responses/tag.getTopTags.json`

## Extractable Entities
1. **LastfmTag** - Primary entities from the response

## Extractable Attributes
### For LastfmTag:
- `name` - Tag name (from DTO field `name`)
- `url` - Tag URL (from DTO field `url`) 
- `usageCount` - Number of times tag was used (from DTO field `count`)
- `usageUsersCount` - Number of users who used the tag (from DTO field `reach`)

## Extractable Relationships
None - this is an entry point method that discovers tags.

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TagTopTagsDtoRoot`
- Extract list of `TagTopTagsTagDto` from `rootObject.tags`

### Step 2: Filter Tags
- Apply business logic filters (e.g., minimum usage count threshold)
- Remove tags that don't meet criteria

### Step 3: Process Tag Entities
- For each `TagTopTagsTagDto`:
  - Check if tag already exists by name (unique key)
  - Create new `LastfmTag` entity if not exists
  - Map DTO fields to entity fields:
    - `name` → `name`
    - `url` → `url`
    - `count` → `usageCount`
    - `reach` → `usageUsersCount`

### Step 4: Extract Attributes
- For each tag entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.RELATIONS_COUNT` (from `count`)
  - `LastfmAttribute.REACH` (from `reach`)

### Step 5: Save Data
- Save all new/updated tag entities
- Save all attribute history records
- No entity relations to save for this method

### Step 6: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- This method serves as entry point for tag discovery
- No scope entity required
- Tags are identified by name as unique key
- All attributes are SCD2 type (track changes over time)
