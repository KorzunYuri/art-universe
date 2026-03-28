# Semantic Analyzer

Spring Boot service that polls analysis tickets, builds LLM prompts, calls LLM providers,
and stores raw responses.


## How It Works

1. [`TicketPollingScheduler`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/scheduler/TicketPollingScheduler.java)
   polls `mu_semantic_analysis.analysis_ticket` for pending tickets using `SELECT FOR UPDATE SKIP LOCKED`
2. [`SemanticAnalyzer`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/analysis/SemanticAnalyzer.java)
   resolves `AnalysisMode` from the ticket, which determines the analysis version, prompt strategy,
   and LLM client
3. [`PromptBuilder`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/prompt/PromptBuilder.java)
   constructs mode-specific system and user prompts. Full extraction includes all proposal schemas,
   semantic attribute definitions, and the category tree. Creative categorization uses a different
   system prompt emphasizing lateral thinking and limits schemas to BIND_ENTITY_CATEGORY + CREATE_CATEGORY
4. [`LlmClientRegistry`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/config/LlmClientRegistry.java)
   maps each `AnalysisMode` to a named `LlmClient` instance with its own model and temperature.
   Built from map-based config by [`LlmClientConfig`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/config/LlmClientConfig.java)
5. Raw LLM responses are stored in `mu_semantic_analysis.analysis_request` via
   [`AnalysisTicketDao`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/persistence/AnalysisTicketDao.java)

Version reprocessing (supersede old proposals + re-ticket) is handled by
[`ReprocessingService`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/reprocessing/ReprocessingService.java).


## Analysis Modes

| Mode | System Prompt | User Prompt | Temperature |
|------|---------------|-------------|-------------|
| FULL_EXTRACTION | "Music data analyst" -- factual extraction | All schemas + attributes + categories | 0.2 |
| CREATIVE_CATEGORIZATION | "Creative cultural analyst" -- lateral thinking, wordplay | BIND/CREATE category schemas + categories only | 1.2 |

Each mode maps to a named LLM client via `semantic.modes.*` config. Each client has its own
provider, model, and temperature baked in at construction. The [`LlmClient`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/llm/LlmClient.java)
interface is provider-agnostic; temperature is an internal detail of the implementation
(e.g. [`OpenAiLlmClient`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/llm/openai/OpenAiLlmClient.java)).


## Caches

- [`CategoryCacheService`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/cache/CategoryCacheService.java) --
  full category tree for prompt inclusion, refreshes every 5 min
- [`AttributeDefCacheService`](src/main/java/yurykorzun/art/universe/music/data/semantic/analyzer/cache/AttributeDefCacheService.java) --
  SEMANTIC attribute definitions (computation_type=4) for prompt inclusion


## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/analysis/tickets/stats` | Ticket count by status |
| POST | `/api/v1/analysis/reprocess` | Trigger version reprocessing |

Configuration: see [`application.yml`](src/main/resources/application.yml).


## See Also

- [Semantic Analysis Service](../../../docs/kb/features/semantic-analysis-service.md) -- feature overview
- [Semantic Models](../semantic-models/README.md) -- shared enums and constants
- [Ticket Intake Service](../ticket-intake-service/README.md) -- upstream ticket producer
- [Master Attributes](../../../docs/kb/features/master-attributes.md) -- attribute definitions cached by this service
