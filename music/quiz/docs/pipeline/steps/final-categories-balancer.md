# Final Categories Balancer Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [FinalCategoriesBalancerProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/FinalCategoriesBalancerProcessor.java)

## Overview

Balances the final track selection across multiple categories using weighted quotas. This sophisticated step ensures that the quiz contains a specified distribution of tracks across different categories while also ensuring artist diversity.

## Logic

The step performs a multi-stage selection process:

### Stage 1: Special Category Assignment
1. For each track, randomly selects one of its matching weighted categories (using weights)
2. Calculates quota for each category: `CEIL(targetCount * (1 - defaultQuota) * categoryWeight)`
3. Stores track-to-category assignments

### Stage 2: Default Quota Pool
1. Adds remaining tracks (not assigned to special categories) to a default pool
2. Default quota: `CEIL(targetCount * defaultQuota)`

### Stage 3: Quota-Based Selection
1. Randomly selects tracks within each category quota (probability-weighted)
2. Each category gets up to its calculated quota of tracks

### Stage 4: Artist Deduplication
1. After quota selection, removes duplicate artists
2. Keeps only one track per artist (highest chance wins)

## Configuration

**Config Type:** `FinalCategoriesBalancerConfig`

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `targetCount` | `Integer` | Yes | Total number of tracks desired in output |
| `defaultQuota` | `Double` | Yes | Fraction (0.0-1.0) of tracks for uncategorized/other categories |
| `categories` | `List<CategoryWeight>` | Yes | List of categories with their weights |

### CategoryWeight

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Category ID |
| `weight` | `Double` | Relative weight for this category |

### Example Configuration

```json
{
  "targetCount": 100,
  "defaultQuota": 0.3,
  "categories": [
    {"id": 10, "weight": 0.4},
    {"id": 20, "weight": 0.3}
  ]
}
```

This configuration:
- Targets 100 total tracks
- Allocates 30% (30 tracks) to default/other categories
- Allocates 70% (70 tracks) to special categories:
  - Category 10: 40% of 70 = 28 tracks
  - Category 20: 30% of 70 = 21 tracks

## Use Case

This step should be used when you want to:
- Ensure specific distribution of content across categories
- Balance quiz difficulty or themes
- Maintain controlled variety in quiz composition
- Guarantee minimum representation for important categories

## Database Function

**Function:** `p_quiz_gen_tracks_step_final_categories_balancer(input_table, output_table, quota_table, target_count, default_quota)`

The function creates multiple intermediate tables to perform the complex quota allocation and artist deduplication.

### Intermediate Tables

- `{prefix}_quota_i1_special_tracks` - Tracks assigned to weighted categories
- `{prefix}_quota_i2_all_tracks` - Special + default pool combined
- `{prefix}_quota_i3_selected` - After quota-based selection

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier

**Note:** The `chance` column is dropped in the output; selection has been finalized.

## Stats

The processor provides enhanced statistics via `FinalCategoriesBalancerStats`:

- **Basic stats:** Input/output counts for tracks and artists
- **Output by category:** Track and artist counts per configured category
- **Default quota:** Counts for tracks not in any configured category

## Important Notes

1. **Weights must sum sensibly:** While not required to sum to 1.0, weights are relative to each other
2. **Artist deduplication is final:** Once applied, artists appear at most once
3. **Probabilistic assignment:** Each track's category assignment involves randomness
4. **Intermediate tables:** Created during execution; useful for debugging

## Example Scenario

Configuration:
- `targetCount`: 50
- `defaultQuota`: 0.2 (20%)
- Categories: Rock (weight: 0.5), Jazz (weight: 0.3)

Results:
- Default pool: 10 tracks (20% of 50)
- Rock quota: CEIL(50 * 0.8 * 0.5) = 20 tracks
- Jazz quota: CEIL(50 * 0.8 * 0.3) = 12 tracks

Total before deduplication: up to 42 special tracks + 10 default = 52 tracks
After artist deduplication: approximately 50 unique artists
