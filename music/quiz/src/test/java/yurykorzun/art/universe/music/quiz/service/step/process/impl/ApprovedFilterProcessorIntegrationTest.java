package yurykorzun.art.universe.music.quiz.service.step.process.impl;

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

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    ApprovedFilterProcessor.class
})
class ApprovedFilterProcessorIntegrationTest extends JpaOnlyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ApprovedFilterProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_approved";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_approved";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_approved (
                id BIGINT,
                primary_artist_id BIGINT
            )
        """).executeUpdate();
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        // Clean up test data from existing tables
        entityManager.createNativeQuery("TRUNCATE TABLE mu_view.v_track").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.track").executeUpdate();
        
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_approved");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_approved");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldFilterApprovedTracks_whenNormalInput() {
        // given - setup test data
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.track (master_id) VALUES (1), (3)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_approved (id, primary_artist_id) VALUES 
            (1, 101), (2, 102), (3, 103)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(2L, count); // Only tracks 1 and 3 are approved

        // Verify indexes were created
        Boolean hasTrackIndex = (Boolean) entityManager.createNativeQuery("""
            SELECT EXISTS (
                SELECT 1 FROM pg_indexes 
                WHERE schemaname = 'mu_quiz_stg' 
                AND tablename = 'test_output_approved' 
                AND indexname LIKE '%track_id%'
            )
        """).getSingleResult();
        assertTrue(hasTrackIndex);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenNoApprovedTracks() {
        // given - setup test data without approved tracks
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_approved (id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
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
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
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
    void processStep_shouldCreateIndexes_whenTableCreated() {
        // given
        entityManager.createNativeQuery("""
            INSERT INTO mu_view.v_track (id, primary_artist_id) VALUES (1, 101)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.track (master_id) VALUES (1)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_approved (id, primary_artist_id) VALUES (1, 101)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then - verify both indexes exist
        Boolean hasTrackIndex = (Boolean) entityManager.createNativeQuery("""
            SELECT EXISTS (
                SELECT 1 FROM pg_indexes 
                WHERE schemaname = 'mu_quiz_stg' 
                AND tablename = 'test_output_approved' 
                AND indexname LIKE '%track_id%'
            )
        """).getSingleResult();
        assertTrue(hasTrackIndex);

        Boolean hasArtistIndex = (Boolean) entityManager.createNativeQuery("""
            SELECT EXISTS (
                SELECT 1 FROM pg_indexes 
                WHERE schemaname = 'mu_quiz_stg' 
                AND tablename = 'test_output_approved' 
                AND indexname LIKE '%primary_artist_id%'
            )
        """).getSingleResult();
        assertTrue(hasArtistIndex);
    }
}
