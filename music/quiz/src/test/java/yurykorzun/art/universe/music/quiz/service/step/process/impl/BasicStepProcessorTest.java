package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.persistence.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicStepProcessorTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private TestStepProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TestStepProcessor(mock(StepProcessorRegistry.class));
        processor.setEntityManager(entityManager);
    }

    @Test
    void getPreview_shouldReturnEmptyJson() {
        // given
        Step step = Step.builder().build();

        // when
        String result = processor.getPreview(step);

        // then
        assertEquals("{}", result);
    }

    @Test
    void verifyConfigurationIsActual_shouldReturnSameConfig_whenValid() {
        // given
        String validConfig = "{}";

        // when
        String result = processor.verifyConfigurationIsActual(validConfig);

        // then
        assertEquals(validConfig, result);
    }

    @Test
    void processStep_shouldThrowException_whenNullInputTable() {
        // given
        Step step = Step.builder().cfgData("{}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, null, "output.table", stepRun)
        );

        assertEquals("DB object name cannot be null or empty", exception.getMessage());
    }

    @Test
    void processStep_shouldThrowException_whenEmptyInputTable() {
        // given
        Step step = Step.builder().cfgData("{}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, "  ", "output.table", stepRun)
        );

        assertEquals("DB object name cannot be null or empty", exception.getMessage());
    }

    @Test
    void processStep_shouldThrowException_whenInvalidInputTableFormat() {
        // given
        Step step = Step.builder().cfgData("{}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, "invalid_table", "output.table", stepRun)
        );

        assertEquals("DB object name must be in format 'schemaName.table', got: 'invalid_table'", exception.getMessage());
    }

    @Test
    void processStep_shouldThrowException_whenTooManyDotsInInputTable() {
        // given
        Step step = Step.builder().cfgData("{}").build();
        StepRun stepRun = StepRun.builder().build();

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> processor.processStep(step, "schema.table.extra", "output.table", stepRun)
        );

        assertEquals("DB object name must be in format 'schemaName.table', got: 'schema.table.extra'", exception.getMessage());
    }

    @Test
    void getResultStats_shouldCalculateBasicStats_whenBothTablesExist() {
        // given
        StepRun stepRun = StepRun.builder()
            .inputTableName("input.table")
            .resultTableName("output.table")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "input.table")).thenReturn(true);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "output.table")).thenReturn(true);
            
            Query inputCountQuery = mock(Query.class);
            Query inputArtistQuery = mock(Query.class);
            Query outputCountQuery = mock(Query.class);
            Query outputArtistQuery = mock(Query.class);
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM input.table")).thenReturn(inputCountQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM input.table")).thenReturn(inputArtistQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM output.table")).thenReturn(outputCountQuery);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM output.table")).thenReturn(outputArtistQuery);
            
            when(inputCountQuery.getSingleResult()).thenReturn(10L);
            when(inputArtistQuery.getSingleResult()).thenReturn(5L);
            when(outputCountQuery.getSingleResult()).thenReturn(8L);
            when(outputArtistQuery.getSingleResult()).thenReturn(4L);

            // when
            var result = processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            Assertions.assertEquals(10L, result.getInputRecords());
            Assertions.assertEquals(5L, result.getInputArtists());
            Assertions.assertEquals(2L, result.getFilteredRecords());
            Assertions.assertEquals(1L, result.getFilteredArtists());
            Assertions.assertEquals(8L, result.getOutputRecords());
            Assertions.assertEquals(4L, result.getOutputArtists());
        }
    }

    @Test
    void getResultStats_shouldHandleStartStep_whenInputTableNotExists() {
        // given
        StepRun stepRun = StepRun.builder()
            .inputTableName("input.table")
            .resultTableName("output.table")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "input.table")).thenReturn(false);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "output.table")).thenReturn(true);
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM output.table")).thenReturn(query);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM output.table")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(8L).thenReturn(4L);

            // when
            var result = processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            Assertions.assertEquals(0L, result.getInputRecords());
            Assertions.assertEquals(0L, result.getInputArtists());
            Assertions.assertEquals(-8L, result.getFilteredRecords()); // 0 - 8 = -8
            Assertions.assertEquals(-4L, result.getFilteredArtists()); // 0 - 4 = -4
            Assertions.assertEquals(8L, result.getOutputRecords());
            Assertions.assertEquals(4L, result.getOutputArtists());
        }
    }

    @Test
    void getResultStats_shouldHandleNoOutput_whenOutputTableNotExists() {
        // given
        StepRun stepRun = StepRun.builder()
            .inputTableName("input.table")
            .resultTableName("output.table")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "input.table")).thenReturn(true);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "output.table")).thenReturn(false);
            
            when(entityManager.createNativeQuery("SELECT COUNT(*) FROM input.table")).thenReturn(query);
            when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM input.table")).thenReturn(query);
            when(query.getSingleResult()).thenReturn(10L).thenReturn(5L);

            // when
            var result = processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            Assertions.assertEquals(10L, result.getInputRecords());
            Assertions.assertEquals(5L, result.getInputArtists());
            Assertions.assertEquals(10L, result.getFilteredRecords());
            Assertions.assertEquals(5L, result.getFilteredArtists());
            Assertions.assertEquals(0L, result.getOutputRecords());
            Assertions.assertEquals(0L, result.getOutputArtists());
        }
    }

    @Test
    void getResultStats_shouldReturnZeros_whenBothTablesNotExist() {
        // given
        StepRun stepRun = StepRun.builder()
            .inputTableName("input.table")
            .resultTableName("output.table")
            .build();

        try (var mockedStatic = mockStatic(DatabaseUtils.class)) {
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "input.table")).thenReturn(false);
            mockedStatic.when(() -> DatabaseUtils.tableExists(entityManager, "output.table")).thenReturn(false);

            // when
            var result = processor.getResultStats(stepRun);

            // then
            assertNotNull(result);
            Assertions.assertEquals(0L, result.getInputRecords());
            Assertions.assertEquals(0L, result.getInputArtists());
            Assertions.assertEquals(0L, result.getFilteredRecords());
            Assertions.assertEquals(0L, result.getFilteredArtists());
            Assertions.assertEquals(0L, result.getOutputRecords());
            Assertions.assertEquals(0L, result.getOutputArtists());
        }
    }

    // Test processor implementation
    private static class TestStepProcessor extends BasicStepProcessor {
        
        public TestStepProcessor(StepProcessorRegistry registry) {
            super(registry);
        }

        @Override
        public StepType getStepType() {
            return StepType.APPROVED_FILTER;
        }

        @Override
        protected StepRunResult executeStepLogic(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
            return StepRunResult.builder()
                .outputTableName(stepTableNameBase + "_test")
                .build();
        }
    }
}
