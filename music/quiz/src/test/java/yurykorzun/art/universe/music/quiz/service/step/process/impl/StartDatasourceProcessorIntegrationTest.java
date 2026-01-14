package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.persistence.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.common.archetypes.BaseQuizJpaTest;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("integration")
@Import({
    StepProcessorRegistry.class,
    StartDatasourceProcessor.class
})
class StartDatasourceProcessorIntegrationTest extends BaseQuizJpaTest {

    @PersistenceContext
    private EntityManager entityManager;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    private StartDatasourceProcessor processor;

    private static final String STG_SCHEMA = "mu_quiz_stg";
    private static final String TEST_DATASOURCE_VALID = STG_SCHEMA + ".test_datasource_valid";
    private static final String TEST_DATASOURCE_INVALID = STG_SCHEMA + ".test_datasource_invalid";
    private static final String TEST_DATASOURCE_MISSING = STG_SCHEMA + ".test_datasource_missing";
    private static final String STEP_TABLENAME_BASE = "test_output";
    private static final String OUTPUT_VIEW_NAME    = "test_output_startds_view";
    private static final String STEP_TABLENAME_BASE_FULL = "mu_quiz_stg.test_output";
    private static final String OUTPUT_VIEW_NAME_FULL = "mu_quiz_stg.test_output_startds_view";

    @BeforeEach
    void setUp() {
        // Create valid test datasource
        entityManager.createNativeQuery("""
            CREATE TABLE %s (
                track_id BIGINT,
                primary_artist_id BIGINT,
                name VARCHAR(255)
            )
        """.formatted(TEST_DATASOURCE_VALID)).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO %s (track_id, primary_artist_id, name) VALUES 
            (1, 101, 'Test Track 1'), (2, 102, 'Test Track 2')
        """.formatted(TEST_DATASOURCE_VALID)).executeUpdate();

        // Create invalid test datasource (missing name field)
        entityManager.createNativeQuery("""
            CREATE TABLE %s (
                id BIGINT,
                artist_id BIGINT
            )
        """.formatted(TEST_DATASOURCE_INVALID)).executeUpdate();

        entityManager.createNativeQuery("""
            INSERT INTO %s (id, artist_id) VALUES 
            (1, 101), (2, 102)
        """.formatted(TEST_DATASOURCE_INVALID)).executeUpdate();
    }

    private void mockConfig(String datasourceReplacement) throws JsonProcessingException {
        StartDatasourceStepConfig config = mock(StartDatasourceStepConfig.class);
        when(config.getDatasource()).thenReturn(datasourceReplacement);

        when(objectMapper.readValue(anyString(), eq(StartDatasourceStepConfig.class))).thenReturn(config);
    }

    @AfterEach
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void tearDown() {
        if (TestTransaction.isActive()) {
            TestTransaction.end();
        }
        // Execute cleanup in new transaction to avoid aborted transaction issues
        entityManager.unwrap(Session.class).doWork(conn -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP VIEW IF EXISTS " + OUTPUT_VIEW_NAME_FULL);
                stmt.execute("DROP TABLE IF EXISTS " + TEST_DATASOURCE_VALID);
                stmt.execute("DROP TABLE IF EXISTS " + TEST_DATASOURCE_INVALID);
            }
        });
    }

    @Test
    void processStep_shouldCreateView_whenValidDatasource() throws JsonProcessingException {

        // given
        Step step = Step.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        mockConfig(TEST_DATASOURCE_VALID);

        // when
        StepRunResult result = processor.processStep(step, null, STEP_TABLENAME_BASE_FULL, stepRun);

        // then
        assertEquals(OUTPUT_VIEW_NAME_FULL, result.getOutputTableName());
        assertTrue(DatabaseUtils.tableExists(entityManager, OUTPUT_VIEW_NAME_FULL));

        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + OUTPUT_VIEW_NAME_FULL)
            .getSingleResult();
        assertEquals(2L, count);

        // Verify all required fields exist
        Boolean hasTrackId = (Boolean) entityManager.createNativeQuery("""
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns 
                    WHERE table_schema = '%s' 
                    AND table_name = '%s' 
                    AND column_name = 'track_id'
                )
            """.formatted(STG_SCHEMA, OUTPUT_VIEW_NAME)).getSingleResult();
        assertTrue(hasTrackId);

        Boolean hasArtistId = (Boolean) entityManager.createNativeQuery("""
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns 
                    WHERE table_schema = '%s' 
                    AND table_name = '%s' 
                    AND column_name = 'primary_artist_id'
                )
            """.formatted(STG_SCHEMA, OUTPUT_VIEW_NAME)).getSingleResult();
        assertTrue(hasArtistId);

        Boolean hasName = (Boolean) entityManager.createNativeQuery("""
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.columns 
                    WHERE table_schema = '%s' 
                    AND table_name = '%s' 
                    AND column_name = 'name'
                )
            """.formatted(STG_SCHEMA, OUTPUT_VIEW_NAME)).getSingleResult();
        assertTrue(hasName);
    }

    @Test
    void processStep_shouldThrowException_whenDatasourceMissingField() throws JsonProcessingException {
        // given
        Step step = Step.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        mockConfig(TEST_DATASOURCE_INVALID);

        StepRun stepRun = StepRun.builder().build();

        // when & then
        assertThrows(RuntimeException.class, 
            () -> processor.processStep(step, null, STEP_TABLENAME_BASE_FULL, stepRun));
    }

    @Test
    void processStep_shouldThrowException_whenDatasourceNotExists() throws JsonProcessingException {

        // given
        Step step = Step.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder().build();

        mockConfig(TEST_DATASOURCE_MISSING);

        // when & then
        assertThrows(RuntimeException.class, 
            () -> processor.processStep(step, null, STEP_TABLENAME_BASE_FULL, stepRun));
    }
}
