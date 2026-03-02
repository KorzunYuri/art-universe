# Kafka Integration

Back to [main plan](SPOTIFY_IMPLEMENTATION_PLAN.md#6-kafka-integration)

## Motivation

In the LastFM pipeline, the Calls Performer polls the `api_call` table for PENDING calls. This works but has drawbacks:
- **Single consumer**: Only one Performer instance can process calls without complex DB-level locking
- **Polling overhead**: Frequent SELECTs on the api_call table, even when nothing is pending
- **No priority support**: All calls are equal; can't prioritize seed requests over refreshes
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
│  5. Produce to Kafka topic                       │
│  6. UPDATE api_call status → PENDING             │
└──────────────────────┬──────────────────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │   Kafka Topics   │
              │                  │
              │ spotify.calls.   │
              │   seed           │ ← Priority: new entity discovery
              │   refresh        │ ← Normal: staleness re-fetch
              └────────┬────────┘
                       │
          ┌────────────┼────────────┐
          ▼            ▼            ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │Performer │ │Performer │ │Performer │   (Consumer group)
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

## Topic Design

### Topics

```
spotify.calls.seed      — Partitions: 3, Retention: 7 days
spotify.calls.refresh   — Partitions: 6, Retention: 3 days
```

Fewer partitions for seed (lower volume, needs ordering). More partitions for refresh (higher volume, order doesn't matter).

### Message Schema

```json
{
    "apiCallId": 12345,
    "type": "ARTIST_GET",
    "spotifyId": "0OdUWJ0sBjDrqHygGUXeCF",
    "entityType": "ARTIST",
    "entityId": 678,
    "priority": "SEED",
    "createdAt": "2026-03-02T10:30:00Z"
}
```

Key: `spotifyId` (ensures same entity always goes to same partition → ordering per entity).

### Consumer Priority

The Performer uses a weighted consumption strategy:

```java
@KafkaListener(topics = "spotify.calls.seed", groupId = "spotify-performer")
public void consumeSeed(SpotifyCallMessage message) {
    rateLimiter.acquire();  // Token bucket: ~8 req/s shared across all consumers
    execute(message);
}

@KafkaListener(topics = "spotify.calls.refresh", groupId = "spotify-performer")
public void consumeRefresh(SpotifyCallMessage message) {
    rateLimiter.acquire();
    execute(message);
}
```

Priority is implemented by giving the seed topic consumer a higher `max.poll.records` (e.g., 10) vs refresh (e.g., 3). When both topics have messages, seed messages are consumed faster.

Alternatively, use a single topic with a priority header and a custom `ConsumerInterceptor` that reorders the poll buffer by priority. But two topics is simpler and sufficient.

## Rate Limiting

### Token Bucket

A shared token bucket (backed by Redis or in-memory if single-instance) limits actual HTTP calls:

```java
public class SpotifyRateLimiter {
    // Spotify: 250 requests per 30 seconds ≈ 8.3/s
    // Conservative: 7/s to leave headroom
    private final RateLimiter limiter = RateLimiter.create(7.0);

    public void acquire() {
        limiter.acquire();  // Blocks until a permit is available
    }
}
```

Using Guava's `RateLimiter` for single-instance deployments. For multi-instance, use a Redis-based distributed rate limiter (e.g., Resilience4j with Redis backend).

### Retry-After Handling

If Spotify returns 429, the Performer:
1. Reads the `Retry-After` header
2. Pauses the rate limiter for that duration
3. Requeues the failed message (produce back to topic or nack)
4. Logs the event for monitoring

```java
if (response.statusCode() == 429) {
    int retryAfter = response.headers().firstValue("Retry-After")
        .map(Integer::parseInt).orElse(30);
    rateLimiter.pause(Duration.ofSeconds(retryAfter));
    // Message will be retried via Kafka retry mechanism
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
- `spotify.kafka.consumer.lag` — per topic, per partition
- `spotify.kafka.consumer.throughput` — messages/second
- `spotify.rate-limiter.permits.available` — current token bucket state
- `spotify.rate-limiter.pause.active` — whether 429-triggered pause is in effect

## Phase 2 Fallback (No Kafka)

Before Kafka is integrated (Phase 2 of implementation), the Performer can poll the `api_call` table directly, same as LastFM:

```java
@Scheduled(fixedDelay = 5000)
public void pollAndExecute() {
    List<SpotifyApiCall> pending = repository.findPendingCalls(limit);
    for (SpotifyApiCall call : pending) {
        rateLimiter.acquire();
        execute(call);
    }
}
```

This allows the full pipeline to work without Kafka infrastructure. Kafka is introduced in Phase 3 as an optimization for scaling.
