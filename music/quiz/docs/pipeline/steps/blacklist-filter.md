# Blacklist Filter Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [BlacklistFilterProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/BlacklistFilterProcessor.java)

## Overview

Excludes tracks that belong to specified blacklisted categories. This step filters out tracks based on their category associations, allowing you to remove unwanted content types from quiz generation.

## Logic

The step performs the following operations:

1. Creates a temporary blacklist table with the configured category IDs
2. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
3. Joins tracks with the `mu_quiz.mu_v_track_category` view to find their categories
4. Excludes any track that belongs to a blacklisted category
5. Creates indexes on `track_id` and `primary_artist_id`

## Configuration

**Config Type:** `BlacklistFilterStepConfig`

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categoryIds` | `List<Long>` | Yes | List of category IDs to exclude from the pool |

### Example Configuration

```json
{
  "categoryIds": [101, 102, 205]
}
```

This configuration would exclude all tracks that belong to categories 101, 102, or 205.

## Use Case

This step should be used when you want to:
- Exclude specific genres or styles from quiz generation
- Filter out explicit content or specific themes
- Remove categories that don't fit the quiz's target audience
- Exclude categories with poor quality or incomplete data

## Database Function

**Function:** `p_quiz_gen_tracks_step_categories_blacklist_filter(input_table, output_table, blacklist_table)`

The function creates an auxiliary blacklist table and filters tracks by checking their category associations against this blacklist.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` - Selection probability (unchanged for remaining tracks)

**Effect:** Removes tracks entirely if they belong to blacklisted categories. Tracks not in any blacklisted category pass through unchanged.

## Stats

The processor provides enhanced statistics via `BlacklistFilterStats`:

- **Basic stats:** Input/output counts for tracks and artists
- **Filtered by category:** Map showing how many records were filtered for each blacklisted category

## Notes

- If a track belongs to multiple categories and any one is blacklisted, the track is excluded
- The blacklist table is created fresh for each step execution (idempotent)
- Empty `categoryIds` list results in no filtering (all tracks pass through)
