# tag.getInfo - Processing Documentation

## Method Information
- **API Method:** `tag.getInfo`
- **Scope Entity:** `LastfmTag` (the tag for which we're getting detailed info)
- **Response Schema:** `TagGetInfoDtoRoot` (to be created)
- **Response Example:** `src/test/resources/apiclient/responses/tag.getInfo.json` (to be added)

## Extractable Entities
1. **LastfmTag** - The tag whose info is being retrieved

## Extractable Attributes
### For LastfmTag:
- `name` - Tag name (from DTO field `name`)
- `url` - Tag URL (from DTO field `url`)
- `usageCount` - Total usage count (from DTO field `total`)
- `usageUsersCount` - Number of users who used the tag (from DTO field `reach`)

## Extractable Relationships
None - this method only updates tag information.

## Processing Algorithm

### Step 1: Parse Response
- Parse JSON response into `TagGetInfoDtoRoot`
- Extract `TagGetInfoTagDto` from `rootObject.tag`

### Step 2: Validate Scope Entity
- Retrieve scope tag entity from API call
- Verify tag exists in database
- Throw exception if tag not found

### Step 3: Process Tag Entity
- Update existing tag entity with detailed information
- Map DTO fields to entity fields:
  - `name` → `name`
  - `url` → `url`
  - `total` → `usageCount`
  - `reach` → `usageUsersCount`

### Step 4: Extract Tag Attributes
- For the tag entity, extract SCD2 attributes:
  - `LastfmAttribute.URL` (from `url`)
  - `LastfmAttribute.RELATIONS_COUNT` (from `total`)
  - `LastfmAttribute.REACH` (from `reach`)

### Step 5: Save Data
- Save the updated tag entity
- Save all attribute history records
- No relationships to save

### Step 6: Update API Response Status
- Mark API response as `COMPLETED`
- Log processing statistics

## Implementation Notes
- Scope entity is the tag from API call parameters
- This method provides detailed tag information and statistics
- Tags are identified by name as unique key
- All attributes are SCD2 type (track changes over time)
- This method primarily updates tag statistics and metadata
- Can be used to get comprehensive information about a specific tag
