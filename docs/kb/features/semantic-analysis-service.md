# Semantic Analysis Service

## Overview

An LLM-powered pipeline that extracts structured information from music entities and proposes
enrichments to master data. The pipeline consists of multiple independent services connected
through a DB-backed ticket queue.


## Architecture

### Three-Tier Decoupled Flow

- Ticket creators create tickets for semantic analysis
- Ticket intake service prepares metadata for analysis requests
- Semantic Analyser build prompts, executes them and saves responses in DB
- Response Parser parses responses and writes them to the 'proposals schema'
- Applicator auto-applies proposals that meet the requirements. The rest of proposals can be applied manually from UI.

## Analysis Modes

The pipeline supports two fundamentally different analysis modes, each with its own prompt strategy and LLM configuration:

| Mode | Code | Purpose | Temperature | Proposal Types |
|------|------|---------|-------------|----------------|
| `FULL_EXTRACTION` | 1 | Factual extraction of entities, relations, attributes from wiki/bio text | 0.2 | All types |
| `CREATIVE_CATEGORIZATION` | 2 | Lateral/creative category binding from entity names or lyrics | 1.2 | BIND_ENTITY_CATEGORY, CREATE_CATEGORY only |

### Full Extraction

Conservative, high-confidence mode. The system prompt instructs the LLM to act as a "music data analyst"
extracting structured information. User prompt includes text samples, all proposal type schemas,
semantic attribute definitions, and the category tree.

### Creative Categorization

Playful, associative mode. The system prompt instructs the LLM to act as a "creative cultural analyst"
finding lateral associations through wordplay, acronyms, cultural references, phonetic similarity,
and visual imagery.

Examples:
- "ABBA" -> "abbreviations" (acronym of member initials)
- "Red Hot Chilli Peppers" -> "colours", "food", "weather/temperature"
- "AC/DC" -> "electricity", "punctuation in name"
- "Iron Maiden" -> "medieval", "mythology"

Lyrics-based categorization uses the same creative mode -- just larger input. No separate mode needed.

### Multi-Client LLM Architecture

Each analysis mode maps to a named LLM client instance via [configuration](../../../music/data/semantic/semantic-analyzer/src/main/resources/application.yml).

The `LlmClientRegistry` resolves `AnalysisMode` -> `LlmClient` at runtime. Each client instance
has its own temperature/model baked in at construction. The `LlmClient` interface is provider-agnostic.


## Proposal Types

| Code | Type | Description | May Create New? |
|------|------|-------------|-----------------|
| 1 | `CREATE_ENTITY` | New master entity | Entity |
| 2 | `CREATE_RELATION` | Relation between entities | -- |
| 3 | `CREATE_ATTRIBUTE` | Set attribute value | -- |
| 4 | `CREATE_ATTRIBUTE_DEF` | New attribute definition | attribute_def |
| 5 | `MODIFY_ATTRIBUTE` | Change existing value | -- |
| 6 | `BIND_ENTITY_CATEGORY` | Bind entity to category | -- |
| 7 | `CREATE_CATEGORY` | New category in hierarchy | Category |
| 8 | `CREATE_DICTIONARY_RECORD` | New dictionary entry | -- |
| 9 | `BIND_EXTERNAL_ENTITY` | Bind raw entity to master | -- |

Every proposal carries: `confidence` (0-100), `reasoning` (text), `synth_id` (for cross-referencing within a response), and `payload` (JSONB matching the proposal type schema).


## Resolution States

```
PENDING(1) --> APPROVED(2)       --> applied to master (single transaction)
           --> DECLINED(3)       --> manual decline
           --> SUPERSEDED(4)     --> newer version replaces
           --> AUTO_APPROVED(5)  --> confidence >= auto-approve threshold
           --> AUTO_DECLINED(6)  --> confidence < auto-decline threshold
```


## Versioning

Analysis version strings are namespaced by analysis mode:

| Mode | Version Namespace | Current |
|------|-------------------|---------|
| FULL_EXTRACTION | `unified-X.Y.Z` | `unified-1.0.0` |
| CREATIVE_CATEGORIZATION | `category-name-X.Y.Z` | `category-name-1.0.0` |

The `AnalysisVersions.currentVersionFor(mode)` helper maps mode to current version. Reprocessing
creates new tickets preserving the original `analysis_mode`.

### What Constitutes a Version Bump

- **Prompt template change** -> minor version bump
- **New proposal type added** -> major version bump
- **LLM model change alone** -> does NOT bump version (tracked for auditing only)


## Caching

| Cache | Contents | Refresh |
|-------|----------|---------|
| Category tree | All categories with parent hierarchy | Every 5 min (configurable) |
| Attribute defs | SEMANTIC attribute definitions (computation_type=4) | Every 5 min |


## Database Schema

All tables live in the `mu_semantic_analysis` schema. Database user `mu_sa_dm` has write access to
`mu_semantic_analysis` and read access to `mu`.

### Tables

- `analysis_ticket` -- ticket queue (see Ticket Table above)
- `analysis_request` -- one per LLM invocation, with input hash, version, provider/model, tokens, raw response
- `proposal` -- individual proposals extracted from responses, with JSONB payload and resolution state

### Deduplication

`analysis_request` has `UNIQUE (input_hash, analysis_version)`. Same input + same version = skip.
The `analysis_mode` is also stored on `analysis_request` for audit purposes.


## See Also

- [Master Data Attributes](master-attributes.md) -- attribute definitions that proposals may target
- [Binding Raw Entities to Master](binding-raw-entities-to-master.md) -- produces entities eligible for analysis
- [Art Domain and Person Entity](art-domain-and-person-entity.md) -- Person entity the service proposes creating/linking
- [Relation Types and Attributes Catalogue](relation-types-and-attributes-catalogue.md) -- extractable relation types and attributes
- [Coded Enums Pattern](../patterns/backend/entities/coded-enums.md) -- pattern used by ProposalType, AnalysisMode enums
