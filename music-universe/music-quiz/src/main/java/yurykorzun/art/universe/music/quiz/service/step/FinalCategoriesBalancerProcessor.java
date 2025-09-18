package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.FinalCategoriesBalancerStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class FinalCategoriesBalancerProcessor extends BaseGenerationStepProcessor<FinalCategoriesBalancerStep> {
    
    private final PipelineRepository pipelineRepository;
    private final EntityManager entityManager;
    
    public FinalCategoriesBalancerProcessor(PipelineRepository pipelineRepository, EntityManager entityManager) {
        super(GenerationStepType.FINAL_CATEGORIES_BALANCER);
        this.pipelineRepository = pipelineRepository;
        this.entityManager = entityManager;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, FinalCategoriesBalancerStep step) {
        // Create quota configuration table
        createQuotaTable(gameId, step.getCategories());
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.finalCategoriesBalancer(parts[0], parts[1], gameId, generationId, stepOrder,
            "mu_quiz_stg", "game_config_quota_" + gameId, step.getTargetCount());
    }
    
    private void createQuotaTable(Long gameId, java.util.List<FinalCategoriesBalancerStep.CategoryWeight> categories) {
        String tableName = String.format("game_config_quota_%d", gameId);
        
        entityManager.createNativeQuery(String.format("DROP TABLE IF EXISTS mu_quiz_stg.%s", tableName)).executeUpdate();
        entityManager.createNativeQuery(
            String.format("CREATE TABLE mu_quiz_stg.%s (category_id BIGINT, weight DECIMAL)", tableName)
        ).executeUpdate();

        for (FinalCategoriesBalancerStep.CategoryWeight category : categories) {
            entityManager.createNativeQuery(String.format("INSERT INTO mu_quiz_stg.%s VALUES (?, ?)", tableName))
                    .setParameter(1, category.id())
                    .setParameter(2, category.weight())
                    .executeUpdate();
        }
    }
}
