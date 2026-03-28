package yurykorzun.art.universe.music.data.semantic.analyzer.prompt;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.analyzer.cache.CategoryCacheService;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.Set;

@Component
public class PromptBuilder {

    private final PayloadSchemaRegistry schemaRegistry;
    private final CategoryCacheService categoryCacheService;

    public PromptBuilder(PayloadSchemaRegistry schemaRegistry, CategoryCacheService categoryCacheService) {
        this.schemaRegistry = schemaRegistry;
        this.categoryCacheService = categoryCacheService;
    }

    public String buildSystemPrompt() {
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

            Respond ONLY with valid JSON in the format:
            {"proposals": [...]}
            """;
    }

    public String buildUserPrompt(
        String subjectType,
        String subjectName,
        Long subjectId,
        String textSamplesJson,
        Set<ProposalType> expectedTypes,
        Set<Integer> expectedEntityTypes
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Subject\n");
        sb.append(String.format("Type: %s, Name: %s", subjectType, subjectName));
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

        String categoryTree = categoryCacheService.getCategoryTreeJson();
        if (categoryTree != null && !categoryTree.isEmpty()) {
            sb.append("## Existing Category Tree\n");
            sb.append(categoryTree);
            sb.append("\n\n");
        }

        sb.append("Analyze the text samples and produce proposals. Use existing category IDs when applicable.\n");

        return sb.toString();
    }
}
