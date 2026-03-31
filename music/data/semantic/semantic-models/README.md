# Semantic Models

Shared Java library containing enums and constants used across all semantic pipeline modules
(`semantic-analyzer`, `semantic-response-parser`, `semantic-applicator`). No Spring dependencies --
only plain Java with Jackson annotations.


## Key Classes

- [`ProposalType`](src/main/java/yurykorzun/art/universe/music/data/semantic/model/ProposalType.java) --
  Coded enum for proposal types: CREATE_ENTITY(1), CREATE_RELATION(2), CREATE_ATTRIBUTE(3),
  CREATE_ATTRIBUTE_DEF(4), MODIFY_ATTRIBUTE(5), BIND_ENTITY_CATEGORY(6), CREATE_CATEGORY(7),
  CREATE_DICTIONARY_RECORD(8), BIND_EXTERNAL_ENTITY(9)

- [`AnalysisMode`](src/main/java/yurykorzun/art/universe/music/data/semantic/model/AnalysisMode.java) --
  Coded enum determining prompt strategy and LLM client selection:
  FULL_EXTRACTION(1), CREATIVE_CATEGORIZATION(2)

- [`AnalysisVersions`](src/main/java/yurykorzun/art/universe/music/data/semantic/model/AnalysisVersions.java) --
  Version string constants per mode. `currentVersionFor(AnalysisMode)` maps mode to current version.
  FULL_EXTRACTION uses `unified-X.Y.Z` namespace, CREATIVE_CATEGORIZATION uses `category-name-X.Y.Z`.

All enums implement the [Coded interface](../../../docs/kb/patterns/backend/entities/coded-enums.md)
from `commons-jpa` and register with `CodedRegistry`.


## See Also

- [Semantic Analysis Service](../../../docs/kb/features/semantic-analysis-service.md) -- feature overview
- [Semantic Analyzer](../semantic-analyzer/README.md) -- main consumer of these models
- [Ticket Intake Service](../ticket-intake-service/README.md) -- Go service using integer codes from these enums
