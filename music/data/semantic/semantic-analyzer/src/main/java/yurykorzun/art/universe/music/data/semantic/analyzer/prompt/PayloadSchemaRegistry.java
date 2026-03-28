package yurykorzun.art.universe.music.data.semantic.analyzer.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.*;

@Component
public class PayloadSchemaRegistry {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<ProposalType, ObjectNode> schemas = new EnumMap<>(ProposalType.class);

    public PayloadSchemaRegistry() {
        initSchemas();
    }

    public String getSchemasJson(Set<ProposalType> requestedTypes, Set<Integer> entityTypeCodes) {
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize schemas", e);
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
        addField(fields, "entity_type", "string", true, "ARTIST, ALBUM, TRACK, PERSON, CATEGORY");
        addField(fields, "name", "string", true, null);
        return schema;
    }

    private ObjectNode buildCreateRelationSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a relation between two entities");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "source_entity_type", "string", true, null);
        addField(fields, "source_entity_id", "integer", false, "existing entity ID (null if using ref)");
        addField(fields, "source_entity_ref", "string", false, "synth_id of a CREATE_ENTITY proposal");
        addField(fields, "target_entity_type", "string", true, null);
        addField(fields, "target_entity_id", "integer", false, null);
        addField(fields, "target_entity_ref", "string", false, null);
        addField(fields, "relation_type_id", "integer", true, null);
        addField(fields, "relation_type_name", "string", false, "human-readable");
        addField(fields, "temporal_type", "string", false, "CONSTANT, INSTANT, PERIOD");
        addField(fields, "event_date", "date", false, null);
        addField(fields, "valid_from", "date", false, null);
        addField(fields, "valid_till", "date", false, null);
        return schema;
    }

    private ObjectNode buildCreateAttributeSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose setting an attribute value on an entity");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "entity_type", "string", true, null);
        addField(fields, "entity_id", "integer", false, null);
        addField(fields, "entity_ref", "string", false, null);
        addField(fields, "attribute_code", "string", true, null);
        addField(fields, "value", "string", true, "string representation of the value");
        addField(fields, "value_type", "string", true, "NUMERIC, STRING, DATE, BOOLEAN");
        addField(fields, "event_date", "date", false, null);
        addField(fields, "valid_from", "date", false, null);
        addField(fields, "valid_till", "date", false, null);
        return schema;
    }

    private ObjectNode buildCreateAttributeDefSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a new attribute definition");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "code", "string", true, null);
        addField(fields, "name", "string", true, null);
        addField(fields, "data_type", "string", true, "NUMERIC, STRING, DATE, BOOLEAN");
        addField(fields, "temporal_type", "string", true, "CONSTANT, INSTANT, PERIOD");
        addField(fields, "applicable_to", "array", true, "array of entity type strings");
        return schema;
    }

    private ObjectNode buildBindEntityCategorySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose binding an entity to a category");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "entity_type", "string", true, null);
        addField(fields, "entity_id", "integer", false, null);
        addField(fields, "entity_ref", "string", false, null);
        addField(fields, "category_name", "string", true, null);
        addField(fields, "category_id", "integer", false, "ID from the provided category tree");
        return schema;
    }

    private ObjectNode buildCreateCategorySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose creating a new category in the hierarchy");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "name", "string", true, null);
        addField(fields, "parent_category_name", "string", false, null);
        addField(fields, "parent_category_id", "integer", false, null);
        return schema;
    }

    private ObjectNode buildBindExternalEntitySchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("description", "Propose binding a raw external entity to a master entity");
        ObjectNode fields = schema.putObject("payload_fields");
        addField(fields, "data_source", "string", true, null);
        addField(fields, "external_id", "integer", true, null);
        addField(fields, "master_entity_type", "string", true, null);
        addField(fields, "master_entity_id", "integer", false, null);
        addField(fields, "master_entity_ref", "string", false, null);
        addField(fields, "master_entity_name", "string", true, null);
        return schema;
    }

    private void addField(ObjectNode fields, String name, String type, boolean required, String description) {
        ObjectNode field = fields.putObject(name);
        field.put("type", type);
        field.put("required", required);
        if (description != null) {
            field.put("description", description);
        }
    }
}
