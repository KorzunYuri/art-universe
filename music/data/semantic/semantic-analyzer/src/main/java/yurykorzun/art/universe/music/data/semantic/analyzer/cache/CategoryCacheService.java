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
public class CategoryCacheService {

    private static final Logger log = LoggerFactory.getLogger(CategoryCacheService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicReference<String> cachedCategoryTree = new AtomicReference<>("[]");

    public CategoryCacheService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCategoryTreeJson() {
        return cachedCategoryTree.get();
    }

    @Scheduled(fixedDelayString = "${semantic.cache.category-refresh-ms:300000}")
    public void refreshCategoryCache() {
        try {
            ArrayNode categories = objectMapper.createArrayNode();

            jdbcTemplate.query(
                "SELECT c.id, c.name, cc.source_category_id as parent_id FROM mu.category c LEFT JOIN mu.category_category cc ON c.id = cc.target_category_id",
                rs -> {
                    ObjectNode cat = objectMapper.createObjectNode();
                    cat.put("id", rs.getLong("id"));
                    cat.put("name", rs.getString("name"));
                    long parentId = rs.getLong("parent_id");
                    if (!rs.wasNull()) {
                        cat.put("parent_id", parentId);
                    }
                    categories.add(cat);
                }
            );

            cachedCategoryTree.set(objectMapper.writeValueAsString(categories));
            log.debug("Category cache refreshed: {} categories", categories.size());
        } catch (Exception e) {
            log.warn("Failed to refresh category cache", e);
        }
    }
}
