package yurykorzun.art.universe.music.data.semantic.analyzer.prompt;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.AttributeDefCacheService;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.CategoryCacheService;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.Set;

@Component
public class PromptBuilder {

    private static final Set<ProposalType> CREATIVE_PROPOSAL_TYPES = Set.of(
            ProposalType.BIND_ENTITY_CATEGORY,
            ProposalType.CREATE_CATEGORY
    );

    private final PayloadSchemaRegistry schemaRegistry;
    private final CategoryCacheService categoryCacheService;
    private final AttributeDefCacheService attributeDefCacheService;

    public PromptBuilder(PayloadSchemaRegistry schemaRegistry,
                         CategoryCacheService categoryCacheService,
                         AttributeDefCacheService attributeDefCacheService) {
        this.schemaRegistry = schemaRegistry;
        this.categoryCacheService = categoryCacheService;
        this.attributeDefCacheService = attributeDefCacheService;
    }

    public String buildSystemPrompt(AnalysisMode mode) {
        return switch (mode) {
            case FULL_EXTRACTION -> buildFullExtractionSystemPrompt();
            case CREATIVE_CATEGORIZATION -> buildCreativeCategorizationSystemPrompt();
        };
    }

    public String buildUserPrompt(
        AnalysisMode mode,
        MasterEntityType subjectType,
        String subjectName,
        Long subjectId,
        String textSamplesJson,
        Set<ProposalType> expectedTypes,
        Set<MasterEntityType> expectedEntityTypes
    ) {
        return switch (mode) {
            case FULL_EXTRACTION -> buildFullExtractionUserPrompt(
                    subjectType, subjectName, subjectId, textSamplesJson, expectedTypes, expectedEntityTypes);
            case CREATIVE_CATEGORIZATION -> buildCreativeCategorizationUserPrompt(
                    subjectType, subjectName, subjectId, textSamplesJson);
        };
    }

    // ── Full Extraction ──

    private String buildFullExtractionSystemPrompt() {
        return """
            You are a music data analyst. You analyze text descriptions about music entities
            (artists, albums, tracks, persons) and extract structured information as proposals.

            Each proposal must include:
            - synth_id: a unique synthetic identifier (s_1, s_2, etc.) for cross-referencing
            - type: the proposal type from the provided schemas
            - confidence: 0-100 indicating how confident you are
            - reasoning: brief explanation of why you're making this proposal
            - payload: the proposal data matching the schema for that type

            When referencing entities created in other proposals, use the synth_id in
            entity_ref/source_entity_ref/target_entity_ref fields.

            For attribute proposals:
            - Use attribute codes from the "Available Semantic Attributes" section when applicable.
            - For multi-value PERIOD attributes (e.g. activity_periods), produce one proposal
              per period with the appropriate valid_from/valid_till dates.
              Example: an artist active 1996-2003 then 2006-present would yield two proposals
              with the same attribute_code but different valid_from/valid_till values.
            - For dates where only the year is known, use YYYY-01-01 format.
            - Use null for valid_till when a period is ongoing.

            Respond ONLY with valid JSON in the format:
            {"proposals": [...]}
            """;
    }

    private String buildFullExtractionUserPrompt(
        MasterEntityType subjectType,
        String subjectName,
        Long subjectId,
        String textSamplesJson,
        Set<ProposalType> expectedTypes,
        Set<MasterEntityType> expectedEntityTypes
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Subject\n");
        sb.append(String.format("Type: %s, Name: %s", subjectTypeLabel(subjectType), subjectName));
        if (subjectId != null) {
            sb.append(String.format(", ID: %d", subjectId));
        }
        sb.append("\n\n");

        sb.append("## Text Samples\n");
        sb.append(textSamplesJson);
        sb.append("\n\n");

        sb.append("## Available Proposal Types and Payload Schemas\n");
        sb.append(schemaRegistry.getSchemasJson(expectedTypes, expectedEntityTypes));
        sb.append("\n\n");

        String attributeDefs = attributeDefCacheService.getAttributeDefsJson();
        if (attributeDefs != null && !"[]".equals(attributeDefs)) {
            sb.append("## Available Semantic Attributes\n");
            sb.append("Use these attribute codes in create_attribute proposals ");
            sb.append("when the text contains matching information.\n");
            sb.append(attributeDefs);
            sb.append("\n\n");
        }

        String categoryTree = categoryCacheService.getCategoryTreeJson();
        if (categoryTree != null && !categoryTree.isEmpty()) {
            sb.append("## Existing Category Tree\n");
            sb.append(categoryTree);
            sb.append("\n\n");
        }

        sb.append("Analyze the text samples and produce proposals. ");
        sb.append("Use existing category IDs and attribute codes when applicable.\n");

        return sb.toString();
    }

    // ── Creative Categorization ──

    private String buildCreativeCategorizationSystemPrompt() {
        return """
            You are a creative cultural analyst specializing in lateral associations, wordplay, \
            and hidden connections in music entity names and lyrics.

            Your task is to find creative, surprising, yet defensible category bindings for music entities. \
            Look for connections through:
            - **Wordplay and puns**: hidden words, double meanings, homophones
            - **Acronyms and abbreviations**: "ABBA" → "abbreviations", "AC/DC" → "electricity"
            - **Literal word meanings**: "Red Hot Chilli Peppers" → "colours", "food", "weather/temperature"
            - **Cultural and historical references**: "Iron Maiden" → "medieval", "mythology"
            - **Visual imagery**: what the name evokes visually
            - **Phonetic similarity**: words that sound like other words or concepts
            - **Linguistic roots**: etymology, borrowed words, language origins
            - **Emotional and sensory connotations**: what feelings or senses the name triggers

            Be inventive and thorough — find multiple connections per entity when possible. \
            Each connection should be genuinely defensible, not forced. Aim for categories that \
            would make someone say "oh, that's clever" rather than "that's a stretch."

            If an appropriate category does not exist in the provided category tree, propose creating it. \
            Prefer binding to existing categories when the fit is strong.

            Each proposal must include:
            - synth_id: a unique synthetic identifier (s_1, s_2, etc.)
            - type: either "bind_entity_category" or "create_category"
            - confidence: 0-100 (how defensible is the creative connection?)
            - reasoning: explain the creative connection clearly
            - payload: the proposal data matching the schema

            Respond ONLY with valid JSON in the format:
            {"proposals": [...]}
            """;
    }

    private String buildCreativeCategorizationUserPrompt(
        MasterEntityType subjectType,
        String subjectName,
        Long subjectId,
        String textSamplesJson
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Subject\n");
        sb.append(String.format("Type: %s, Name: %s", subjectTypeLabel(subjectType), subjectName));
        if (subjectId != null) {
            sb.append(String.format(", ID: %d", subjectId));
        }
        sb.append("\n\n");

        sb.append("## Input\n");
        sb.append(textSamplesJson);
        sb.append("\n\n");

        sb.append("## Available Proposal Types and Payload Schemas\n");
        sb.append(schemaRegistry.getSchemasJson(CREATIVE_PROPOSAL_TYPES, null));
        sb.append("\n\n");

        String categoryTree = categoryCacheService.getCategoryTreeJson();
        if (categoryTree != null && !categoryTree.isEmpty()) {
            sb.append("## Existing Category Tree\n");
            sb.append("Bind to existing categories when the connection is strong. ");
            sb.append("Propose new categories when no suitable one exists.\n");
            sb.append(categoryTree);
            sb.append("\n\n");
        }

        sb.append("Find creative, lateral category connections for this entity. ");
        sb.append("Be inventive but defensible. Look for wordplay, hidden meanings, ");
        sb.append("cultural references, and surprising associations.\n");

        return sb.toString();
    }

    private String subjectTypeLabel(MasterEntityType type) {
        return type.getName().toUpperCase();
    }
}
