# Centralized Configuration Pattern

## Purpose

Provides runtime-tunable properties for application modules without redeployment. 
Property values are stored in PostgreSQL, managed by a dedicated service, and consumed via a Spring Boot auto-configuration client library.

## When to Use

Use this pattern when:
- A module has tunable parameters (delays, thresholds, rate limits, enable/disable flags)
- Values need to be changed at runtime without restart
- Multiple modules share the same operational knobs that should be auditable in one place

Do **not** use for:
- Environment-specific infrastructure config (DB URLs, ports, secrets) — use env files
- Static application wiring (Spring beans, profiles) — use `application.yml`

## Infrastructure

| Module | Role                                                      |
|--------|-----------------------------------------------------------|
| [config-service](../../../../../common/config/config-service/README.md) | REST API + PostgreSQL persistence for all property values |
| [config-client](../../../../../common/config/config-client/README.md) | Auto-configuration library for consumption by app modules |

## Implementation Steps

See [Usage section](../../../../../common/config/config-client/README.md#usage) in config-client's docs

## Property Key Naming Convention

Keys follow a hierarchical dot-separated structure derived from the module name and property purpose:

```
{module}.{category}.{specific-name}
```

Examples from existing modules:
- `lastfm.generator.schedule.delay-secs`
- `lastfm.generator.generate.artist-get-info`
- `spotify.parser.parse.artist-get`
- `spotify.performer.rate-limiter.min-delay-ms`

**Rules**:
- All lowercase, kebab-case segments
- Category segment groups related properties (e.g. `generate`, `parse`, `rate-limiter`)
- For enable/disable flags tied to an API call type: derive the suffix from `ApiCallType.name().toLowerCase().replace('_', '-')`
  - `ARTIST_GET_INFO` → key suffix `artist-get-info`
  - `GENERATE_` prefix: `lastfm.generator.generate.artist-get-info`
  - `PARSE_` prefix: `lastfm.parser.parse.artist-get-info`

## Enable/Disable Flags and `valueOf` Lookup

When a set of boolean flags maps 1:1 to an enum of API call types, avoid a switch expression. Use `Enum.valueOf()` to derive the property constant from the call type's name:

```java
private boolean isGenerationEnabled(LastfmApiCallType callType) {
    LastfmGeneratorProperty prop = LastfmGeneratorProperty.valueOf("GENERATE_" + callType.name());
    return configPropertyHolder.getBoolean(prop);
}
```

This makes the relationship self-maintaining: adding a new `LastfmApiCallType` only requires adding the corresponding `GENERATE_*` enum constant — no switch to update. If the constant is missing, `valueOf` throws `IllegalArgumentException` at the point of first use, giving a clear failure signal.


## Constraints

Optional. Validated on `PUT /api/v1/config/properties/{key}`.

```java
PropertyConstraints.ofRange(1, 3600)          // numeric min/max
PropertyConstraints.ofAllowedValues("A","B")   // allowedValues list
```

Pass `null` for unconstrained properties.

## Examples in Codebase

| Module | Enum | Properties |
|--------|------|------------|
| [lastfm-calls-generator](../../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/README.md) | [LastfmGeneratorProperty](../../../../../music/data/raw/lastfm/etl/lastfm-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/LastfmGeneratorProperty.java) | Scheduler delay, per-call-type enable flags, due-duration days |
| [lastfm-calls-performer](../../../../../music/data/raw/lastfm/etl/lastfm-calls-performer/README.md) | [LastfmPerformerProperty](../../../../../music/data/raw/lastfm/etl/lastfm-calls-performer/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/LastfmPerformerProperty.java) | Scheduler delay, calls-per-second rate |
| [lastfm-response-parser](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/README.md) | [LastfmParserProperty](../../../../../music/data/raw/lastfm/etl/lastfm-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/LastfmParserProperty.java) | Scheduler delay, per-call-type parse flags, quality thresholds |
| [lastfm-etl-rest-api](../../../../../music/data/raw/lastfm/etl/lastfm-etl-rest-api/README.md) | [LastfmMaintenanceProperty](../../../../../music/data/raw/lastfm/etl/lastfm-etl-rest-api/src/main/java/yurykorzun/art/universe/music/data/raw/lastfm/config/LastfmMaintenanceProperty.java) | Entity quality thresholds, unbind batch size |
| [spotify-calls-generator](../../../../../music/data/raw/spotify/etl/spotify-calls-generator/README.md) | [SpotifyGeneratorProperty](../../../../../music/data/raw/spotify/etl/spotify-calls-generator/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/config/SpotifyGeneratorProperty.java) | Scheduler delay, per-call-type enable flags, due-duration days |
| [spotify-calls-performer](../../../../../music/data/raw/spotify/etl/spotify-calls-performer/README.md) | [SpotifyPerformerProperty](../../../../../music/data/raw/spotify/etl/spotify-calls-performer/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/config/SpotifyPerformerProperty.java) | Scheduler delay, adaptive rate-limiter parameters |
| [spotify-response-parser](../../../../../music/data/raw/spotify/etl/spotify-response-parser/README.md) | [SpotifyParserProperty](../../../../../music/data/raw/spotify/etl/spotify-response-parser/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/config/SpotifyParserProperty.java) | Scheduler delay, per-call-type parse flags, staging iteration limits |
| [spotify-staging-applicator](../../../../../music/data/raw/spotify/etl/spotify-staging-applicator/README.md) | [SpotifyApplicatorProperty](../../../../../music/data/raw/spotify/etl/spotify-staging-applicator/src/main/java/yurykorzun/art/universe/music/data/raw/spotify/config/SpotifyApplicatorProperty.java) | Scheduler delay, cleanup delay, cleanup retention hours |

## See Also

- [config-service README](../../../../common/config/config-service/README.md)
- [config-client README](../../../../common/config/config-client/README.md)
- [Environment Profiles](environment-profiles.md) — for infrastructure/env config (not runtime tuning)
- [Spring Config Import](spring-config-import.md) — for sharing static config from library modules
