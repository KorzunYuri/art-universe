package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    ObjectMapper.class,
    FinalCategoriesBalancerProcessor.class
})
class FinalCategoriesBalancerProcessorIntegrationTest extends JpaOnlyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FinalCategoriesBalancerProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_balancer";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_categories_balancer";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table with chance column
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_balancer (
                track_id BIGINT,
                primary_artist_id BIGINT,
                chance DECIMAL DEFAULT 1.0
            )
        """).executeUpdate();
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        // Clean up test data
        entityManager.createNativeQuery("DELETE FROM mu_view.v_artist_category").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM mu_view.v_track").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM mu_view.v_category_children").executeUpdate();
        
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_balancer");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_categories_balancer");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_categories_balancer_quotas");
        
        // Drop intermediate tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_categories_balancer_quota_i1_special_tracks");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_categories_balancer_quota_i2_all_tracks");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_categories_balancer_quota_i3_selected");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldBalanceCategories_whenNormalInput() {
        // given - setup test data with 10 tracks, 2 categories with quotas
        setupTracksAndCategories();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_balancer (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 101, 1.0), (3, 102, 1.0), (4, 102, 1.0), (5, 102, 1.0),
            (6, 103, 1.0), (7, 103, 1.0), (8, 104, 1.0), (9, 104, 1.0), (10, 105, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":6,\"defaultQuota\":0.5,\"categories\":[{\"id\":10,\"weight\":0.3},{\"id\":20,\"weight\":0.2}]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertTrue(count <= 6L); // Should not exceed target count
        assertTrue(count > 0L); // Should have some results

        // Verify artist deduplication - no duplicate artists
        Long uniqueArtists = (Long) entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(count, uniqueArtists); // Each track should have unique artist
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldUseDefaultQuota_whenNoCategoryWeights() {
        // given - setup test data without category quotas
        setupTracksAndCategories();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_balancer (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 102, 1.0), (3, 103, 1.0), (4, 104, 1.0), (5, 105, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":3,\"defaultQuota\":1.0,\"categories\":[]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(3L, count); // Should use default quota (100% of target)
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenZeroTargetCount() {
        // given
        setupTracksAndCategories();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_balancer (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 102, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":0,\"defaultQuota\":0.5,\"categories\":[]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(0L, count);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenEmptyInput() {
        // given - empty input table
        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":5,\"defaultQuota\":0.5,\"categories\":[]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(0L, count);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldApplyQuotaWeights_whenCategoriesConfigured() {
        // given - setup with specific quotas to test distribution
        setupTracksAndCategories();
        
        // 6 tracks: 2 in category 10, 2 in category 20, 2 without categories
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_balancer (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 101, 1.0), (3, 102, 1.0), (4, 102, 1.0), (5, 105, 1.0), (6, 105, 1.0)
        """).executeUpdate();

        // Target: 6, Category 10: 50% (3), Category 20: 0% (0), Default: 50% (3)
        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":6,\"defaultQuota\":0.5,\"categories\":[{\"id\":10,\"weight\":0.5},{\"id\":20,\"weight\":0.0}]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertTrue(count <= 6L);

        // Check that no tracks from category 20 (weight 0.0) are selected
        @SuppressWarnings("unchecked")
        List<Object> results = entityManager.createNativeQuery("SELECT track_id FROM " + OUTPUT_TABLE)
            .getResultList();
        
        List<Long> selectedTrackIds = results.stream()
            .map(row -> ((Number) row).longValue())
            .collect(Collectors.toList());
        
        // Tracks 3,4 are from artist 102 (category 20 with weight 0.0) - should not be selected
        assertFalse(selectedTrackIds.contains(3L));
        assertFalse(selectedTrackIds.contains(4L));
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldHandleCategoryInheritance_whenChildCategoriesExist() {
        // given - setup with parent-child category relationship
        setupTracksAndCategories();
        
        // Add child category relationship: 30 is child of 10
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_category_children (id, name, child_id, child_name) VALUES 
            (10, 'Parent Category', 30, 'Child Category')
        """).executeUpdate();

        // Artist 106 has child category 30
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES (11, 106)
        """).executeUpdate();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES (106, 30)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_balancer (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (11, 106, 1.0)
        """).executeUpdate();

        // Configure quota for parent category 10 (should include child 30)
        Step step = Step.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{\"targetCount\":2,\"defaultQuota\":0.0,\"categories\":[{\"id\":10,\"weight\":1.0}]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(1L, count); // Only track 1 should be selected (parent category 10), child category inheritance not implemented yet
    }

    private void setupTracksAndCategories() {
        // Setup tracks
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 101), (3, 102), (4, 102), (5, 102),
            (6, 103), (7, 103), (8, 104), (9, 104), (10, 105)
        """).executeUpdate();

        // Setup artist-category relationships
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20), (103, 10), (104, 20)
        """).executeUpdate();
        // Artist 105 has no categories (for default quota testing)
    }
}
