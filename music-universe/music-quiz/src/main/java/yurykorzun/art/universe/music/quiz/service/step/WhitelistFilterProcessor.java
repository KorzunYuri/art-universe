package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.List;
import java.util.Map;

@Component
public class WhitelistFilterProcessor extends BaseGenerationStepProcessor {
    
    private final PipelineRepository pipelineRepository;
    private final EntityManager entityManager;
    
    public WhitelistFilterProcessor(PipelineRepository pipelineRepository, EntityManager entityManager) {
        super(GenerationStepType.WHITELIST_FILTER);
        this.pipelineRepository = pipelineRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    protected void validateParams(GenerationStep step) {
        if (step.getParams() == null || !step.getParams().containsKey("categories")) {
            throw new IllegalArgumentException("Whitelist step requires 'categories' parameter");
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categories = (List<Map<String, Object>>) step.getParams().get("categories");
        
        for (Map<String, Object> category : categories) {
            if (!category.containsKey("id") || !category.containsKey("weight")) {
                throw new IllegalArgumentException("Each category must have 'id' and 'weight' fields");
            }
        }
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step) {
        // Create whitelist configuration table
        createWhitelistTable(gameId, step.getParams());
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.whitelistFilter(parts[0], parts[1], gameId, generationId, stepId, 
            "mu_quiz_stg", "game_config_whitelist_" + gameId);
    }
    
    @SuppressWarnings("unchecked")
    private void createWhitelistTable(Long gameId, Map<String, Object> params) {
        List<Map<String, Object>> categories = (List<Map<String, Object>>) params.get("categories");
        String tableName = String.format("game_config_whitelist_%d", gameId);
        
        entityManager.createNativeQuery(String.format("DROP TABLE IF EXISTS mu_quiz_stg.%s", tableName)).executeUpdate();
        entityManager.createNativeQuery(
            String.format("CREATE TABLE mu_quiz_stg.%s (category_id BIGINT, weight DECIMAL)", tableName)
        ).executeUpdate();

        String insertQuery = String.format("INSERT INTO mu_quiz_stg.%s (category_id, weight) VALUES (?, ?)", tableName);
        Query query = entityManager.createNativeQuery(insertQuery);

        for (Map<String, Object> category : categories) {
            Long id = ((Number) category.get("id")).longValue();
            Double weight = ((Number) category.get("weight")).doubleValue();
            query.setParameter(1, id)
                 .setParameter(2, weight)
                 .executeUpdate();
        }
    }
}
