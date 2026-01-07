# Artist Recency Penalty Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [ArtistRecencyPenaltyProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/ArtistRecencyPenaltyProcessor.java)

## Overview

Reduces the selection probability of artists that have appeared in recently approved quiz generations. This helps avoid repetition and keeps quiz content fresh by giving recently-used artists a cooldown period.

## Logic

The step performs the following operations:

1. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
2. Looks up when each artist last appeared in an approved generation
3. Calculates months since last appearance
4. Applies a time-based penalty multiplier to the track's chance
5. Creates indexes on `track_id` and `primary_artist_id`

### Penalty Formula

The penalty multiplier is calculated based on months since last appearance:

- **Never appeared or ≥12 months ago:** multiplier = `1.0` (no penalty)
- **≤1 month ago:** multiplier = `0.2` (80% penalty)
- **Between 1-12 months:** Linear interpolation
  - Formula: `0.2 + (months_ago - 1) * 0.8 / 11.0`
  - Example: 6 months → `0.2 + 5 * 0.073 = 0.56` (44% penalty)

## Configuration

This step has no configuration parameters. The penalty curve is hard-coded in the database function.

## Penalty Examples

| Months Since Last Use | Multiplier | Effective Penalty |
|----------------------|------------|-------------------|
| 0-1 | 0.20 | 80% |
| 2 | 0.27 | 73% |
| 3 | 0.35 | 65% |
| 6 | 0.56 | 44% |
| 9 | 0.78 | 22% |
| 12+ | 1.00 | 0% |

## Use Case

This step should be used when you want to:
- Avoid showing the same artists too frequently
- Create a natural rotation of artists across quiz generations
- Balance freshness with user familiarity

## Database Function

**Function:** `p_quiz_gen_tracks_step_artist_recency_penalty(input_table, output_table)`

The function queries the `mu_quiz.generation_track` and `mu_quiz.generation` tables to find the most recent approved appearance of each artist.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability (defaults to 1.0)

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` - Adjusted selection probability with recency penalty applied

**Effect:** Reduces chances for artists who appeared recently, with the penalty diminishing over 12 months.
