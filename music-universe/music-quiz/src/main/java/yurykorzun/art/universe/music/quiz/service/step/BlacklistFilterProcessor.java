package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import java.util.List;
import java.util.Map;

@Component
public class BlacklistFilterProcessor extends BaseGenerationStepProcessor {
    
    private final PipelineRepository pipelineRepository;
    private final EntityManager entityManager;
    
    public BlacklistFilterProcessor(PipelineRepository pipelineRepository, EntityManager entityManager) {
        super(GenerationStepType.BLACKLIST_FILTER);
        this.pipelineRepository = pipelineRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    protected void validateParams(GenerationStep step) {
        if (step.getParams() == null || !step.getParams().containsKey("categoryIds")) {
            throw new IllegalArgumentException("Blacklist step requires 'categoryIds' parameter");
        }
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step) {
        // Create blacklist configuration table
        createBlacklistTable(gameId, step.getParams());
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.blacklistFilter(parts[0], parts[1], gameId, generationId, stepId,
            "mu_quiz_stg", "game_config_blacklist_" + gameId);
    }
    
    @SuppressWarnings("unchecked")
    private void createBlacklistTable(Long gameId, Map<String, Object> params) {
        List<Long> categoryIds = (List<Long>) params.get("categoryIds");
        String tableName = String.format("game_config_blacklist_%d", gameId);
        
        entityManager.createNativeQuery(String.format("DROP TABLE IF EXISTS mu_quiz_stg.%s", tableName)).executeUpdate();
        entityManager.createNativeQuery(
            String.format("CREATE TABLE mu_quiz_stg.%s (category_id BIGINT)", tableName)
        ).executeUpdate();
        
        for (Long categoryId : categoryIds) {
            entityManager.createNativeQuery(String.format("INSERT INTO mu_quiz_stg.%s VALUES (?)", tableName))
                    .setParameter(1, categoryId)
                    .executeUpdate();
        }
    }
}
