# Pipeline Step Processors

This directory contains documentation for all available pipeline step processors used in quiz track generation.

## Overview

Quiz generation uses a multi-step pipeline where each step transforms the track pool through filtering, weighting, or selection operations. Steps are executed sequentially, with each step's output becoming the next step's input.

## Available Steps

### Initialization

| Step | Type | Configuration | Description |
|------|------|--------------|-------------|
| [Start Datasource](./start-datasource.md) | `START_DATASOURCE` | None | Initializes the pipeline with tracks from the master datasource |

### Filtering Steps

| Step | Type | Configuration | Description |
|------|------|--------------|-------------|
| [Approved Filter](./approved-filter.md) | `APPROVED_FILTER` | None | Keeps only tracks that have been bound to the quiz database |
| [Blacklist Filter](./blacklist-filter.md) | `BLACKLIST_FILTER` | Category IDs | Excludes tracks belonging to specified categories |
| [Whitelist Filter](./whitelist-filter.md) | `WHITELIST_FILTER` | Category weights | Includes only specified categories with weighted compensation |

### Penalty/Weighting Steps

| Step | Type | Configuration | Description |
|------|------|--------------|-------------|
| [Artist Diversity](./artist-diversity.md) | `ARTIST_DIVERSITY` | None | Reduces probability for artists with many tracks |
| [Artist Recency Penalty](./artist-recency-penalty.md) | `ARTIST_RECENCY_PENALTY` | None | Penalizes artists used in recent quizzes (lenient) |
| [Track Recency Penalty](./track-recency-penalty.md) | `TRACK_RECENCY_PENALTY` | None | Penalizes tracks used in recent quizzes (strict) |

### Final Selection Steps

| Step | Type | Configuration | Description |
|------|------|--------------|-------------|
| [Final Categories Balancer](./final-categories-balancer.md) | `FINAL_CATEGORIES_BALANCER` | Target count, quotas, category weights | Selects tracks with category-based quotas and artist deduplication |
| [Final Limiter](./final-limiter.md) | `FINAL_LIMITER` | Target count | Simple selection of N tracks with artist deduplication |

## Step Categories

### 1. Initialization Steps
- **Start Datasource:** Must be the first step in every pipeline

### 2. Hard Filters
- **Approved Filter:** Binary yes/no decision
- **Blacklist Filter:** Binary yes/no decision based on categories
- Tracks are either included or excluded entirely

### 3. Soft Filters (Probability Adjustments)
- **Whitelist Filter:** Includes categories but adjusts probabilities
- **Artist Diversity:** Adjusts probabilities based on artist catalog size
- **Recency Penalties:** Adjust probabilities based on usage history
- Tracks remain in pool but with modified selection chances

### 4. Final Selection
- **Categories Balancer:** Complex quota-based selection with category distribution
- **Final Limiter:** Simple top-N selection
- Choose one or the other, not both

## Common Pipeline Patterns

### Basic Pipeline
```
1. START_DATASOURCE
2. APPROVED_FILTER
3. FINAL_LIMITER (targetCount: 50)
```

### Category-Focused Pipeline
```
1. START_DATASOURCE
2. APPROVED_FILTER
3. BLACKLIST_FILTER (exclude categories: [1, 2, 3])
4. WHITELIST_FILTER (include with weights: [{id: 10, weight: 0.6}, {id: 20, weight: 0.4}])
5. FINAL_CATEGORIES_BALANCER (targetCount: 50, defaultQuota: 0.2)
```

### Diversity-Focused Pipeline
```
1. START_DATASOURCE
2. APPROVED_FILTER
3. ARTIST_DIVERSITY
4. ARTIST_RECENCY_PENALTY
5. TRACK_RECENCY_PENALTY
6. FINAL_LIMITER (targetCount: 50)
```

### Comprehensive Pipeline
```
1. START_DATASOURCE
2. APPROVED_FILTER
3. BLACKLIST_FILTER (exclude unwanted categories)
4. WHITELIST_FILTER (focus on preferred categories)
5. ARTIST_DIVERSITY
6. ARTIST_RECENCY_PENALTY
7. TRACK_RECENCY_PENALTY
8. FINAL_CATEGORIES_BALANCER (balanced category distribution)
```

## Key Concepts

### Chance Column
Many steps work with a `chance` column that represents selection probability:
- Starts at `1.0` (100% relative probability)
- Modified by penalty/weighting steps
- Used by final selection steps for weighted random selection
- Automatically added if missing (defaults to 1.0)

### Artist Deduplication
Final selection steps ensure each artist appears at most once:
- Applied after all other processing
- Keeps the highest-ranked track per artist
- Ensures maximum artist diversity in final quiz

### Idempotency
All steps are designed to be idempotent:
- Can be re-run safely
- Temporary tables are dropped and recreated
- Results are deterministic (except for random selection)

### Intermediate Tables
Steps create intermediate tables for debugging:
- Named with game/generation/step identifiers
- Indexed for performance
- Can be inspected to understand step behavior

## Configuration Format

Most steps use JSON configuration stored in the `Step` entity's `cfg_data` field.

Example:
```java
// BlacklistFilterStepConfig
{
  "categoryIds": [1, 2, 3]
}

// WhitelistFilterStepConfig
{
  "categories": [
    {"id": 10, "weight": 0.6},
    {"id": 20, "weight": 0.4}
  ]
}

// FinalLimiterStepConfig
{
  "targetCount": 50
}
```

## Implementation Details

All processors:
- Extend `BasicStepProcessor`
- Call PostgreSQL functions for core logic
- Validate configuration on creation/update
- Generate statistics after execution
- Create indexes for performance

## Database Functions

Core logic resides in PostgreSQL functions in the `mu_quiz` schema:
- Prefix: `p_quiz_gen_tracks_step_*`
- Written in PL/pgSQL
- Use dynamic SQL for table/view creation
- Optimized with appropriate indexes

## See Also

- [Pipeline Architecture](../../docs/trashcan/pipeline-architecture.md) - Overall pipeline design
- [API Reference](../../docs/api.md) - Pipeline manipulation endpoints
- [Step Processor Code](../../../src/main/java/yurykorzun/art/universe/music/quiz/service/step/process/impl/) - Java implementations
