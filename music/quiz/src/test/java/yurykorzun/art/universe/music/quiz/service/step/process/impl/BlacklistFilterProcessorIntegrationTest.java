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
import yurykorzun.art.universe.music.quiz.common.archetypes.BaseQuizJpaTest;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    ObjectMapper.class,
    BlacklistFilterProcessor.class
})
class BlacklistFilterProcessorIntegrationTest extends BaseQuizJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BlacklistFilterProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_blacklist";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_blacklist_filter";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_blacklist (
                track_id BIGINT,
                primary_artist_id BIGINT
            )
        """).executeUpdate();
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        // Clean up test data from existing tables
        entityManager.createNativeQuery("DELETE FROM mu_view.v_artist_category").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM mu_view.v_track").executeUpdate();
        
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_blacklist");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_blacklist_filter");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_blacklist_filter_blacklist");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldFilterBlacklistedCategories_whenNormalInput() {
        // given - setup test data
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_blacklist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20), (103, 30)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.BLACKLIST_FILTER)
            .cfgData("{\"categoryIds\":[10,20]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(1L, count); // Only track 3 should remain (category 30 not blacklisted)

        // Verify correct track remains
        Long trackId = (Long) entityManager.createNativeQuery("SELECT track_id FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(3L, trackId);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnAll_whenEmptyBlacklist() {
        // given - setup test data with empty blacklist
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_blacklist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.BLACKLIST_FILTER)
            .cfgData("{\"categoryIds\":[]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(2L, count); // All tracks should remain
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenAllBlacklisted() {
        // given - setup test data where all tracks are blacklisted
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_blacklist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 10)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.BLACKLIST_FILTER)
            .cfgData("{\"categoryIds\":[10]}")
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
            .type(StepType.BLACKLIST_FILTER)
            .cfgData("{\"categoryIds\":[10]}")
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
    void processStep_shouldHandleMultipleCategories_whenTrackInMultiple() {
        // given - track in multiple categories, one blacklisted
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_blacklist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (101, 30), (102, 20)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.BLACKLIST_FILTER)
            .cfgData("{\"categoryIds\":[10]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(1L, count); // Only track 2 should remain (track 1 has blacklisted category 10)

        Long trackId = (Long) entityManager.createNativeQuery("SELECT track_id FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(2L, trackId);
    }
}
