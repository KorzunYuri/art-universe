package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.config.CategoryWeight;
import yurykorzun.art.universe.music.quiz.dto.step.config.WhitelistFilterStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

import java.util.List;
import java.util.Map;

@Component
public class WhitelistFilterProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final ObjectMapper objectMapper;

    public WhitelistFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.WHITELIST_FILTER, stepRunRepository, stepRepository);
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getStepSuffix() {
        return "whitelist";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            WhitelistFilterStepConfig config = objectMapper.readValue(step.getCfgData(), WhitelistFilterStepConfig.class);
            List<CategoryWeight> weights = config.getCategories();
            
            // Create temporary whitelist table
            String whitelistTable = "mu_quiz_stg.temp_whitelist_" + stepRun.getId();
            
            // Create and populate whitelist table
            entityManager.createNativeQuery(
                "CREATE TEMP TABLE " + whitelistTable + " (category_id BIGINT, weight DOUBLE PRECISION)")
                .executeUpdate();
            
            if (weights != null && !weights.isEmpty()) {
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < weights.size(); i++) {
                    if (i > 0) values.append(",");
                    CategoryWeight category = weights.get(i);
                    values.append("(").append(category.id()).append(",").append(category.weight()).append(")");
                }
                entityManager.createNativeQuery(
                    "INSERT INTO " + whitelistTable + " VALUES " + values)
                    .executeUpdate();
            }
            
            return (String) entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_categories_whitelist_filter(:inputTable, :outputTable, :whitelistTable)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("whitelistTable", whitelistTable)
                .getSingleResult();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process whitelist filter step", e);
        }
    }
}
