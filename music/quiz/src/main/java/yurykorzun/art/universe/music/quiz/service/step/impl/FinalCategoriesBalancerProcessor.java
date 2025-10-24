package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
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
    protected String getStepSuffix() {
        return "balancer";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            FinalCategoriesBalancerConfig config = objectMapper.readValue(step.getCfgData(), FinalCategoriesBalancerConfig.class);
            Integer targetCount = config.getTargetCount();
            Double defaultQuota = config.getDefaultQuota();
            List<CategoryWeight> categories = config.getCategories();

            // Create temporary quota table
            String quotaTable = "mu_quiz_stg.temp_quota_" + stepRun.getId();

            // Create and populate quota table
            entityManager.createNativeQuery(
                "CREATE TEMP TABLE " + quotaTable + " (category_id BIGINT, weight DOUBLE PRECISION)")
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

            return (String) entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_final_categories_balancer(:inputTable, :outputTable, :quotaTable, :targetCount, :defaultQuota)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("quotaTable", quotaTable)
                .setParameter("targetCount", targetCount)
                .setParameter("defaultQuota", defaultQuota)
                .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process categories balancer step", e);
        }
    }
}
