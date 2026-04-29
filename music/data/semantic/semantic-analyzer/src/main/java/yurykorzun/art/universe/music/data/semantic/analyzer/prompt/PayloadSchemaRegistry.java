package yurykorzun.art.universe.music.data.semantic.analyzer.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFieldType;
import yurykorzun.art.universe.music.data.semantic.model.PayloadFields;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PayloadSchemaRegistry {

    private final ObjectMapper objectMapper;
    private final Map<ProposalType, ObjectNode> schemas = new EnumMap<>(ProposalType.class);

    public PayloadSchemaRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        initSchemas();
    }

    public String getSchemasJson(Set<ProposalType> requestedTypes, Set<MasterEntityType> entityTypes) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode proposalTypesNode = root.putObject("proposal_types");

            Set<ProposalType> types = (requestedTypes != null && !requestedTypes.isEmpty())
                ? requestedTypes
                : EnumSet.allOf(ProposalType.class);

            for (ProposalType type : types) {
                ObjectNode schema = schemas.get(type);
                if (schema != null) {
                    proposalTypesNode.set(type.getName(), schema.deepCopy());
                }
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize proposal type schemas", e);
        }
    }

    private void initSchemas() {
        schemas.put(ProposalType.CREATE_ENTITY, buildCreateEntitySchema());
        schemas.put(ProposalType.CREATE_RELATION, buildCreateRelationSchema());
        schemas.put(ProposalType.CREATE_ATTRIBUTE, buildCreateAttributeSchema());
        schemas.put(ProposalType.CREATE_ATTRIBUTE_DEF, buildCreateAttributeDefSchema());
        schemas.put(ProposalType.BIND_ENTITY_CATEGORY, buildBindEntityCategorySchema());
        schemas.put(ProposalType.CREATE_CATEGORY, buildCreateCategorySchema());
        schemas.put(ProposalType.BIND_EXTERNAL_ENTITY, buildBindExternalEntitySchema());
    }

    private ObjectNode buildCreateEntitySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a new master entity");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.ENTITY_TYPE, PayloadFieldType.STRING, true, "ARTIST, ALBUM, TRACK, PERSON, CATEGORY");
        addField(fields, PayloadFields.NAME, PayloadFieldType.STRING, true, null);
        return schema;
    }

    private ObjectNode buildCreateRelationSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a relation between two entities");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.SOURCE_ENTITY_TYPE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.SOURCE_ENTITY_ID, PayloadFieldType.INTEGER, false, "existing entity ID (null if using ref)");
        addField(fields, PayloadFields.SOURCE_ENTITY_REF, PayloadFieldType.STRING, false, "synth_id of a CREATE_ENTITY proposal");
        addField(fields, PayloadFields.TARGET_ENTITY_TYPE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.TARGET_ENTITY_ID, PayloadFieldType.INTEGER, false, null);
        addField(fields, PayloadFields.TARGET_ENTITY_REF, PayloadFieldType.STRING, false, null);
        addField(fields, PayloadFields.RELATION_TYPE_ID, PayloadFieldType.INTEGER, true, null);
        addField(fields, PayloadFields.RELATION_TYPE_NAME, PayloadFieldType.STRING, false, "human-readable");
        addField(fields, PayloadFields.TEMPORAL_TYPE, PayloadFieldType.STRING, false, "CONSTANT, INSTANT, PERIOD");
        addField(fields, PayloadFields.EVENT_DATE, PayloadFieldType.DATE, false, null);
        addField(fields, PayloadFields.VALID_FROM, PayloadFieldType.DATE, false, null);
        addField(fields, PayloadFields.VALID_TILL, PayloadFieldType.DATE, false, null);
        return schema;
    }

    private ObjectNode buildCreateAttributeSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose setting an attribute value on an entity. " +
            "For multi-value PERIOD attributes (e.g. activity_periods), emit one proposal per period " +
            "with the same attribute_code but different valid_from/valid_till. Use null for ongoing periods.");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.ENTITY_TYPE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.ENTITY_ID, PayloadFieldType.INTEGER, false, null);
        addField(fields, PayloadFields.ENTITY_REF, PayloadFieldType.STRING, false, null);
        addField(fields, PayloadFields.ATTRIBUTE_CODE, PayloadFieldType.STRING, true, "must match a code from Available Semantic Attributes when applicable");
        addField(fields, PayloadFields.VALUE, PayloadFieldType.STRING, true, "string representation of the value");
        addField(fields, PayloadFields.VALUE_TYPE, PayloadFieldType.STRING, true, "NUMERIC, STRING, DATE, BOOLEAN");
        addField(fields, PayloadFields.EVENT_DATE, PayloadFieldType.DATE, false, "for INSTANT temporal_type");
        addField(fields, PayloadFields.VALID_FROM, PayloadFieldType.DATE, false, "period start (YYYY-MM-DD); use YYYY-01-01 if only year known");
        addField(fields, PayloadFields.VALID_TILL, PayloadFieldType.DATE, false, "period end (YYYY-MM-DD); null if ongoing");
        return schema;
    }

    private ObjectNode buildCreateAttributeDefSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a new attribute definition");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.CODE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.NAME, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.DATA_TYPE, PayloadFieldType.STRING, true, "NUMERIC, STRING, DATE, BOOLEAN");
        addField(fields, PayloadFields.TEMPORAL_TYPE, PayloadFieldType.STRING, true, "CONSTANT, INSTANT, PERIOD");
        addField(fields, PayloadFields.APPLICABLE_TO, PayloadFieldType.ARRAY, true, "array of entity type strings");
        return schema;
    }

    private ObjectNode buildBindEntityCategorySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose binding an entity to a category");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.ENTITY_TYPE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.ENTITY_ID, PayloadFieldType.INTEGER, false, null);
        addField(fields, PayloadFields.ENTITY_REF, PayloadFieldType.STRING, false, null);
        addField(fields, PayloadFields.CATEGORY_NAME, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.CATEGORY_ID, PayloadFieldType.INTEGER, false, "ID from the provided category tree");
        return schema;
    }

    private ObjectNode buildCreateCategorySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a new category in the hierarchy");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.NAME, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.PARENT_CATEGORY_NAME, PayloadFieldType.STRING, false, null);
        addField(fields, PayloadFields.PARENT_CATEGORY_ID, PayloadFieldType.INTEGER, false, null);
        return schema;
    }

    private ObjectNode buildBindExternalEntitySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose binding a raw external entity to a master entity");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, PayloadFields.DATA_SOURCE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.EXTERNAL_ID, PayloadFieldType.INTEGER, true, null);
        addField(fields, PayloadFields.MASTER_ENTITY_TYPE, PayloadFieldType.STRING, true, null);
        addField(fields, PayloadFields.MASTER_ENTITY_ID, PayloadFieldType.INTEGER, false, null);
        addField(fields, PayloadFields.MASTER_ENTITY_REF, PayloadFieldType.STRING, false, null);
        addField(fields, PayloadFields.MASTER_ENTITY_NAME, PayloadFieldType.STRING, true, null);
        return schema;
    }

    private void addField(ObjectNode fields, String name, PayloadFieldType type, boolean required, String description) {
        ObjectNode field = fields.putObject(name);
        field.put("type", type.getValue());
        field.put("required", required);
        if (description != null) {
            field.put("description", description);
        }
    }
}
