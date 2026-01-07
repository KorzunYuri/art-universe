# Track Recency Penalty Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [TrackRecencyPenaltyProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/TrackRecencyPenaltyProcessor.java)

## Overview

Reduces the selection probability of specific tracks that have appeared in recently approved quiz generations. Similar to Artist Recency Penalty, but applies at the individual track level, preventing the exact same songs from appearing too frequently.

## Logic

The step performs the following operations:

1. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
2. Looks up when each specific track last appeared in an approved generation
3. Calculates months since last appearance
4. Applies a time-based penalty multiplier to the track's chance
5. **Filters out** tracks with zero probability (recently used tracks)
6. Creates indexes on `track_id` and `primary_artist_id`

### Penalty Formula

The penalty multiplier is calculated based on months since last appearance:

- **Never appeared or ≥12 months ago:** multiplier = `1.0` (no penalty)
- **≤1 month ago:** multiplier = `0.0` (complete exclusion - tracks are filtered out)
- **Between 1-12 months:** Linear interpolation
  - Formula: `(months_ago - 1) / 11.0`
  - Example: 6 months → `(6 - 1) / 11 = 0.45` (55% penalty)

## Configuration

This step has no configuration parameters. The penalty curve is hard-coded in the database function.

## Penalty Examples

| Months Since Last Use | Multiplier | Effective Penalty | Result |
|----------------------|------------|-------------------|---------|
| 0-1 | 0.00 | 100% | **EXCLUDED** |
| 2 | 0.09 | 91% | Very unlikely |
| 3 | 0.18 | 82% | Unlikely |
| 6 | 0.45 | 55% | Moderate penalty |
| 9 | 0.73 | 27% | Light penalty |
| 12+ | 1.00 | 0% | No penalty |

## Comparison with Artist Recency Penalty

| Feature | Artist Recency | Track Recency |
|---------|---------------|---------------|
| **Scope** | All tracks by artist | Individual track |
| **Minimum penalty** | 0.2 (20% chance remains) | 0.0 (complete exclusion) |
| **Use case** | Artist rotation | Track rotation |
| **Strictness** | Lenient | Strict |

## Use Case

This step should be used when you want to:
- Prevent the exact same songs from repeating in consecutive quizzes
- Ensure variety at the track level (not just artist level)
- Give tracks a mandatory cooldown period after use
- Create a strict rotation policy for quiz content

## Typical Pipeline Position

Usually placed:
- **After** artist recency penalty (if both are used)
- **Before** final selection steps
- Can be used **alone** or **with** artist recency penalty

Combined usage example:
```
Artist Recency Penalty → Reduces artist repetition (lenient)
Track Recency Penalty → Prevents exact track repetition (strict)
```

## Database Function

**Function:** `p_quiz_gen_tracks_step_track_recency_penalty(input_table, output_table)`

The function queries the `mu_quiz.generation_track` and `mu_quiz.generation` tables to find the most recent approved appearance of each specific track.

### Important Detail

The WHERE clause filters out tracks with zero probability:
```sql
WHERE ... > 0
```
This means tracks used within the last month are completely removed from the pool, not just penalized.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability (defaults to 1.0)

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` - Adjusted selection probability with recency penalty applied

**Effect:**
- Completely removes tracks used in the last month
- Reduces chances for tracks used 1-12 months ago
- No penalty for tracks unused for 12+ months

## Example Scenario

Imagine a track "Song X" was in an approved quiz:

- **Day after quiz:** Track X is completely excluded (0% chance)
- **30 days later:** Still excluded (0% chance)
- **31-60 days later:** Very low chance (9% of original)
- **6 months later:** Moderate chance (45% of original)
- **12 months later:** Full chance (100% of original)

## Best Practices

1. **Use with Artist Recency:** Combine both for comprehensive rotation
2. **Consider quiz frequency:** If quizzes are monthly, the 1-month exclusion is appropriate
3. **Monitor pool size:** Aggressive penalties can reduce available tracks significantly
4. **Balance with other steps:** Ensure enough variety remains after all filters

## Notes

- More aggressive than Artist Recency Penalty (minimum 0.0 vs 0.2)
- Creates a "hard cooldown" of 1 month for tracks
- Tracks can appear again after 1 month, but with reduced probability
- After 12 months, tracks are back to full rotation eligibility
