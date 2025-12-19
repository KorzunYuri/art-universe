# Whitelist Filter Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [WhitelistFilterProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/WhitelistFilterProcessor.java)

## Overview

Filters tracks to include only those belonging to specified whitelisted categories, while applying weighted compensation to achieve desired category distribution. Unlike a simple filter, this step uses sophisticated balancing to adjust selection probabilities based on target weights.

## Logic

The step performs a complex compensation-based balancing:

1. Creates a temporary whitelist table with category IDs and their weights
2. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
3. Maps each track to its whitelisted categories
4. Calculates current vs target weight distribution
5. Applies compensation factors to rebalance categories
6. Keeps only tracks in whitelisted categories
7. Creates indexes on `track_id` and `primary_artist_id`

### Compensation Algorithm

1. **Category Mapping:**
   - For each track, find all matching whitelisted categories
   - Track gets assigned to all matching categories with their weights

2. **Current State Calculation:**
   - Sum current chances for each category: `current_chance_sum`
   - Calculate total across all categories: `total_current_chance`

3. **Compensation Factors:**
   - For each category: `compensation = (target_weight * total_current_chance) / current_chance_sum`
   - If a category is under-represented, its compensation factor is >1
   - If over-represented, compensation factor is <1

4. **Track Chance Adjustment:**
   - Each track's new chance = `old_chance * max(compensation_factors_for_its_categories)`
   - If track belongs to multiple categories, uses the highest compensation
   - Filters out tracks with zero or negative adjusted chance

## Configuration

**Config Type:** `WhitelistFilterStepConfig`

### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `categories` | `List<CategoryWeight>` | Yes | List of categories to whitelist with their target weights |

### CategoryWeight

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Category ID to include |
| `weight` | `Double` | Target relative weight for this category |

### Example Configuration

```json
{
  "categories": [
    {"id": 10, "weight": 0.6},
    {"id": 20, "weight": 0.4}
  ]
}
```

This configuration:
- Includes only tracks from categories 10 or 20
- Adjusts chances to achieve 60/40 distribution between the categories

## Compensation Example

Suppose we want 60/40 distribution (Rock: 0.6, Jazz: 0.4):

**Before compensation:**
- Rock: 100 tracks with total chance = 100
- Jazz: 50 tracks with total chance = 50
- Total chance = 150

**Current distribution:** Rock = 100/150 = 67%, Jazz = 50/150 = 33%

**Target distribution:** Rock = 60%, Jazz = 40%

**Compensation factors:**
- Rock: (0.6 * 150) / 100 = 0.9 (reduce Rock's chances)
- Jazz: (0.4 * 150) / 50 = 1.2 (increase Jazz's chances)

**After compensation:**
- Rock tracks: multiplied by 0.9
- Jazz tracks: multiplied by 1.2
- New effective distribution: closer to 60/40

## Use Case

This step should be used when you want to:
- Include only specific categories of content
- Control the relative distribution of those categories
- Rebalance category representation based on current pool state
- Achieve target category mix through probability adjustment

## Comparison with Other Category Steps

| Step | Inclusion Logic | Distribution Control | Strictness |
|------|----------------|---------------------|------------|
| **Whitelist Filter** | Only included categories | Weighted compensation | Soft (probability-based) |
| **Blacklist Filter** | Exclude specific categories | None | Hard (filter) |
| **Final Categories Balancer** | Optional categories + default | Quota-based | Hard (quota enforcement) |

## Database Function

**Function:** `p_quiz_gen_tracks_step_categories_whitelist_filter(input_table, output_table, whitelist_table)`

The function uses CTEs (Common Table Expressions) for the multi-stage calculation:
1. `category_mapping` - Map tracks to categories
2. `category_stats` - Calculate current state per category
3. `total_stats` - Calculate total current chance
4. `compensation_factors` - Calculate adjustment factors
5. Final SELECT - Apply adjustments and aggregate

### Important Detail

Tracks belonging to multiple whitelisted categories:
- Appear once in output
- Use `MAX(chance * compensation)` across all their categories
- Benefit from the most favorable compensation factor

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` - Adjusted selection probability with compensation applied

**Effect:**
- Removes tracks not in any whitelisted category
- Adjusts chances of remaining tracks to achieve target distribution
- Uses GROUP BY to deduplicate tracks in multiple categories

## Stats

The processor provides enhanced statistics via `WhitelistFilterStats`:

- **Basic stats:** Input/output counts for tracks and artists
- **Output by category:** Track and artist counts per whitelisted category

## Advanced Notes

1. **Weights are relative:**
   - Only the ratios matter, not absolute values
   - `{0.6, 0.4}` equals `{3, 2}` equals `{60, 40}`

2. **Probabilistic nature:**
   - Compensation adjusts probabilities, not guarantees
   - Final selection steps may not perfectly match targets
   - Use Final Categories Balancer for strict quotas

3. **Multiple categories per track:**
   - Track gets the best compensation factor available
   - This can lead to over-representation of multi-category tracks

4. **Idempotent execution:**
   - Whitelist table is dropped and recreated each run
   - Safe to re-run the step

## Example Scenario

Configuration: Rock (weight: 0.7), Jazz (weight: 0.3)

Input pool:
- 1000 Rock tracks (average chance: 0.8)
- 200 Jazz tracks (average chance: 0.5)

Current state:
- Rock total chance: 800
- Jazz total chance: 100
- Total: 900

Target state:
- Rock should be: 70% of 900 = 630
- Jazz should be: 30% of 900 = 270

Compensation:
- Rock: 630/800 = 0.7875 (reduce by ~21%)
- Jazz: 270/100 = 2.7 (increase by 170%)

Result:
- Rock tracks: multiplied by 0.79
- Jazz tracks: multiplied by 2.7
- Distribution moves toward 70/30 target

## Best Practices

1. **Use before final selection:** Apply whitelist early, use final steps for exact counts
2. **Combine with penalties:** Apply recency/diversity penalties after whitelist
3. **Monitor actual distribution:** Check stats to see if compensation is working
4. **Adjust weights iteratively:** May need to tune weights to achieve desired results
5. **Consider pool composition:** Compensation works best when categories have reasonable representation
