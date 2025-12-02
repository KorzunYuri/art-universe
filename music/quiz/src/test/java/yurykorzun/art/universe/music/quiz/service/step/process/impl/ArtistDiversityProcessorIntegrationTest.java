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
    ArtistDiversityProcessor.class
})
class ArtistDiversityProcessorIntegrationTest extends JpaOnlyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ArtistDiversityProcessor processor;

    private static final String INPUT_TABLE = "mu_quiz_stg.test_input_diversity";
    private static final String OUTPUT_TABLE = "mu_quiz_stg.test_output_artist_diversity";

    @BeforeEach
    @Transactional
    @Commit
    void setUp() {
        // Create input table
        entityManager.createNativeQuery("""
            CREATE TABLE mu_quiz_stg.test_input_diversity (
                track_id BIGINT,
                primary_artist_id BIGINT
            )
        """).executeUpdate();
    }

    @AfterEach
    @Transactional
    @Commit
    void tearDown() {
        // Drop created tables
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_input_diversity");
        DatabaseUtils.dropTable(entityManager, "mu_quiz_stg.test_output_artist_diversity");
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldApplyDiversityPenalty_whenManyTracksPerArtist() {
        // given - artist 101 has 5 tracks (many), artist 102 has 1 track (few)
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_diversity (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 101), (3, 101), (4, 101), (5, 101), (6, 102)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_DIVERSITY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        // Verify penalty applied to artist with many tracks (5 tracks, so penalty 1.0/5 = 0.2)
        Number manyTracksChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 101 LIMIT 1")
            .getSingleResult();
        assertEquals(0.2, manyTracksChanceNum.doubleValue(), 0.001); // 1.0 / 5 tracks

        // Verify penalty for artist with few tracks (1 track, so penalty 1.0/1 = 1.0)
        Number fewTracksChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 102")
            .getSingleResult();
        assertEquals(1.0, fewTracksChanceNum.doubleValue(), 0.001); // 1.0 / 1 track
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldNotApplyPenalty_whenFewTracksPerArtist() {
        // given - all artists have <= 3 tracks
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_diversity (track_id, primary_artist_id) VALUES 
            (1, 101), (2, 101), (3, 101), (4, 102), (5, 102), (6, 103)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_DIVERSITY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        // Verify penalty for each artist (all have different track counts)
        @SuppressWarnings("unchecked")
        var results = entityManager.createNativeQuery("SELECT chance FROM " + OUTPUT_TABLE + " ORDER BY primary_artist_id")
            .getResultList();
        
        assertEquals(6, results.size());
        // Artist 101: 3 tracks -> 1.0/3 = 0.333...
        assertEquals(0.333, ((Number) results.get(0)).doubleValue(), 0.001);
        assertEquals(0.333, ((Number) results.get(1)).doubleValue(), 0.001);
        assertEquals(0.333, ((Number) results.get(2)).doubleValue(), 0.001);
        // Artist 102: 2 tracks -> 1.0/2 = 0.5
        assertEquals(0.5, ((Number) results.get(3)).doubleValue(), 0.001);
        assertEquals(0.5, ((Number) results.get(4)).doubleValue(), 0.001);
        // Artist 103: 1 track -> 1.0/1 = 1.0
        assertEquals(1.0, ((Number) results.get(5)).doubleValue(), 0.001);
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldReturnEmpty_whenEmptyInput() {
        // given - empty input table
        Step step = Step.builder()
            .type(StepType.ARTIST_DIVERSITY)
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
    void processStep_shouldCalculateCorrectCounts_whenSameArtistMultipleTracks() {
        // given - artist 101 has exactly 11 tracks (should get 0.3 penalty)
        StringBuilder insertQuery = new StringBuilder("INSERT INTO mu_quiz_stg.test_input_diversity (track_id, primary_artist_id) VALUES ");
        for (int i = 1; i <= 11; i++) {
            if (i > 1) insertQuery.append(", ");
            insertQuery.append("(").append(i).append(", 101)");
        }
        insertQuery.append(", (12, 102)"); // Artist 102 has 1 track
        
        entityManager.createNativeQuery(insertQuery.toString()).executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_DIVERSITY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        // Verify penalty for artist with >10 tracks (11 tracks, so penalty 1.0/11 = 0.091)
        Number heavyPenaltyChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 101 LIMIT 1")
            .getSingleResult();
        assertEquals(0.091, heavyPenaltyChanceNum.doubleValue(), 0.001); // 1.0 / 11 tracks

        // Verify no penalty for artist with 1 track
        Number noPenaltyChanceNum = (Number) entityManager.createNativeQuery(
            "SELECT chance FROM " + OUTPUT_TABLE + " WHERE primary_artist_id = 102")
            .getSingleResult();
        assertEquals(1.0, noPenaltyChanceNum.doubleValue(), 0.001); // 1.0 / 1 track
    }

    @Test
    @Transactional
    @Commit
    void processStep_shouldEnsureChanceColumn_whenInputWithoutChance() {
        // given - input table without chance column (will be added by p_ensure_chance_column)
        entityManager.createNativeQuery("""
            INSERT INTO mu_quiz_stg.test_input_diversity (track_id, primary_artist_id) VALUES 
            (1, 101)
        """).executeUpdate();

        Step step = Step.builder()
            .type(StepType.ARTIST_DIVERSITY)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        // when
        StepRunResult result = processor.processStep(step, INPUT_TABLE, "mu_quiz_stg.test_output", stepRun);

        // then - should work and add chance column
        assertEquals(OUTPUT_TABLE, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_TABLE));

        // Verify chance column exists in output
        Boolean hasChance = (Boolean) entityManager.createNativeQuery("""
            SELECT EXISTS (
                SELECT 1 FROM information_schema.columns 
                WHERE table_schema = 'mu_quiz_stg' 
                AND table_name = 'test_output_artist_diversity' 
                AND column_name = 'chance'
            )
        """).getSingleResult();
        assertTrue(hasChance);
    }
}
