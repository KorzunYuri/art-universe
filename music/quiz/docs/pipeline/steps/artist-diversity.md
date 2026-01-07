# Artist Diversity Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [ArtistDiversityProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/ArtistDiversityProcessor.java)

## Overview

Promotes artist diversity by reducing the selection probability of tracks from artists with many tracks in the pool. This prevents artists with large catalogs from dominating the quiz.

## Logic

The step performs the following operations:

1. Ensures the input table has a `chance` column (defaults to 1.0 if not present)
2. Counts how many tracks each artist has in the current pool
3. Adjusts each track's chance by dividing by the artist's track count
4. Formula: `new_chance = current_chance * (1.0 / artist_track_count)`
5. Filters out tracks with zero or negative chance
6. Creates indexes on `track_id` and `primary_artist_id`

## Configuration

This step has no configuration parameters.

## Example

If an artist has 10 tracks in the pool:
- Each track's chance is multiplied by `1/10 = 0.1`
- This makes each individual track less likely to be selected
- However, the artist's combined chance across all 10 tracks remains comparable to artists with fewer tracks

## Use Case

This step should be used when you want to:
- Ensure variety in quiz content across different artists
- Prevent artists with extensive catalogs from overwhelming the selection
- Balance representation between prolific and less prolific artists

## Database Function

**Function:** `p_quiz_gen_tracks_step_artist_diversity(input_table, output_table)`

The function calculates the diversity penalty based on the number of tracks per artist in the current pool.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` (optional) - Selection probability (defaults to 1.0)

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier
- `chance` - Adjusted selection probability

**Effect:** Reduces individual track chances for artists with many tracks while maintaining overall artist representation.
