package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.config.CategoryWeight;
import yurykorzun.art.universe.music.quiz.dto.step.config.FinalCategoriesBalancerConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

import java.util.List;
import java.util.Map;

@Component
public class FinalCategoriesBalancerProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    public FinalCategoriesBalancerProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.FINAL_CATEGORIES_BALANCER, stepRunRepository, stepRepository);
        this.objectMapper = objectMapper;
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }
    
    private FinalCategoriesBalancerConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, FinalCategoriesBalancerConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for categories balancer step", e);
        }
    }

    @Override
    protected String getStepSuffix() {
        return "balancer";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            FinalCategoriesBalancerConfig config = parseConfig(step.getCfgData());
            Integer targetCount = config.getTargetCount();
            Double defaultQuota = config.getDefaultQuota();
            List<CategoryWeight> categories = config.getCategories();

            // Create auxiliary quota table
            String quotaTable = generateAuxiliaryTableName(step, stepRun, "quotas");

            // Drop table if exists for idempotency
            DatabaseUtils.dropTable(entityManager, quotaTable);

            // Create and populate quota table
            entityManager.createNativeQuery(
                "CREATE TABLE " + quotaTable + " (category_id BIGINT, weight DOUBLE PRECISION)")
                .executeUpdate();

            if (categories != null && !categories.isEmpty()) {
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < categories.size(); i++) {
                    if (i > 0) values.append(",");
                    CategoryWeight categoryWeight = categories.get(i);
                    values.append("(")
                        .append(categoryWeight.id())
                        .append(",").append(categoryWeight.weight())
                        .append(")");
                }
                entityManager.createNativeQuery(
                    "INSERT INTO " + quotaTable + " VALUES " + values)
                    .executeUpdate();
            }

            entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_final_categories_balancer(:inputTable, :outputTable, :quotaTable, :targetCount, :defaultQuota)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("quotaTable", quotaTable)
                .setParameter("targetCount", targetCount)
                .setParameter("defaultQuota", defaultQuota)
                .executeUpdate();
                
            return "{}"; // Empty stats, will be calculated separately
        } catch (Exception e) {
            throw new RuntimeException("Failed to process categories balancer step", e);
        }
    }
}
