# Kafka Integration

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#6-kafka-integration)

## Motivation

In the LastFM pipeline, the Calls Performer polls the `api_call` table for PENDING calls. This works but has drawbacks:
- **Single consumer**: Only one Performer instance can process calls without complex DB-level locking
- **Polling overhead**: Frequent SELECTs on the api_call table, even when nothing is pending
- **No priority support**: All calls are equal; can't prioritize one API call type over others
- **Scaling requires coordination**: Multiple performers would need partition/ownership logic in the DB

Kafka solves these naturally while the database remains the source of truth for deduplication and staleness.

## Hybrid Architecture

```
┌─────────────────────────────────────────────────┐
│                Calls Generator                   │
│                                                  │
│  1. Query DB: "which entities are stale?"        │
│  2. Query DB: "which calls are already pending?" │
│  3. Deduplicate                                  │
│  4. INSERT into api_call (status=CREATED)        │
│  5. Produce to Kafka topic (by call type)        │
│  6. UPDATE api_call status → PENDING             │
└──────────────────────┬──────────────────────────┘
                       │
                       ▼
    ┌──────────────────────────────────────────────┐
    │              Kafka Topics                     │
    │                                               │
    │  spotify.calls.artist-get                     │
    │  spotify.calls.artist-albums                  │
    │  spotify.calls.album-get                      │
    │  spotify.calls.album-tracks                   │
    │  spotify.calls.track-get                      │
    │  spotify.calls.search-artist                  │
    │  spotify.calls.search-album                   │
    │  spotify.calls.search-track                   │
    └──────────────────────┬───────────────────────┘
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
       ┌──────────┐ ┌──────────┐ ┌──────────┐
       │Performer │ │Performer │ │Performer │  (Consumer group)
       │    #1    │ │    #2    │ │    #3    │
       └──────────┘ └──────────┘ └──────────┘
```

### What Goes Through DB vs Kafka

| Concern | Mechanism | Why |
|---------|-----------|-----|
| Deduplication | Database (api_call table) | Need to check if call already exists |
| Staleness detection | Database (entity due_dttm) | Need to compare timestamps |
| Call dispatch | Kafka | Enables scaling, priority, backpressure |
| Response storage | Database (api_response table) | Needs durable storage with query capability |
| Call status tracking | Database | Needs to survive Kafka consumer restarts |

## Topic Design: Per Call Type

### Topics

One topic per `SpotifyApiCallType`:

```
spotify.calls.artist-get       — Partitions: 3
spotify.calls.artist-albums    — Partitions: 3
spotify.calls.album-get        — Partitions: 3
spotify.calls.album-tracks     — Partitions: 3
spotify.calls.track-get        — Partitions: 3
spotify.calls.search-artist    — Partitions: 3
spotify.calls.search-album     — Partitions: 1
spotify.calls.search-track     — Partitions: 1
```

Retention: 3 days for all (calls that aren't consumed in 3 days are stale anyway).

Per-call-type topics enable:
- **Granular quota control** — allocate % of rate budget per call type
- **Selective pause** — disable a specific call type without affecting others
- **Independent monitoring** — track lag per call type
- **Ordering guarantees** — within a call type, same spotify_id → same partition

### Message Schema

```json
{
    "apiCallId": 12345,
    "type": "ARTIST_GET",
    "spotifyId": "0OdUWJ0sBjDrqHygGUXeCF",
    "entityType": "ARTIST",
    "entityId": 678,
    "createdAt": "2026-03-02T10:30:00Z"
}
```

Key: `spotifyId` — ensures operations on the same entity are ordered and go to the same partition.

## Weighted Quota System

### Configuration

Each call type gets a configurable **weight** that determines its share of the total rate budget:

```yaml
spotify:
  performer:
    total-rate: 7.0  # requests/second (conservative, under the 8.3 limit)
    quotas:
      artist-get:      30    # 30% → ~2.1 req/s
      artist-albums:   20    # 20% → ~1.4 req/s
      album-get:       15    # 15% → ~1.05 req/s
      search-artist:   15    # 15% → ~1.05 req/s
      track-get:       10    # 10% → ~0.7 req/s
      album-tracks:    5     # 5%  → ~0.35 req/s
      search-album:    3     # 3%  → ~0.21 req/s
      search-track:    2     # 2%  → ~0.14 req/s
```

Weights are relative — they're normalized to percentages at runtime. Changing `artist-get` from 30 to 60 doubles its share relative to others.

### Weighted Round-Robin Consumer

Instead of independent `@KafkaListener` per topic (which would fight over the rate limiter unpredictably), a single orchestrator manages consumption across all topics:

```java
@Component
public class WeightedCallConsumer {

    private final Map<SpotifyApiCallType, KafkaConsumer<String, SpotifyCallMessage>> consumers;
    private final Map<SpotifyApiCallType, Integer> weights;        // from config
    private final Map<SpotifyApiCallType, Integer> currentTokens;  // replenished each cycle
    private final SpotifyRateLimiter rateLimiter;
    private final SpotifyCallExecutor executor;

    /**
     * Main consumption loop. Each cycle:
     * 1. Replenish tokens proportionally from weights
     * 2. Poll from each topic up to its token count
     * 3. Execute calls, gated by the shared rate limiter
     */
    @Scheduled(fixedDelay = 1000)
    public void consumeCycle() {
        replenishTokens();

        for (var entry : consumers.entrySet()) {
            SpotifyApiCallType type = entry.getKey();
            KafkaConsumer<String, SpotifyCallMessage> consumer = entry.getValue();
            int tokens = currentTokens.getOrDefault(type, 0);

            if (tokens <= 0) continue;

            ConsumerRecords<String, SpotifyCallMessage> records =
                consumer.poll(Duration.ofMillis(100));

            int consumed = 0;
            for (var record : records) {
                if (consumed >= tokens) break;
                rateLimiter.acquire();  // Blocks until global rate budget allows
                executor.execute(record.value());
                consumed++;
            }
            currentTokens.put(type, tokens - consumed);

            // If topic was empty, its unused tokens redistribute
            if (records.isEmpty()) {
                redistributeTokens(type, tokens);
            }
        }
    }

    private void replenishTokens() {
        int totalTokensPerCycle = (int) (totalRate * cycleDurationSeconds);
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        for (var entry : weights.entrySet()) {
            int tokens = (int) Math.ceil(
                (double) entry.getValue() / totalWeight * totalTokensPerCycle
            );
            currentTokens.put(entry.getKey(), tokens);
        }
    }

    private void redistributeTokens(SpotifyApiCallType emptyType, int unusedTokens) {
        // Distribute unused tokens to other types proportionally
        int remainingWeight = weights.entrySet().stream()
            .filter(e -> !e.getKey().equals(emptyType))
            .mapToInt(Map.Entry::getValue)
            .sum();

        for (var entry : weights.entrySet()) {
            if (entry.getKey().equals(emptyType)) continue;
            int extra = (int) ((double) entry.getValue() / remainingWeight * unusedTokens);
            currentTokens.merge(entry.getKey(), extra, Integer::sum);
        }
    }
}
```

### Quota Behaviors

| Scenario | Behavior |
|----------|----------|
| All topics have messages | Each type gets its configured % of rate budget |
| One topic is empty | Its unused budget redistributes to others proportionally |
| All topics except one are empty | That one type gets 100% of rate budget |
| Weight set to 0 | That call type is effectively paused |
| Weight changed at runtime | Takes effect on next replenish cycle (1 second) |

### Dynamic Quota Adjustment

Quotas can be adjusted at runtime via:

1. **Spring Cloud Config refresh** (if using config server)
2. **Actuator endpoint**: `POST /actuator/spotify/quotas` with new weights
3. **ConfigMap change + pod restart** (K8s)

Option 2 is the most practical for quick adjustments during operation.

## Rate Limiting

### Global Token Bucket

A shared token bucket limits actual HTTP calls regardless of which topic they came from:

```java
public class SpotifyRateLimiter {
    // Spotify: 250 requests per 30 seconds ≈ 8.3/s
    // Conservative: 7/s to leave headroom
    private final RateLimiter limiter = RateLimiter.create(7.0);

    private volatile Instant pausedUntil = Instant.MIN;

    public void acquire() {
        // Wait if we're in a 429-triggered pause
        while (Instant.now().isBefore(pausedUntil)) {
            Thread.sleep(1000);
        }
        limiter.acquire();
    }

    public void pause(Duration duration) {
        pausedUntil = Instant.now().plus(duration);
    }
}
```

Using Guava's `RateLimiter` for single-instance deployments. For multi-instance, use a Redis-based distributed rate limiter (e.g., Resilience4j with Redis backend or a Lua script implementing token bucket in Redis).

### Retry-After Handling

If Spotify returns 429, the Performer:
1. Reads the `Retry-After` header
2. Pauses the global rate limiter for that duration
3. Does NOT commit the Kafka offset — the message will be redelivered
4. Logs the event for monitoring

```java
if (response.statusCode() == 429) {
    int retryAfter = response.headers().firstValue("Retry-After")
        .map(Integer::parseInt).orElse(30);
    rateLimiter.pause(Duration.ofSeconds(retryAfter));
    throw new RetryableException("Rate limited, retry after " + retryAfter + "s");
}
```

## Exactly-Once Semantics

We don't need exactly-once for API calls — duplicate calls are harmless (idempotent GETs). But we do want at-least-once delivery:

- Consumer commits offsets **after** successfully storing the response in DB
- If the consumer crashes after HTTP call but before DB write, the message is redelivered and the call is re-executed (harmless duplicate)
- If the consumer crashes after DB write but before offset commit, the call is re-executed but the response is deduplicated by `(api_call_id)` unique constraint

## Monitoring

Key Kafka metrics to expose:

| Metric | Granularity | Purpose |
|--------|-------------|---------|
| `spotify.kafka.consumer.lag` | Per topic, per partition | Backlog detection |
| `spotify.kafka.consumer.throughput` | Per topic | Actual consumption rate |
| `spotify.quota.weight` | Per call type | Current configured weight |
| `spotify.quota.effective-rate` | Per call type | Actual achieved rate (after redistribution) |
| `spotify.quota.empty-topic-ratio` | Per call type | % of cycles where topic was empty |
| `spotify.rate-limiter.permits.available` | Global | Token bucket state |
| `spotify.rate-limiter.pause.active` | Global | Whether 429-triggered pause is in effect |

## Phase 2 Fallback (No Kafka)

Before Kafka is integrated (Phase 3 of implementation), the Performer can poll the `api_call` table directly, same as LastFM. The weighted quota system can still work by grouping DB queries by call type:

```java
@Scheduled(fixedDelay = 1000)
public void pollAndExecute() {
    for (var entry : quotas.entrySet()) {
        SpotifyApiCallType type = entry.getKey();
        int limit = calculateTokensForType(type);
        if (limit <= 0) continue;

        List<SpotifyApiCall> calls = repository.findPendingByType(type, limit);
        for (SpotifyApiCall call : calls) {
            rateLimiter.acquire();
            execute(call);
        }
    }
}
```

This provides the same quota behavior without Kafka, just without horizontal scaling.
