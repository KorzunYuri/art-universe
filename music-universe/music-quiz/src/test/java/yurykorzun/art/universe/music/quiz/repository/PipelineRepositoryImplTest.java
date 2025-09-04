package yurykorzun.art.universe.music.quiz.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Commit;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import yurykorzun.art.universe.music.quiz.repository.impl.PipelineRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@Import(
        PipelineRepositoryImpl.class
)
class PipelineRepositoryImplTest extends JpaOnlyTest {

    @Autowired
    private PipelineRepository pipelineRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    @Commit
    void setUp() {
        // Clear all staging tables
        entityManager.createNativeQuery("""
            DO $$
            DECLARE
                r RECORD;
            BEGIN
                FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'mu_quiz_stg' AND tablename LIKE 'gen_tracks_%')
                LOOP
                    EXECUTE 'DROP TABLE IF EXISTS mu_quiz_stg.' || quote_ident(r.tablename);
                END LOOP;
            END $$;
        """).executeUpdate();

        // Clean up test data
        entityManager.createNativeQuery("TRUNCATE TABLE mu_quiz.track").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_view.v_track").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE mu_view.v_artist").executeUpdate();

        // Insert test data
        entityManager.createNativeQuery("INSERT INTO mu_view.v_artist (id, name) VALUES (1, 'Artist 1'), (2, 'Artist 2')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO mu_view.v_track (id, primary_artist_id, name) VALUES (1, 1, 'Track 1'), (2, 1, 'Track 2'), (3, 2, 'Track 3')").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO mu_quiz.track (master_id) VALUES (1), (2), (3)").executeUpdate();
    }

    @Test
    void approvedFilter_shouldFilterApprovedTracks() {
        // when
        String resultTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable)
            .getSingleResult();
        assertEquals(3, count); // All tracks are approved
    }

    @Test
    void recencyPenalty_shouldApplyPenalty() {
        // given - create input table
        String inputTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);

        // when
        String resultTable = pipelineRepository.recencyPenalty(
            "mu_quiz_stg", inputTable.substring(inputTable.lastIndexOf('.') + 1), 1L, 1L, 2);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable + " WHERE chance > 0")
            .getSingleResult();
        assertTrue(count > 0);
    }

    @Test
    void recencyPenalty_shouldWorkWithChanceColumn() {
        // given - create input table with chance column (some records with chance = 0)
        String inputTable = "mu_quiz_stg.test_input_with_chance";
        entityManager.createNativeQuery("DROP TABLE IF EXISTS " + inputTable).executeUpdate();
        entityManager.createNativeQuery(
            "CREATE TABLE " + inputTable + " AS " +
            "SELECT id as track_id, primary_artist_id, " +
            "CASE WHEN id = 1 THEN 0.0 ELSE 1.0 END as chance " +
            "FROM mu_view.v_track").executeUpdate();

        // when
        String resultTable = pipelineRepository.recencyPenalty(
            "mu_quiz_stg", "test_input_with_chance", 1L, 1L, 2);

        // then
        assertNotNull(resultTable);
        Long totalCount = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable).getSingleResult();
        Long zeroChanceCount = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable + " WHERE track_id = 1").getSingleResult();
        
        assertTrue(totalCount > 0);
        assertEquals(0L, zeroChanceCount); // Record with chance=0 should be filtered out
    }

    @Test
    void artistRecencyPenalty_shouldApplyArtistPenalty() {
        // given - create input table
        String inputTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);

        // when
        String resultTable = pipelineRepository.artistRecencyPenalty(
                "mu_quiz_stg", inputTable.substring(inputTable.lastIndexOf('.') + 1), 1L, 1L, 2);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM " + resultTable + " WHERE chance > 0")
                .getSingleResult();
        assertTrue(count > 0);
    }

    @Test
    void artistDiversity_shouldWorkWithoutChanceColumn() {
        // given - create input table without chance column
        String inputTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);

        // when
        String resultTable = pipelineRepository.artistDiversity(
            "mu_quiz_stg", inputTable.substring(inputTable.lastIndexOf('.') + 1), 1L, 1L, 3);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable + " WHERE chance > 0")
            .getSingleResult();
        assertTrue(count > 0);
    }

    @Test
    void whitelistFilter_shouldWorkWithoutChanceColumn() {
        // given - create input table and whitelist
        String inputTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);
        
        String whitelistTable = "mu_quiz_stg.test_whitelist";
        entityManager.createNativeQuery("DROP TABLE IF EXISTS " + whitelistTable).executeUpdate();
        entityManager.createNativeQuery(
            "CREATE TABLE " + whitelistTable + " (category_id BIGINT, weight DECIMAL)").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO " + whitelistTable + " VALUES (999, 0.5)").executeUpdate();

        // when
        String resultTable = pipelineRepository.whitelistFilter(
            "mu_quiz_stg", inputTable.substring(inputTable.lastIndexOf('.') + 1),
            1L, 1L, 2, "mu_quiz_stg", "test_whitelist");

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable)
            .getSingleResult();
        assertEquals(0L, count); // No tracks should remain since no artists have category 999
    }

    @Test
    void blacklistFilter_shouldFilterBlacklistedCategories() {
        // given - create input table and blacklist
        String inputTable = pipelineRepository.approvedFilter("mu_view", "v_track", 1L, 1L, 1);
        
        String blacklistTable = "mu_quiz_stg.test_blacklist";
        entityManager.createNativeQuery("DROP TABLE IF EXISTS " + blacklistTable).executeUpdate();
        entityManager.createNativeQuery(
            "CREATE TABLE " + blacklistTable + " (category_id BIGINT)").executeUpdate();
        entityManager.createNativeQuery("INSERT INTO " + blacklistTable + " VALUES (999)").executeUpdate(); // Non-existent category

        // when
        String resultTable = pipelineRepository.blacklistFilter(
            "mu_quiz_stg", inputTable.substring(inputTable.lastIndexOf('.') + 1),
            1L, 1L, 2, "mu_quiz_stg", "test_blacklist");

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable)
            .getSingleResult();
        assertEquals(3L, count); // All tracks should remain since category 999 doesn't exist
    }

    @Test
    void finalSelection_shouldLimitResults() {
        // given - create input table with chance column
        String inputTable = "mu_quiz_stg.test_input";
        entityManager.createNativeQuery("DROP TABLE IF EXISTS " + inputTable).executeUpdate();
        entityManager.createNativeQuery(
            "CREATE TABLE " + inputTable + " AS " +
            "SELECT id as track_id, primary_artist_id, 1.0 as chance FROM mu_view.v_track").executeUpdate();

        // when
        String resultTable = pipelineRepository.finalSelection(
            "mu_quiz_stg", "test_input", 1L, 1L, 3, 2);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable)
            .getSingleResult();
        assertTrue(count <= 2); // Should limit to target count
    }

    @Test
    void runPipeline_shouldExecuteFullPipeline() {
        // when
        String resultTable = pipelineRepository.runPipeline(1L, 1L, 2);

        // then
        assertNotNull(resultTable);
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM " + resultTable)
            .getSingleResult();
        assertTrue(count > 0);
        assertTrue(count <= 2);
    }
}
