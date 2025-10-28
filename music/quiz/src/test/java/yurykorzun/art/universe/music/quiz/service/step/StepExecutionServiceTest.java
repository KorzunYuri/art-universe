package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BasicStepStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.service.StepService;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepExecutionServiceTest {

    @Mock
    private StepProcessorRegistry stepProcessorRegistry;

    @Mock
    private StepRunService stepRunService;

    @Mock
    private StepService stepService;

    @Mock
    private StepProcessor stepProcessor;

    @Mock
    private StepMetadataProjection stepMetadata;

    @InjectMocks
    private StepExecutionServiceImpl stepExecutionService;

    @Test
    void generatePreview_shouldReturnProcessorPreview() {
        // given
        Long stepId = 1L;
        Step step = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .build();

        when(stepService.getStep(stepId)).thenReturn(step);
        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getPreview(step)).thenReturn("{\"preview\":\"data\"}");

        // when
        String result = stepExecutionService.generatePreview(stepId);

        // then
        assertEquals("{\"preview\":\"data\"}", result);
        verify(stepService).getStep(stepId);
        verify(stepProcessor).getPreview(step);
        verify(stepService).updatePreview(stepId, "{\"preview\":\"data\"}");
    }

    @Test
    void executeStep_shouldExecuteSuccessfully() {
        // given
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

        StepRunResult stepResult = StepRunResult.builder()
            .outputTableName("output.table")
            .build();

        BasicStepStats stats = new BasicStepStats();
        stats.setOutputRecords(10L);

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getStepType()).thenReturn(StepType.START_DATASOURCE);
        when(stepRunService.createStepRun(step, "input.table", 1L)).thenReturn(stepRun);
        when(stepService.getStepMetadata(1L)).thenReturn(stepMetadata);
        when(stepMetadata.getGameId()).thenReturn(1L);
        when(stepMetadata.getPipelineId()).thenReturn(1L);
        when(stepProcessor.processStep(eq(step), eq("input.table"), anyString(), eq(stepRun))).thenReturn(stepResult);
        when(stepProcessor.getResultStats(any(StepRun.class))).thenReturn(stats);

        // when
        StepRun result = stepExecutionService.executeStep(step, "input.table", 1L);

        // then
        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("output.table", result.getResultTableName());
        
        verify(stepRunService).createStepRun(step, "input.table", 1L);
        verify(stepRunService).updateStepRunStatus(eq(1L), eq(ExecutionStatus.STARTED));
        verify(stepRunService).completeStepRun(1L, "output.table", stats, 1L);
        verify(stepProcessor).processStep(eq(step), eq("input.table"), anyString(), eq(stepRun));
    }

    @Test
    void executeStep_shouldHandleFailure() {
        // given
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

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getStepType()).thenReturn(StepType.START_DATASOURCE);
        when(stepRunService.createStepRun(step, "input.table", 1L)).thenReturn(stepRun);
        when(stepService.getStepMetadata(1L)).thenReturn(stepMetadata);
        when(stepMetadata.getGameId()).thenReturn(1L);
        when(stepMetadata.getPipelineId()).thenReturn(1L);
        when(stepProcessor.processStep(eq(step), eq("input.table"), anyString(), eq(stepRun)))
            .thenThrow(new RuntimeException("Processing failed"));

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(step, "input.table", 1L)
        );

        assertEquals("Step processing failed", exception.getMessage());
        verify(stepRunService).failStepRun(1L);
    }

    @Test
    void executeStep_shouldThrowException_whenNullStep() {
        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepExecutionService.executeStep(null, "input.table", 1L)
        );

        assertEquals("Step cannot be null", exception.getMessage());
    }



    @Test
    void executeStep_shouldThrowException_whenStepTypeMismatch() {
        // given
        Step step = Step.builder()
            .type(StepType.START_DATASOURCE)
            .build();

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getStepType()).thenReturn(StepType.APPROVED_FILTER);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepExecutionService.executeStep(step, "input.table", 1L)
        );

        assertTrue(exception.getMessage().contains("Step type mismatch"));
    }

    @Test
    void getResultStats_shouldReturnProcessorStats() {
        // given
        StepRun stepRun = StepRun.builder()
            .stepType(StepType.START_DATASOURCE)
            .build();

        BasicStepStats stats = new BasicStepStats();
        stats.setOutputRecords(10L);

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getResultStats(stepRun)).thenReturn(stats);

        // when
        var result = stepExecutionService.getResultStats(stepRun);

        // then
        assertNotNull(result);
        assertEquals(10L, result.getOutputRecords());
        verify(stepProcessor).getResultStats(stepRun);
    }
}
