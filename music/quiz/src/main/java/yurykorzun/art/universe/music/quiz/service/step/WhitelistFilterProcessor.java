package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.middle.WhitelistFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class WhitelistFilterProcessor extends BaseGenerationStepProcessor<WhitelistFilterStep> {
    
    private final PipelineRepository pipelineRepository;
    private final EntityManager entityManager;
    
    public WhitelistFilterProcessor(PipelineRepository pipelineRepository, EntityManager entityManager) {
        super(GenerationStepType.WHITELIST_FILTER);
        this.pipelineRepository = pipelineRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, WhitelistFilterStep step) {
        // Create whitelist configuration table
        createWhitelistTable(gameId, step.getCategories());
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.whitelistFilter(parts[0], parts[1], gameId, generationId, stepOrder, 
            "mu_quiz_stg", "game_config_whitelist_" + gameId);
    }
    
    private void createWhitelistTable(Long gameId, java.util.List<WhitelistFilterStep.CategoryWeight> categories) {
        String tableName = String.format("game_config_whitelist_%d", gameId);
        
        entityManager.createNativeQuery(String.format("DROP TABLE IF EXISTS mu_quiz_stg.%s", tableName)).executeUpdate();
        entityManager.createNativeQuery(
            String.format("CREATE TABLE mu_quiz_stg.%s (category_id BIGINT, weight DECIMAL)", tableName)
        ).executeUpdate();

        String insertQuery = String.format("INSERT INTO mu_quiz_stg.%s (category_id, weight) VALUES (?, ?)", tableName);
        Query query = entityManager.createNativeQuery(insertQuery);

        for (WhitelistFilterStep.CategoryWeight category : categories) {
            query.setParameter(1, category.id())
                 .setParameter(2, category.weight())
                 .executeUpdate();
        }
    }
}
