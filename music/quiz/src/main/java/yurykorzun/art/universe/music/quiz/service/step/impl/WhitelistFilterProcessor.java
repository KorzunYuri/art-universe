package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.CategoryWeight;
import yurykorzun.art.universe.music.quiz.dto.step.config.WhitelistFilterStepConfig;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BasicStepStats;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.dto.step.stats.WhitelistFilterStats;
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
public class WhitelistFilterProcessor extends BaseGenerationStepProcessor {

    public WhitelistFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.WHITELIST_FILTER, stepRunRepository, stepRepository, objectMapper);
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }
    
    private WhitelistFilterStepConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, WhitelistFilterStepConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for whitelist filter step", e);
        }
    }

    @Override
    protected String getStepSuffix() {
        return "whitelist";
    }
    
    @Override
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            WhitelistFilterStepConfig config = parseConfig(step.getCfgData());
            List<CategoryWeight> weights = config.categories();
            
            // Create auxiliary whitelist table
            String whitelistTable = generateAuxiliaryTableName(step, stepRun, "weights");
            
            // Drop table if exists for idempotency
            DatabaseUtils.dropTable(entityManager, whitelistTable);
            
            // Create and populate whitelist table
            entityManager.createNativeQuery(
                "CREATE TABLE " + whitelistTable + " (category_id BIGINT, weight DOUBLE PRECISION)")
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
            
            entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_categories_whitelist_filter(:inputTable, :outputTable, :whitelistTable)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("whitelistTable", whitelistTable)
                .executeUpdate();
                
            return StepRunResult.builder()
                .outputTableName(outputTableName)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process whitelist filter step", e);
        }
    }

    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        WhitelistFilterStats stats = new WhitelistFilterStats();
        
        // Copy basic stats from parent
        StepRunStats basicStats = super.getResultStats(stepRun);
        BasicStepStats.copyBasicStats(basicStats, stats);
        
        String inputTableName = stepRun.getInputTableName();
        String outputTableName = stepRun.getResultTableName();
        
        // Calculate output records and artists by category
        try {
            WhitelistFilterStepConfig config = parseConfig(stepRun.getStepCfgData());
            Map<Long, Long> recordsByCategory = new HashMap<>();
            Map<Long, Long> artistsByCategory = new HashMap<>();
            
            if (config.categories() != null && DatabaseUtils.tableExists(entityManager, outputTableName)) {
                for (CategoryWeight categoryWeight : config.categories()) {
                    Long categoryId = categoryWeight.id();
                    
                    // Count tracks in this category
                    @SuppressWarnings("unchecked")
                    List<Object[]> trackResult = entityManager.createNativeQuery("""
                        SELECT COUNT(*)
                        FROM %s o
                        JOIN mu_view.v_artist_category ac ON o.primary_artist_id = ac.artist_id
                        WHERE ac.category_id = :categoryId
                    """.formatted(outputTableName))
                        .setParameter("categoryId", categoryId)
                        .getResultList();
                    
                    Long trackCount = ((Number) trackResult.get(0)[0]).longValue();
                    recordsByCategory.put(categoryId, trackCount);
                    
                    // Count artists in this category
                    @SuppressWarnings("unchecked")
                    List<Object[]> artistResult = entityManager.createNativeQuery("""
                        SELECT COUNT(DISTINCT o.primary_artist_id)
                        FROM %s o
                        JOIN mu_view.v_artist_category ac ON o.primary_artist_id = ac.artist_id
                        WHERE ac.category_id = :categoryId
                    """.formatted(outputTableName))
                        .setParameter("categoryId", categoryId)
                        .getResultList();
                    
                    Long artistCount = ((Number) artistResult.get(0)[0]).longValue();
                    artistsByCategory.put(categoryId, artistCount);
                }
            }
            
            stats.setOutputRecordsByCategory(recordsByCategory);
            stats.setOutputArtistsByCategory(artistsByCategory);
        } catch (Exception e) {
            stats.setOutputRecordsByCategory(new HashMap<>());
            stats.setOutputArtistsByCategory(new HashMap<>());
        }
        
        return stats;
    }
}
