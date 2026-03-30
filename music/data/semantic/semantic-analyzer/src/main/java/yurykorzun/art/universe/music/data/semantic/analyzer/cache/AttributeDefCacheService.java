package yurykorzun.art.universe.music.data.semantic.analyzer.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class AttributeDefCacheService {

    private static final Logger log = LoggerFactory.getLogger(AttributeDefCacheService.class);

    private static final int COMPUTATION_TYPE_SEMANTIC = 4;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> cachedAttributeDefs = new AtomicReference<>("[]");

    public AttributeDefCacheService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getAttributeDefsJson() {
        return cachedAttributeDefs.get();
    }

    @Scheduled(fixedDelayString = "${semantic.cache.attribute-def-refresh-ms:300000}")
    public void refreshAttributeDefCache() {
        try {
            ArrayNode defs = objectMapper.createArrayNode();

            jdbcTemplate.query(
                """
                SELECT d.id, d.code, d.name, d.description, d.data_type, d.temporal_type,
                       d.is_multi_value,
                       array_agg(a.target_type ORDER BY a.target_type) AS target_types
                FROM mu.attribute_def d
                JOIN mu.attribute_def_applicability a ON d.id = a.attribute_def_id
                WHERE d.computation_type = ?
                GROUP BY d.id, d.code, d.name, d.description, d.data_type, d.temporal_type, d.is_multi_value
                ORDER BY d.id
                """,
                rs -> {
                    ObjectNode def = objectMapper.createObjectNode();
                    def.put("code", rs.getString("code"));
                    def.put("name", rs.getString("name"));
                    String desc = rs.getString("description");
                    if (desc != null) {
                        def.put("description", desc);
                    }
                    def.put("data_type", mapDataType(rs.getInt("data_type")));
                    def.put("temporal_type", mapTemporalType(rs.getInt("temporal_type")));
                    def.put("multi_value", rs.getBoolean("is_multi_value"));

                    ArrayNode targets = def.putArray("applicable_to");
                    java.sql.Array arr = rs.getArray("target_types");
                    if (arr != null) {
                        Integer[] codes = (Integer[]) arr.getArray();
                        for (int code : codes) {
                            targets.add(mapTargetType(code));
                        }
                    }
                    defs.add(def);
                },
                COMPUTATION_TYPE_SEMANTIC
            );

            cachedAttributeDefs.set(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(defs));
            log.debug("Attribute def cache refreshed: {} semantic attributes", defs.size());
        } catch (Exception e) {
            log.warn("Failed to refresh attribute def cache", e);
        }
    }

    private String mapDataType(int code) {
        return switch (code) {
            case 1 -> "NUMERIC";
            case 2 -> "STRING";
            case 3 -> "DATE";
            case 4 -> "BOOLEAN";
            default -> "UNKNOWN";
        };
    }

    private String mapTemporalType(int code) {
        return switch (code) {
            case 1 -> "CONSTANT";
            case 2 -> "INSTANT";
            case 3 -> "PERIOD";
            default -> "UNKNOWN";
        };
    }

    private String mapTargetType(int code) {
        return switch (code) {
            case 1 -> "ARTIST";
            case 2 -> "ALBUM";
            case 3 -> "TRACK";
            case 4 -> "CATEGORY";
            case 101 -> "PERSON";
            default -> "UNKNOWN(" + code + ")";
        };
    }
}
