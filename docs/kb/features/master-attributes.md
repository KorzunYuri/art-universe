# Master Data Attributes

## Overview

A rich attribute system for master music data. Attributes describe properties of master entities
(Artist, Album, Track) and the relations between them. They may be constant, time-bound to a
specific event, or valid for a date range.

The system uses a **4-tier computation model** where attributes are classified by how their
values are produced, and a **view-based serving model** where raw data stays in source schemas
and is read through `mu_view` views rather than copied to master.

Related: [Semantic Analysis Service](semantic-analysis-service.md) -- LLM-driven extraction
that produces SEMANTIC attribute proposals.

Related: [Master Attribute Calculator Plan](master-attribute-calculator-plan.md) -- exchange
schema and calculator implementation for DERIVED/COMPOSITE attributes.


## Computation Types

| Code | Type | Description | Data Source | Examples |
|------|------|-------------|-------------|----------|
| 1 | **RAW** | Pass-through from a single external source | `lastfm`, `spotify` | listeners_count, play_count, popularity |
| 2 | **DERIVED** | Calculated within one source using formulas | `lastfm`, `spotify` | rank_in_artist_by_listeners (window function) |
| 3 | **COMPOSITE** | Calculated across multiple sources | `multiple` | combined_popularity (weighted blend) |
| 4 | **SEMANTIC** | Extracted by LLM from unstructured text | NULL | country_of_origin, founding_year, activity_periods |
| 5 | **MANUAL** | Human-curated via UI | NULL | User-defined attributes |

### View-Based Serving

RAW and DERIVED attributes are **not copied** to master. Since all schemas live in one
`art_universe` database, `mu_view` views read directly from raw schemas (`mu_raw_lastfm`,
future `mu_raw_spotify`). This avoids data duplication and keeps raw data as the single source
of truth.

The `entity_attribute_value` table stores only MANUAL overrides, SEMANTIC values (from LLM),
and COMPOSITE values that need materialization. A stored value always wins over a view-derived
value for manual curation.


## Domain Model

- [Attribute Definition](../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/attribute/AttributeDef.java)
- [Attribute Definition Applicability](../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/attribute/AttributeDefApplicability.java)
- [Attribute Value](../../../music/data/master/src/main/java/yurykorzun/art/universe/music/data/master/entity/attribute/EntityAttributeValue.java)


## Temporal Types

| Type | Meaning | Columns Used | Examples |
|------|---------|--------------|---------|
| **CONSTANT** | Value does not change | value only | MusicBrainz ID, country_of_origin, ISRC |
| **INSTANT** | True at a specific date | `event_date` | founding_year, release_date, on_tour snapshot |
| **PERIOD** | Valid within a date range | `valid_from`, `valid_till` (NULL = ongoing) | activity_periods, listeners_count snapshot |

For multi-value PERIOD attributes (e.g. `activity_periods`), each period is a separate
`entity_attribute_value` row with its own `valid_from`/`valid_till`.


## Enums

| Enum | Values | Module |
|------|--------|--------|
| `AttributeDataType` | NUMERIC(1), STRING(2), DATE(3), BOOLEAN(4) | music-data-master |
| `AttributeTemporalType` | CONSTANT(1), INSTANT(2), PERIOD(3) | music-data-master |
| `AttributeComputationType` | RAW(1), DERIVED(2), COMPOSITE(3), SEMANTIC(4), MANUAL(5) | music-data-master |
| `AttributeSourceType` | MANUAL(1), CALCULATED(2), LLM_EXTRACTED(3) | music-data-master |

All follow the [Coded Enum Pattern](../patterns/backend/entities/coded-enums.md).


## REST API

```
GET    /api/v1/attributes/definitions                     -- List (filterable by target_type)
GET    /api/v1/attributes/definitions/{id}                -- Get one
POST   /api/v1/attributes/definitions                     -- Create user-defined
PUT    /api/v1/attributes/definitions/{id}                -- Update
DELETE /api/v1/attributes/definitions/{id}                -- Delete user-defined only

GET    /api/v1/attributes/{targetType}/{targetId}         -- List values for entity
POST   /api/v1/attributes/{targetType}/{targetId}         -- Create value (MANUAL source)
DELETE /api/v1/attributes/values/{valueId}                -- Delete value
```


## See Also

- [Semantic Analysis Service](semantic-analysis-service.md) -- LLM-driven extraction producing attribute proposals
- [Master Attribute Calculator Plan](master-attribute-calculator-plan.md) -- exchange schema and calculator
- [Relation Types and Attributes Catalogue](relation-types-and-attributes-catalogue.md) -- complete catalogue
- [SCD2 Attribute History Pattern](../patterns/backend/database/scd2-attribute-history.md) -- temporal storage in raw layer
- [Coded Enums Pattern](../patterns/backend/entities/coded-enums.md) -- pattern used by attribute enums
