# Approved Filter Step

> **See also:** [Pipeline Steps Index](../../../../../docs/kb/features/quiz/quiz-pack-generation-pipeline.md) - Complete list of all available pipeline steps

**Processor:** [ApprovedFilterProcessor](../../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/ApprovedFilterProcessor.java)

## Overview

Filters the track pool to include only tracks that have been bound/approved in the quiz database. This step ensures that only tracks that exist in the `mu_quiz.track` table are included in the generation.

## Logic

The step performs the following operations:

1. Reads all tracks from the input table
2. Joins with the `mu_quiz.track` table using `master_id` (the track ID from the master database)
3. Keeps only tracks that exist in the quiz database (i.e., tracks that have been explicitly approved/bound)
4. Creates indexes on `track_id` and `primary_artist_id` for performance

## Configuration

This step has no configuration parameters.

## Use Case

This step is typically used early in the pipeline to ensure that:
- Only tracks that have been reviewed and approved are considered for quiz generation
- The quiz doesn't accidentally include tracks that haven't been vetted
- Track metadata exists in the quiz database for proper quiz operation

## Database Function

**Function:** `p_quiz_gen_tracks_step_approved_filter(input_table, output_table)`

The function creates a new table containing only tracks that exist in the `mu_quiz.track` table.

## Input/Output

**Input Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier

**Output Table Columns:**
- `track_id` - Track identifier
- `primary_artist_id` - Artist identifier

**Note:** This step filters out unapproved tracks but doesn't modify the remaining records.
