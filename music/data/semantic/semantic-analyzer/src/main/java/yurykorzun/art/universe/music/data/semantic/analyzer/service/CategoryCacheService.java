package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.MasterCategoryReadRepository;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.MasterCategoryReadRepository.CategoryRow;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CategoryCacheService {

    private static final Logger log = LoggerFactory.getLogger(CategoryCacheService.class);

    private final MasterCategoryReadRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final AtomicReference<String> cachedCategoryTree = new AtomicReference<>("[]");

    public CategoryCacheService(MasterCategoryReadRepository categoryRepository, ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    public String getCategoryTreeJson() {
        return cachedCategoryTree.get();
    }

    @Scheduled(fixedDelayString = "${semantic.cache.category-refresh-ms:300000}")
    public void refreshCategoryCache() {
        try {
            List<CategoryRow> rows = categoryRepository.findAll();
            ArrayNode categories = objectMapper.createArrayNode();
            for (CategoryRow row : rows) {
                ObjectNode cat = objectMapper.createObjectNode();
                cat.put("id", row.id());
                cat.put("name", row.name());
                if (row.parentId() != null) {
                    cat.put("parent_id", row.parentId());
                }
                categories.add(cat);
            }
            cachedCategoryTree.set(objectMapper.writeValueAsString(categories));
            log.debug("Category cache refreshed: {} categories", categories.size());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize category cache", e);
        } catch (Exception e) {
            log.warn("Failed to refresh category cache", e);
        }
    }
}
