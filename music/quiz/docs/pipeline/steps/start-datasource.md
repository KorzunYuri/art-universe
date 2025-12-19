# Start Datasource Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [StartDatasourceProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/StartDatasourceProcessor.java)

## Overview

Initializes the track generation pipeline by creating the initial dataset from a master data source. This is always the first step in any quiz generation pipeline, providing the base pool of tracks for all subsequent filtering and processing steps.

## Logic

The step performs the following operations:

1. Creates a view (not a table) based on the configured datasource
2. Selects the essential columns: `track_id`, `name`, `primary_artist_id`
3. The view references the master data source directly
4. No data transformation or filtering is applied at this stage

### Key Characteristics

- **Creates a VIEW, not a TABLE:** This makes it lightweight and always current
- **No input table required:** This is the starting point of the pipeline
- **Simple projection:** Just selects the basic track information needed downstream

## Configuration

**Config Type:** `StartDatasourceStepConfig`

### Parameters

This step has no user-configurable parameters. It uses a default datasource.

**Default Datasource:** `mu_quiz.ds_mu_v_track`

### Example Configuration

```json
{}
```

The configuration is always empty; the datasource is hard-coded.

## Use Case

This step must be used as:
- The **first step** in every quiz generation pipeline
- The **entry point** that defines the initial track pool
- The **foundation** for all subsequent filtering and selection steps

## Implementation Details

Unlike other processors that call database functions, this processor directly creates a SQL view:

```sql
CREATE OR REPLACE VIEW {output_view_name} AS
SELECT
    ds.track_id,
    ds.name,
    ds.primary_artist_id
FROM mu_quiz.ds_mu_v_track as ds
```

## Input/Output

**Input:**
- None (this is the first step)

**Output Table/View:**
- `track_id` - Track identifier from master database
- `name` - Track name
- `primary_artist_id` - Primary artist identifier from master database

## Data Source

The default datasource `mu_quiz.ds_mu_v_track` typically contains:
- All tracks from the master music database
- That have been bound to the quiz database
- With their associated metadata

## Pipeline Position

- **Must be:** The first step (position 0 or 1)
- **Cannot be:** Placed after other steps
- **Validates:** That no input table is provided

## Example Pipeline Start

```
1. START_DATASOURCE → Creates initial pool of all available tracks
2. APPROVED_FILTER → Filters to only approved tracks
3. BLACKLIST_FILTER → Removes unwanted categories
4. ... (more steps)
```

## Notes

1. **View vs Table:**
   - Uses `CREATE OR REPLACE VIEW` for efficiency
   - Always reflects current state of master data
   - No storage overhead

2. **No validation on datasource name:**
   - The datasource name is validated as a SQL object name
   - But there's no check that the datasource exists until execution

3. **Minimal columns:**
   - Only essential columns are selected
   - Additional track metadata can be joined later if needed
   - Keeps intermediate tables lightweight

## Common Issues

- **Error if used as non-first step:** The processor overrides input table validation
- **Missing datasource:** Will fail at execution if `mu_quiz.ds_mu_v_track` doesn't exist
- **Empty results:** If datasource view is empty, all downstream steps will have no data
