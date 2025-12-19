# Quiz Packs Generation Pipeline

The Quiz Packs Generation Pipeline is a sequence of steps. Steps execute sequentially - each step's output becomes the next step's input:
- taking a set of master tracks as an input
- depending on step type, may require configuration (e.g. expected number of records in output)
- outputs a sub- or even super-set of the input set

Steps in the pipeline can thus be arranged in any order, with a single exception:
there must be an 'initial' step taking no input set and providing the 'initial' set for generation.

Many steps work with a `chance` column representing selection probability. Starts at 1.0 and is modified by penalty/weighting steps.
If the column is not present in the input dataset, it will be added in the output dataset.

## Modules Involved

- [Music UI](../../../../music/ui/README.md) - Provides UI for pipeline configuration and execution
- [Music Quiz service](../../../../music/quiz/README.md) - Serves as metadata storage and execution engine

## Steps Overview

| Name | Description | Backend Implementation |
|------|-------------|----------------------|
| **Start Datasource** | Initializes the pipeline with tracks from the master datasource. Must be the first step in every pipeline. | [start-datasource.md](../../../../music/quiz/docs/pipeline/steps/start-datasource.md) |
| **Approved Filter** | Filters to include only tracks that have been bound/approved in the quiz database. Binary yes/no decision. | [approved-filter.md](../../../../music/quiz/docs/pipeline/steps/approved-filter.md) |
| **Blacklist Filter** | Excludes tracks belonging to specified categories. Binary filter - tracks are removed entirely if they match blacklisted categories. | [blacklist-filter.md](../../../../music/quiz/docs/pipeline/steps/blacklist-filter.md) |
| **Whitelist Filter** | Includes only specified categories and applies weighted compensation to achieve target distribution. Adjusts probabilities rather than hard filtering. | [whitelist-filter.md](../../../../music/quiz/docs/pipeline/steps/whitelist-filter.md) |
| **Artist Diversity** | Reduces selection probability for artists with many tracks in the pool. Prevents artists with large catalogs from dominating the quiz. | [artist-diversity.md](../../../../music/quiz/docs/pipeline/steps/artist-diversity.md) |
| **Artist Recency Penalty** | Applies time-based penalty to artists that appeared in recently approved quizzes. Lenient approach with minimum 20% chance remaining. | [artist-recency-penalty.md](../../../../music/quiz/docs/pipeline/steps/artist-recency-penalty.md) |
| **Track Recency Penalty** | Applies time-based penalty to tracks that appeared in recently approved quizzes. Strict approach - completely excludes tracks used within last month. | [track-recency-penalty.md](../../../../music/quiz/docs/pipeline/steps/track-recency-penalty.md) |
| **Limiter** | Performs final selection of N tracks using weighted random selection with artist deduplication. Simple count-based approach. | [final-limiter.md](../../../../music/quiz/docs/pipeline/steps/final-limiter.md) |
| **Categories Balancer** | Balances final track selection across categories using weighted quotas. Ensures specified distribution while maintaining artist diversity. | [final-categories-balancer.md](../../../../music/quiz/docs/pipeline/steps/final-categories-balancer.md) |
