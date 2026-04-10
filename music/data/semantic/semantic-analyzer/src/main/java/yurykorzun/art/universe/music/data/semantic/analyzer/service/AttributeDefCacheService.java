package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.MasterAttributeDefReadRepository;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.MasterAttributeDefReadRepository.AttributeDefRow;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AttributeDefCacheService {

    private static final Logger log = LoggerFactory.getLogger(AttributeDefCacheService.class);

    private final MasterAttributeDefReadRepository attributeDefRepository;
    private final ObjectMapper objectMapper;
    private final AtomicReference<String> cachedAttributeDefs = new AtomicReference<>("[]");

    public AttributeDefCacheService(MasterAttributeDefReadRepository attributeDefRepository, ObjectMapper objectMapper) {
        this.attributeDefRepository = attributeDefRepository;
        this.objectMapper = objectMapper;
    }

    public String getAttributeDefsJson() {
        return cachedAttributeDefs.get();
    }

    @Scheduled(fixedDelayString = "${semantic.cache.attribute-def-refresh-ms:300000}")
    public void refreshAttributeDefCache() {
        try {
            List<AttributeDefRow> rows = attributeDefRepository.findSemanticAttributeDefs();
            ArrayNode defs = objectMapper.createArrayNode();
            for (AttributeDefRow row : rows) {
                ObjectNode def = objectMapper.createObjectNode();
                def.put("code", row.code());
                def.put("name", row.name());
                if (row.description() != null) {
                    def.put("description", row.description());
                }
                def.put("data_type", dataTypeLabel(row.dataTypeCode()));
                def.put("temporal_type", temporalTypeLabel(row.temporalTypeCode()));
                def.put("multi_value", row.multiValue());
                ArrayNode targets = def.putArray("applicable_to");
                for (int code : row.targetTypeCodes()) {
                    targets.add(targetTypeLabel(code));
                }
                defs.add(def);
            }
            cachedAttributeDefs.set(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs));
            log.debug("Attribute def cache refreshed: {} semantic attributes", defs.size());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize attribute def cache", e);
        } catch (Exception e) {
            log.warn("Failed to refresh attribute def cache", e);
        }
    }

    private String dataTypeLabel(int code) {
        return switch (code) {
            case 1 -> "NUMERIC";
            case 2 -> "STRING";
            case 3 -> "DATE";
            case 4 -> "BOOLEAN";
            default -> "UNKNOWN(" + code + ")";
        };
    }

    private String temporalTypeLabel(int code) {
        return switch (code) {
            case 1 -> "CONSTANT";
            case 2 -> "INSTANT";
            case 3 -> "PERIOD";
            default -> "UNKNOWN(" + code + ")";
        };
    }

    /**
     * Resolves an attribute target type code to its display label.
     * Entity target types come from {@link MasterEntityType}; relation-pair target types
     * (codes 10-18, defined in AttributeTargetType in music-master-rest-api) are not
     * currently exported to this module and are rendered as {@code UNKNOWN(code)} if
     * seen. Semantic attribute definitions should target entities, not relation pairs.
     */
    private String targetTypeLabel(int code) {
        for (MasterEntityType type : MasterEntityType.values()) {
            if (type.getCode() == code) {
                return type.getName().toUpperCase();
            }
        }
        return "UNKNOWN(" + code + ")";
    }
}
