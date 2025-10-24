package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.middle.BlacklistFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class BlacklistFilterProcessor extends BaseGenerationStepProcessor<BlacklistFilterStep> {
    
    private final PipelineRepository pipelineRepository;
    private final EntityManager entityManager;
    
    public BlacklistFilterProcessor(PipelineRepository pipelineRepository, EntityManager entityManager) {
        super(GenerationStepType.BLACKLIST_FILTER);
        this.pipelineRepository = pipelineRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, BlacklistFilterStep step) {
        // Create blacklist configuration table
        createBlacklistTable(gameId, step.getCategoryIds());
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.blacklistFilter(parts[0], parts[1], gameId, generationId, stepOrder,
            "mu_quiz_stg", "game_config_blacklist_" + gameId);
    }
    
    private void createBlacklistTable(Long gameId, java.util.List<Long> categoryIds) {
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
