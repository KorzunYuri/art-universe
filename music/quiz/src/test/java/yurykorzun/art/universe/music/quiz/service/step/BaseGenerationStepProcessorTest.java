package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.CommonTestConfig;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseGenerationStepProcessorTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private ObjectMapper objectMapper;

    // Test processor for testing BaseGenerationStepProcessor
    private static class TestProcessor extends BaseGenerationStepProcessor {
        
        public TestProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
            super(StepType.START_DATASOURCE, stepRunRepository, stepRepository, objectMapper);
        }

        @Override
        protected String getStepSuffix() {
            return "test";
        }

        @Override
        protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
            return StepRunResult.builder()
                .outputTableName(outputTableName)
                .build();
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = CommonTestConfig.getObjectMapper();
    }

    @Test
    void process_shouldCreateStepRunAndReturnResult() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder()
            .id(1L)
            .stepId(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        StepMetadataProjection metadata = mock(StepMetadataProjection.class);
        when(metadata.getGameId()).thenReturn(1L);
        when(metadata.getPipelineId()).thenReturn(1L);

        when(stepRunRepository.save(any(StepRun.class))).thenReturn(stepRun);
        when(stepRepository.getStepMetadata(1L)).thenReturn(metadata);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);

        // when
        StepRun result = processor.process(step, null, null);

        // then
        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getResultTableName());
        assertNotNull(result.getResultStats());
        verify(stepRunRepository, atLeast(2)).save(any(StepRun.class));
        verify(stepRepository).save(step);
    }

    @Test
    void getStepType_shouldReturnCorrectType() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);

        // when
        StepType result = processor.getStepType();

        // then
        assertEquals(StepType.START_DATASOURCE, result);
    }

    @Test
    void process_shouldHandleProcessingFailure_whenExceptionThrown() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder()
            .id(1L)
            .stepId(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        StepMetadataProjection metadata = mock(StepMetadataProjection.class);
        when(metadata.getGameId()).thenReturn(1L);
        when(metadata.getPipelineId()).thenReturn(1L);

        when(stepRunRepository.save(any(StepRun.class))).thenReturn(stepRun);
        when(stepRepository.getStepMetadata(1L)).thenReturn(metadata);
        when(entityManager.createNativeQuery(anyString())).thenThrow(new RuntimeException("DB error"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> processor.process(step, null, null));
        
        assertEquals("Step processing failed", exception.getMessage());
        verify(stepRunRepository, times(3)).save(any(StepRun.class)); // PENDING, STARTED, FAILED
    }

    @Test
    void process_shouldValidateStep_whenStepIsNull() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.process(null, null, null));
        
        assertEquals("Step cannot be null", exception.getMessage());
    }

    @Test
    void process_shouldValidateStepType_whenTypeMismatch() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.FINAL_SELECTION) // Wrong type
            .algVersion(1)
            .cfgData("{}")
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.process(step, null, null));
        
        assertTrue(exception.getMessage().contains("Step type mismatch"));
    }

    @Test
    void process_shouldValidateInputTable_whenInvalidFormat() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.process(step, "invalid_table_name", null));
        
        assertEquals("Input table must be in format 'schema.table'", exception.getMessage());
    }

    @Test
    void process_shouldValidateInputTable_whenNullOrEmpty() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.process(step, "", null));
        
        assertEquals("Input table cannot be null or empty", exception.getMessage());
    }

    @Test
    void process_shouldProcessWithInputTable_whenValidFormat() {
        // given
        TestProcessor processor = new TestProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        StepRun stepRun = StepRun.builder()
            .id(1L)
            .stepId(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        StepMetadataProjection metadata = mock(StepMetadataProjection.class);
        when(metadata.getGameId()).thenReturn(1L);
        when(metadata.getPipelineId()).thenReturn(1L);

        when(stepRunRepository.save(any(StepRun.class))).thenReturn(stepRun);
        when(stepRepository.getStepMetadata(1L)).thenReturn(metadata);
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(5L);

        // when
        StepRun result = processor.process(step, "schema.table", null);

        // then
        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getResultTableName());
        assertNotNull(result.getResultStats());
    }
}
