package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

@Import({
    StepProcessorRegistry.class,
    ObjectMapper.class,
    WhitelistFilterProcessor.class
})
class WhitelistFilterProcessorIntegrationTest extends BaseQuizJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private WhitelistFilterProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_whitelist";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_whitelist_filter";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_whitelist (
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
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_whitelist");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_whitelist_filter");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_whitelist_filter_weights");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldFilterWhitelistedCategories_whenNormalInput() {
        // given - setup test data
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20), (103, 30)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_whitelist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.WHITELIST_FILTER)
            .cfgData("{\"categories\":[{\"id\":10,\"weight\":1.0},{\"id\":20,\"weight\":2.0}]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(2L, count); // Only tracks 1 and 2 should remain (categories 10 and 20 whitelisted)

        // Verify chance values are applied
        @SuppressWarnings("unchecked")
        var results = entityManager.createNativeQuery("SELECT track_id, chance FROM " + OUTPUT_TABLE + " ORDER BY track_id")
            .getResultList();
        
        assertEquals(2, results.size());
        Object[] track1 = (Object[]) results.get(0);
        Object[] track2 = (Object[]) results.get(1);
        
        assertEquals(1L, ((Number) track1[0]).longValue());
        assertEquals(2.0, ((Number) track1[1]).doubleValue(), 0.001); // weight 1.0
        
        assertEquals(2L, ((Number) track2[0]).longValue());
        assertEquals(4.0, ((Number) track2[1]).doubleValue(), 0.001); // compensated weight
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnAll_whenEmptyWhitelist() {
        // given - setup test data with empty whitelist
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_whitelist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.WHITELIST_FILTER)
            .cfgData("{\"categories\":[]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(0L, count); // No tracks when whitelist is empty (no matching categories)
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenNoWhitelistedTracks() {
        // given - setup test data where no tracks match whitelist
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (102, 20)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_whitelist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.WHITELIST_FILTER)
            .cfgData("{\"categories\":[{\"id\":99,\"weight\":1.0}]}")
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
            .type(StepType.WHITELIST_FILTER)
            .cfgData("{\"categories\":[{\"id\":10,\"weight\":1.0}]}")
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
    void processStep_shouldHandleMaxWeight_whenTrackInMultipleCategories() {
        // given - track in multiple whitelisted categories, should get MAX weight
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_artist_category (artist_id, category_id) VALUES 
            (101, 10), (101, 20), (102, 30)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_whitelist (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.WHITELIST_FILTER)
            .cfgData("{\"categories\":[{\"id\":10,\"weight\":1.0},{\"id\":20,\"weight\":3.0}]}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(1L, count); // Only track 1 should remain (has whitelisted categories)

        // Verify MAX weight is used (3.0, not 1.0)
        Double chance = (Double) entityManager.createNativeQuery("SELECT chance FROM " + OUTPUT_TABLE + " WHERE track_id = 1")
            .getSingleResult();
        assertEquals(6.0, chance, 0.001);
    }
}
