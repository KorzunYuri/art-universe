package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.CategoryWeight;
import yurykorzun.art.universe.music.quiz.dto.step.config.FinalCategoriesBalancerConfig;
import yurykorzun.art.universe.music.quiz.dto.step.stats.FinalCategoriesBalancerStats;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FinalCategoriesBalancerProcessor extends BaseGenerationStepProcessor {

    public FinalCategoriesBalancerProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.FINAL_CATEGORIES_BALANCER, stepRunRepository, stepRepository, objectMapper);
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
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            FinalCategoriesBalancerConfig config = parseConfig(step.getCfgData());
            Integer targetCount = config.targetCount();
            Double defaultQuota = config.defaultQuota();
            List<CategoryWeight> categories = config.categories();

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
                
            return StepRunResult.builder()
                .outputTableName(outputTableName)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to process categories balancer step", e);
        }
    }

    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        FinalCategoriesBalancerStats stats = (FinalCategoriesBalancerStats) super.getResultStats(stepRun);
        
        String inputTableName = stepRun.getInputTableName();
        String outputTableName = stepRun.getResultTableName();
        
        // Calculate output records and artists by category + default quota
        try {
            FinalCategoriesBalancerConfig config = parseConfig(stepRun.getStepCfgData());
            Map<Long, Long> recordsByCategory = new HashMap<>();
            Map<Long, Long> artistsByCategory = new HashMap<>();
            
            Set<Long> configuredCategoryIds = config.categories() != null ? 
                config.categories().stream().map(CategoryWeight::id).collect(Collectors.toSet()) : 
                Set.of();
            
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
            
            // Calculate default quota (tracks/artists not in any configured category)
            Long defaultQuotaRecords = 0L;
            Long defaultQuotaArtists = 0L;
            
            if (DatabaseUtils.tableExists(entityManager, outputTableName)) {
                String categoryFilter = configuredCategoryIds.isEmpty() ? "1=1" : 
                    "ac.category_id NOT IN (" + configuredCategoryIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
                
                @SuppressWarnings("unchecked")
                List<Object[]> defaultTrackResult = entityManager.createNativeQuery("""
                    SELECT COUNT(*)
                    FROM %s o
                    LEFT JOIN mu_view.v_artist_category ac ON o.primary_artist_id = ac.artist_id
                    WHERE ac.category_id IS NULL OR (%s)
                """.formatted(outputTableName, categoryFilter))
                    .getResultList();
                
                defaultQuotaRecords = ((Number) defaultTrackResult.get(0)[0]).longValue();
                
                @SuppressWarnings("unchecked")
                List<Object[]> defaultArtistResult = entityManager.createNativeQuery("""
                    SELECT COUNT(DISTINCT o.primary_artist_id)
                    FROM %s o
                    LEFT JOIN mu_view.v_artist_category ac ON o.primary_artist_id = ac.artist_id
                    WHERE ac.category_id IS NULL OR (%s)
                """.formatted(outputTableName, categoryFilter))
                    .getResultList();
                
                defaultQuotaArtists = ((Number) defaultArtistResult.get(0)[0]).longValue();
            }
            
            stats.setOutputRecordsByCategory(recordsByCategory);
            stats.setOutputArtistsByCategory(artistsByCategory);
            stats.setDefaultQuotaRecords(defaultQuotaRecords);
            stats.setDefaultQuotaArtists(defaultQuotaArtists);
        } catch (Exception e) {
            stats.setOutputRecordsByCategory(new HashMap<>());
            stats.setOutputArtistsByCategory(new HashMap<>());
            stats.setDefaultQuotaRecords(0L);
            stats.setDefaultQuotaArtists(0L);
        }
        
        return stats;
    }
}
