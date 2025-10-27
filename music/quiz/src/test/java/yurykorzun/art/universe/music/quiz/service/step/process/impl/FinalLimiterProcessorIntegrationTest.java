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

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    ObjectMapper.class,
    FinalLimiterProcessor.class
})
class FinalLimiterProcessorIntegrationTest extends JpaOnlyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FinalLimiterProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_limiter";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_limiter";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_limiter (
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
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_limiter");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_limiter");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldLimitToTargetCount_whenMoreTracksAvailable() {
        // given - 5 tracks, limit to 3
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_limiter (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 102, 1.0), (3, 103, 1.0), (4, 104, 1.0), (5, 105, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":3}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(3L, count);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnAll_whenFewerTracksAvailable() {
        // given - 2 tracks, limit to 5
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_limiter (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 102, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":5}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_TABLE)
            .getSingleResult();
        assertEquals(2L, count); // All available tracks
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenZeroTargetCount() {
        // given
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_limiter (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 1.0), (2, 102, 1.0)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":0}")
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
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":5}")
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
    void processStep_shouldUseChanceForSelection_whenChanceColumnExists() {
        // given - tracks with different chance values
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_limiter (track_id, primary_artist_id, chance) VALUES 
            (1, 101, 0.1), (2, 102, 10.0), (3, 103, 0.1)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{\"targetCount\":1}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when - run multiple times to check probability
        int track2Selected = 0;
        for (int i = 0; i < 10; i++) {
            DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_limiter");
            
            processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);
            
            Long selectedTrackId = (Long) entityManager.createNativeQuery("SELECT track_id FROM " + OUTPUT_TABLE)
                .getSingleResult();
            
            if (selectedTrackId.equals(2L)) {
                track2Selected++;
            }
        }

        // then - track 2 (highest chance) should be selected most often
        assertTrue(track2Selected >= 7); // Should be selected in most runs due to high chance
    }
}
