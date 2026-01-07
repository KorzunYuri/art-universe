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
import yurykorzun.art.universe.music.quiz.common.archetypes.BaseQuizJpaTest;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    ArtistRecencyPenaltyProcessor.class
})
class ArtistRecencyPenaltyProcessorIntegrationTest extends BaseQuizJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ArtistRecencyPenaltyProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_artist_recency";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_artist_recency";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_artist_recency (
                track_id BIGINT,
                primary_artist_id BIGINT
            )
        """).executeUpdate();

        // Create test game and generation for FK constraints
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.game (id) VALUES (1)
        """).executeUpdate();
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        // Clean up test data in correct order (FK constraints)
        entityManager.createNativeQuery("DELETE FROM mu_quiz.generation_track").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM mu_quiz.generation").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM mu_quiz.game").executeUpdate();
        
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_artist_recency");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_artist_recency");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldApplyPenalty_whenRecentArtists() {
        // given - artist used 5 days ago (recent)
        Instant fiveDaysAgo = Instant.now().minus(5, ChronoUnit.DAYS);
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.generation (id, game_id, target_count, status, approved, created_at) VALUES (1, 1, 10, 2, true, :recentDate)
        """)
        .setParameter("recentDate", fiveDaysAgo)
        .executeUpdate();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_artist_recency (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 102)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.generation_track (track_id, primary_artist_id, generation_id, created_at, track_name, artist_name, order_index) VALUES 
            (99, 101, 1, :recentDate, 'Test Track', 'Test Artist', 1)
        """)
        .setParameter("recentDate", fiveDaysAgo)
        .executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_RECENCY_PENALTY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        // Verify penalty applied to recent artist
        Number recentArtistChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 101")
            .getSingleResult();
        assertEquals(0.2, recentArtistChanceNum.doubleValue(), 0.001); // Recent penalty

        // Verify no penalty for never-used artist
        Number neverUsedChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 102")
            .getSingleResult();
        assertEquals(1.0, neverUsedChanceNum.doubleValue(), 0.001); // No penalty
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldNotApplyPenalty_whenOldArtists() {
        // given - artist used 13 months ago (old)
        Instant thirteenMonthsAgo = Instant.now().minus(400, ChronoUnit.DAYS);
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.generation (id, game_id, target_count, status, approved, created_at) VALUES (1, 1, 10, 2, true, :oldDate)
        """)
        .setParameter("oldDate", thirteenMonthsAgo)
        .executeUpdate();
        
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_artist_recency (track_id, primary_artist_id) VALUES 
            (1, 101)
        """).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz.generation_track (track_id, primary_artist_id, generation_id, created_at, track_name, artist_name, order_index) VALUES 
            (99, 101, 1, :oldDate, 'Test Track', 'Test Artist', 1)
        """)
        .setParameter("oldDate", thirteenMonthsAgo)
        .executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_RECENCY_PENALTY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        Number chanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 101")
            .getSingleResult();
        assertEquals(1.0, chanceNum.doubleValue(), 0.001); // No penalty for old artist
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldNotApplyPenalty_whenNeverUsedArtists() {
        // given - artist never used
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_artist_recency (track_id, primary_artist_id) VALUES 
            (1, 101)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_RECENCY_PENALTY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        Number chanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 101")
            .getSingleResult();
        assertEquals(1.0, chanceNum.doubleValue(), 0.001); // No penalty for never used
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenEmptyInput() {
        // given - empty input table
        Step step = Step.builder()
            .type(StepType.ARTIST_RECENCY_PENALTY)
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
}
