package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.BlacklistFilterStepConfig;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BlacklistFilterStats;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BlacklistFilterProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    public BlacklistFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.BLACKLIST_FILTER, stepRunRepository, stepRepository, objectMapper);
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }
    
    private BlacklistFilterStepConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, BlacklistFilterStepConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for blacklist filter step", e);
        }
    }

    @Override
    protected String getStepSuffix() {
        return "blacklist";
    }

    @Override
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            BlacklistFilterStepConfig config = parseConfig(step.getCfgData());
            List<Long> categoryIds = config.categoryIds();
            
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
                
            return StepRunResult.builder()
                .outputTableName(outputTableName)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process blacklist filter step", e);
        }
    }

    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        BlacklistFilterStats stats = new BlacklistFilterStats();
        
        // Copy basic stats from parent
        StepRunStats basicStats = super.getResultStats(stepRun);
        stats.setInputRecords(basicStats.getInputRecords());
        stats.setFilteredRecords(basicStats.getFilteredRecords());
        stats.setOutputRecords(basicStats.getOutputRecords());
        stats.setInputArtists(basicStats.getInputArtists());
        stats.setFilteredArtists(basicStats.getFilteredArtists());
        stats.setOutputArtists(basicStats.getOutputArtists());
        stats.setExecutionTimeMs(basicStats.getExecutionTimeMs());
        
        String inputTableName = stepRun.getInputTableName();
        String outputTableName = stepRun.getResultTableName();
        
        // Calculate filtered records by category
        try {
            BlacklistFilterStepConfig config = parseConfig(stepRun.getStepCfgData());
            Map<Long, Long> filteredByCategory = new HashMap<>();
            
            if (config.categoryIds() != null && inputTableName != null && 
                DatabaseUtils.tableExists(entityManager, inputTableName) && 
                DatabaseUtils.tableExists(entityManager, outputTableName)) {
                
                for (Long categoryId : config.categoryIds()) {
                    @SuppressWarnings("unchecked")
                    List<Object[]> result = entityManager.createNativeQuery("""
                        SELECT COUNT(*)
                        FROM %s i
                        JOIN mu_view.v_artist_category ac ON i.primary_artist_id = ac.artist_id
                        WHERE ac.category_id = :categoryId
                        AND NOT EXISTS (
                            SELECT 1 FROM %s o WHERE o.track_id = i.track_id
                        )
                    """.formatted(inputTableName, outputTableName))
                        .setParameter("categoryId", categoryId)
                        .getResultList();
                    
                    Long filteredCount = ((Number) result.get(0)[0]).longValue();
                    filteredByCategory.put(categoryId, filteredCount);
                }
            }
            
            stats.setFilteredRecordsByCategory(filteredByCategory);
        } catch (Exception e) {
            stats.setFilteredRecordsByCategory(new HashMap<>());
        }
        
        return stats;
    }
}
