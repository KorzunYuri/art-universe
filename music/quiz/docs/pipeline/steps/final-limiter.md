# Final Limiter Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [FinalLimiterProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/FinalLimiterProcessor.java)

## Overview

Performs the final selection of tracks from the pool, limiting the output to a specific target count while ensuring artist diversity. This is typically the last step in the pipeline that produces the final quiz track list.

## Logic

The step performs the following operations:

1. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
2. Ranks all tracks by their weighted random selection (`RANDOM() * chance`)
3. Deduplicates tracks by artist (keeps the highest-ranked track per artist)
4. Selects the top N tracks based on the target count
5. Creates indexes on `track_id` and `primary_artist_id`

### Selection Algorithm

1. **Weighted Random Ranking:**
   - Each track gets a score: `RANDOM() * chance`
   - Tracks with higher `chance` are more likely to rank higher
   - Randomness ensures variety across different runs

2. **Artist Deduplication:**
   - For each artist, only the highest-ranked track is kept
   - This ensures maximum artist diversity in the final selection

3. **Top N Selection:**
   - Selects the top `targetCount` tracks after deduplication
   - Final output is limited to exactly this many tracks (or fewer if not enough unique artists)

## Configuration

**Config Type:** `FinalLimiterStepConfig`

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `targetCount` | `Integer` | Yes | Exact number of tracks to select for the quiz |

### Example Configuration

```json
{
  "targetCount": 50
}
```

This configuration selects exactly 50 tracks from the input pool (or fewer if there aren't enough unique artists).

## Use Case

This step should be used when you want to:
- Finalize the track selection to a specific quiz size
- Ensure each artist appears at most once
- Produce the final, ready-to-use track list for quiz generation
- Apply the final weighted random selection

## Database Function

**Function:** `p_quiz_gen_tracks_step_final_selection(input_table, output_table, target_count)`

The function uses a CTE-based approach with ROW_NUMBER for ranking and deduplication.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability (defaults to 1.0)

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier

**Note:** The `chance` column is dropped in the output; selection is final.

## Behavior Details

### Artist Deduplication

- **Before deduplication:** Multiple tracks from the same artist may be in the pool
- **After deduplication:** Each artist appears exactly once
- **Selection criteria:** Highest-ranked track per artist (based on weighted random score)

### Edge Cases

1. **Fewer tracks than target:**
   - If input has fewer unique artists than `targetCount`
   - Output will contain all available tracks (less than target)

2. **Zero or low chances:**
   - Tracks with very low chances can still be selected (random factor)
   - Tracks with `chance = 0` are filtered out before ranking

## Typical Pipeline Position

The Final Limiter is usually placed:
- **After all filtering steps** (approved, blacklist, whitelist)
- **After all penalty steps** (recency, diversity)
- **As the last step** in the pipeline

Alternative final step:
- Use **Final Categories Balancer** if you need category-based distribution
- Use **Final Limiter** if you only need simple count-based selection

## Example

Input pool: 500 tracks from 300 artists with varying `chance` values
Configuration: `targetCount = 50`

Process:
1. Rank all 500 tracks by `RANDOM() * chance`
2. Keep highest-ranked track per artist (reduces to 300 tracks)
3. Select top 50 ranked tracks
4. Output: 50 tracks from 50 different artists

Each run produces different results due to randomness, but tracks with higher `chance` values appear more frequently across multiple runs.
