package yurykorzun.art.universe.music.quiz.service.step;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.impl.StartDatasourceProcessor;

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

    @Test
    void process_shouldCreateStepRunAndReturnResult() {
        // given
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
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
        when(query.executeUpdate()).thenReturn(1);
        when(entityManager.createNativeQuery("SELECT COUNT(*) FROM mu_quiz_stg.sr_g1_p1_s1_sr1_start")).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0L);
        when(entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM mu_quiz_stg.sr_g1_p1_s1_sr1_start")).thenReturn(query);

        // when
        StepRunResult result = processor.process(step, null, null);

        // then
        assertNotNull(result);
        assertNotNull(result.getOutputTableName());
        assertNotNull(result.getResultStats());
        verify(stepRunRepository, atLeast(2)).save(any(StepRun.class));
        verify(stepRepository).save(step);
    }

    @Test
    void getStepType_shouldReturnCorrectType() {
        // given
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);

        // when
        StepType result = processor.getStepType();

        // then
        assertEquals(StepType.START_DATASOURCE, result);
    }

    @Test
    void process_shouldHandleProcessingFailure_whenExceptionThrown() {
        // given
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
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
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> processor.process(null, null, null));
        
        assertEquals("Step cannot be null", exception.getMessage());
    }

    @Test
    void process_shouldValidateStepType_whenTypeMismatch() {
        // given
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
        
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
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
        
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
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
        
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
        StartDatasourceProcessor processor = new StartDatasourceProcessor(stepRunRepository, stepRepository);
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
        when(query.executeUpdate()).thenReturn(1);
        when(query.getSingleResult()).thenReturn(5L);

        // when
        StepRunResult result = processor.process(step, "schema.table", null);

        // then
        assertNotNull(result);
        assertNotNull(result.getOutputTableName());
        assertNotNull(result.getResultStats());
        assertEquals(5L, result.getResultStats().getInputRecords());
        assertEquals(5L, result.getResultStats().getOutputRecords());
    }
}
