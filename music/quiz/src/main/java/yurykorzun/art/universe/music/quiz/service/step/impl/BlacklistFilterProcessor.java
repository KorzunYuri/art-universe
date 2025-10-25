package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.config.BlacklistFilterStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

import java.util.List;

@Component
public class BlacklistFilterProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    public BlacklistFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.BLACKLIST_FILTER, stepRunRepository, stepRepository);
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getStepSuffix() {
        return "blacklist";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            BlacklistFilterStepConfig config = objectMapper.readValue(step.getCfgData(), BlacklistFilterStepConfig.class);
            List<Long> categoryIds = config.getCategoryIds();
            
            // Create auxiliary blacklist table
            String blacklistTable = generateAuxiliaryTableName(step, stepRun, "blacklist");
            
            // Drop table if exists for idempotency
            DatabaseUtils.dropTable(entityManager, blacklistTable);
            
            // Create and populate blacklist table
            entityManager.createNativeQuery(
                "CREATE TABLE " + blacklistTable + " (category_id BIGINT)")
                .executeUpdate();
            
            if (categoryIds != null && !categoryIds.isEmpty()) {
                StringBuilder values = new StringBuilder();
                for (int i = 0; i < categoryIds.size(); i++) {
                    if (i > 0) values.append(",");
                    values.append("(").append(categoryIds.get(i).longValue()).append(")");
                }
                entityManager.createNativeQuery(
                    "INSERT INTO " + blacklistTable + " VALUES " + values)
                    .executeUpdate();
            }
            
            entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_categories_blacklist_filter(:inputTable, :outputTable, :blacklistTable)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("blacklistTable", blacklistTable)
                .executeUpdate();
                
            return "{}"; // Empty stats, will be calculated separately
        } catch (Exception e) {
            throw new RuntimeException("Failed to process blacklist filter step", e);
        }
    }
}
