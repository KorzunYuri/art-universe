# Pipeline Steps Index

This document lists step processors with links to their documentation.

## Implemented Steps

### Start Steps 

- [Start Datasource](steps/start-datasource.md) - Starting point: choose the dataset to generate a subset based on it. Currently is not configured: uses ALL [master tracks](../../../data/master/README.md#entities).

### Transforming Steps

- [Approved Filter](steps/approved-filter.md) - Filters out master tracks that are not 'bound' in current module
- [Artist Diversity](steps/artist-diversity.md) - Applies penalty to tracks of artists having many tracks. The coefficient formula is linear and, as experiments show, inefficient.
- [Artist Recency Penalty](steps/artist-recency-penalty.md) - Applies penalty to tracks of artists that have been played recently, i.e. finished in generations that have been approved.
- [Track Recency Penalty](steps/track-recency-penalty.md) - Applies penalty to tracks that have been played recently, i.e. finished in generations that have been approved.
- [Blacklist Filter](steps/blacklist-filter.md) - Filters out tracks that have relations with unwanted categories in [master data](../../../data/master/README.md#entities)
- [Whitelist Filter](steps/whitelist-filter.md) - Leaves only tracks from listed categories, and applies coefficients provided per-category. Coefficients are not efficient on big datasets so it's better to use equal values for all categories.

### Limiting Steps

- [Limiter](steps/final-limiter.md) - Keeps N randomly selected tracks, deduplicated by artist
- [Categories Balancer](steps/final-categories-balancer.md) - Given a target number of tracks T, list of categories with their coefficients and 'default' category with coefficient D:
  - Keeps T x D tracks from categories not present in list ('default' category)
  - Keeps T x (1 - D) tracks from provided categories, balancing them according to their coefficients