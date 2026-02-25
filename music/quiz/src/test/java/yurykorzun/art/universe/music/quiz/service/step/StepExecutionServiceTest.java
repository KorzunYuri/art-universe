package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.CommonConfig;
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

    private ObjectMapper objectMapper = CommonConfig.getObjectMapper();

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
    void executeStep_shouldExecuteSuccessfully() throws JsonProcessingException {
        // given
        final long stepId = 1L;
        final long stepRunId = 2L;
        final long gameId = 3L;
        final long pipelineId = 4L;
        final long pipelineRunId = 5L;
        Step step = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        final var inputTableName = "input.table";
        final var resultTableName = "output.table";
        BasicStepStats resultStats = new BasicStepStats();
        resultStats.setOutputRecords(10L);
        final var resultsStatsJson = objectMapper.writeValueAsString(resultStats);

        StepRun stepRunPending = StepRun.builder()
            .id(stepRunId)
            .stepId(stepId)
            .status(ExecutionStatus.PENDING)
            .inputTableName(inputTableName)
            .build();
        StepRun stepRunStarted = cloneStepRun(stepRunPending)
            .status(ExecutionStatus.STARTED)
            .build();
        StepRun stepRunCompleted = cloneStepRun(stepRunStarted)
            .status(ExecutionStatus.COMPLETED)
            .resultTableName(resultTableName)
            .build();
        StepRun stepRunWithStats = cloneStepRun(stepRunCompleted)
            .resultStats(resultsStatsJson)
            .build();

        StepRunResult stepResult = StepRunResult.builder()
            .outputTableName(resultTableName)
            .build();

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getStepType()).thenReturn(StepType.START_DATASOURCE);

        when(stepRunService.createStepRun(step, inputTableName, pipelineRunId)).thenReturn(stepRunPending);
        when(stepRunService.updateStepRunStatus(stepRunId, ExecutionStatus.STARTED)).thenReturn(stepRunStarted);
        when(stepRunService.completeStepRun(eq(stepRunId), anyString(), eq(step.getId()))).thenReturn(stepRunCompleted);
        when(stepRunService.setResultStats(stepRunId, resultStats)).thenReturn(stepRunWithStats);

        when(stepService.getStepMetadata(stepId)).thenReturn(stepMetadata);
        when(stepMetadata.getGameId()).thenReturn(gameId);
        when(stepMetadata.getPipelineId()).thenReturn(pipelineId);
        when(stepProcessor.processStep(eq(step), eq(inputTableName), anyString(), eq(stepRunStarted))).thenReturn(stepResult);
        when(stepProcessor.getResultStats(any(StepRun.class))).thenReturn(resultStats);

        // when
        StepRun result = stepExecutionService.executeStep(step, inputTableName, pipelineRunId);

        // then
        assertNotNull(result);
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(inputTableName, result.getInputTableName());
        assertEquals(resultTableName, result.getResultTableName());
        assertEquals(resultsStatsJson, result.getResultStats());

        verify(stepRunService).createStepRun(step, inputTableName, pipelineRunId);
        verify(stepRunService).updateStepRunStatus(eq(stepRunId), eq(ExecutionStatus.STARTED));
        verify(stepRunService).completeStepRun(stepRunId, resultTableName, stepId);
        verify(stepProcessor).processStep(eq(step), eq(inputTableName), anyString(), eq(stepRunStarted));
    }

    @Test
    void executeStep_shouldHandleFailure() {
        // given
        final long stepId = 1L;
        final long stepRunId = 2L;
        final long gameId = 3L;
        final long pipelineId = 4L;
        final long pipelineRunId = 5L;
        Step step = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        final var inputTableName = "input.table";

        StepRun stepRunPending = StepRun.builder()
            .id(stepRunId)
            .stepId(stepId)
            .status(ExecutionStatus.PENDING)
            .inputTableName(inputTableName)
            .build();
        StepRun stepRunStarted = cloneStepRun(stepRunPending)
            .status(ExecutionStatus.STARTED)
            .build();

        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.getStepType()).thenReturn(StepType.START_DATASOURCE);

        when(stepRunService.createStepRun(step, inputTableName, pipelineRunId)).thenReturn(stepRunPending);
        when(stepRunService.updateStepRunStatus(stepRunId, ExecutionStatus.STARTED)).thenReturn(stepRunStarted);

        when(stepService.getStepMetadata(stepId)).thenReturn(stepMetadata);
        when(stepMetadata.getGameId()).thenReturn(gameId);
        when(stepMetadata.getPipelineId()).thenReturn(pipelineId);
        when(stepProcessor.processStep(eq(step), eq(inputTableName), anyString(), eq(stepRunStarted)))
            .thenThrow(new RuntimeException("Processing failed"));

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> stepExecutionService.executeStep(step, inputTableName, pipelineRunId)
        );

        assertEquals("Step processing failed", exception.getMessage());
        verify(stepRunService).createStepRun(step, inputTableName, pipelineRunId);
        verify(stepRunService).updateStepRunStatus(stepRunId, ExecutionStatus.STARTED);
        verify(stepRunService).failStepRun(stepRunId);
        verify(stepRunService, never()).completeStepRun(anyLong(), anyString(), anyLong());
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

    private StepRun.StepRunBuilder<?, ?> cloneStepRun(StepRun stepRun) {
        return StepRun.builder()
            .id(stepRun.getId())
            .stepId(stepRun.getStepId())
            .status(stepRun.getStatus())
            .inputTableName(stepRun.getInputTableName())
            .resultTableName(stepRun.getResultTableName())
            .resultStats(stepRun.getResultStats());
    }
}
