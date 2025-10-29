package yurykorzun.art.universe.music.quiz.service.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BasicStepStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepRunServiceTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private StepRunServiceImpl stepRunService;

    @Test
    void createStepRun_shouldCreateAndSaveStepRun() {
        // given
        Step step = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();
        
        StepRun savedStepRun = StepRun.builder()
            .id(1L)
            .stepId(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        when(stepRunRepository.save(any(StepRun.class))).thenReturn(savedStepRun);

        // when
        StepRun result = stepRunService.createStepRun(step, "input.table", 1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(ExecutionStatus.PENDING, result.getStatus());
        verify(stepRunRepository).save(any(StepRun.class));
    }

    @Test
    void updateStepRunStatus_shouldUpdateStatusAndStartTime() {
        // given
        StepRun stepRun = StepRun.builder()
            .id(1L)
            .status(ExecutionStatus.PENDING)
            .build();

        when(stepRunRepository.findById(1L)).thenReturn(Optional.of(stepRun));

        // when
        stepRunService.updateStepRunStatus(1L, ExecutionStatus.STARTED);

        // then
        assertEquals(ExecutionStatus.STARTED, stepRun.getStatus());
        assertNotNull(stepRun.getStartedAt());
        verify(stepRunRepository).save(stepRun);
    }

    @Test
    void updateStepRunStatus_shouldThrowException_whenStepRunNotFound() {
        // given
        when(stepRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepRunService.updateStepRunStatus(999L, ExecutionStatus.STARTED)
        );
        
        assertEquals("StepRun not found: 999", exception.getMessage());
    }

    @Test
    void completeStepRun_shouldCompleteStepRun() throws Exception {
        // given
        StepRun stepRun = StepRun.builder()
            .id(1L)
            .status(ExecutionStatus.STARTED)
            .build();

        when(stepRunRepository.findById(1L)).thenReturn(Optional.of(stepRun));

        // when
        stepRunService.completeStepRun(1L, "result.table", 1L);

        // then
        assertEquals(ExecutionStatus.COMPLETED, stepRun.getStatus());
        assertEquals("result.table", stepRun.getResultTableName());
        assertNotNull(stepRun.getCompletedAt());
        verify(stepRunRepository).save(stepRun);
    }

    @Test
    void completeStepRun_shouldThrowException_whenStepRunNotFound() {
        // given
        when(stepRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepRunService.completeStepRun(999L, "result.table", 1L)
        );
        
        assertEquals("StepRun not found: 999", exception.getMessage());
    }

    @Test
    void failStepRun_shouldMarkStepRunAsFailed() {
        // given
        StepRun stepRun = StepRun.builder()
            .id(1L)
            .status(ExecutionStatus.STARTED)
            .build();

        when(stepRunRepository.findById(1L)).thenReturn(Optional.of(stepRun));

        // when
        stepRunService.failStepRun(1L);

        // then
        assertEquals(ExecutionStatus.FAILED, stepRun.getStatus());
        assertNotNull(stepRun.getCompletedAt());
        verify(stepRunRepository).save(stepRun);
    }

    @Test
    void failStepRun_shouldThrowException_whenStepRunNotFound() {
        // given
        when(stepRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepRunService.failStepRun(999L)
        );
        
        assertEquals("StepRun not found: 999", exception.getMessage());
    }

    @Test
    void setResultStats_shouldUpdateStepRunWithStats() throws Exception {
        // given
        final long stepRunId = 1L;
        BasicStepStats stats = new BasicStepStats();
        stats.setOutputRecords(10L);
        stats.setExecutionTimeMs(1500L);

        StepRun stepRun = StepRun.builder()
            .id(stepRunId)
            .status(ExecutionStatus.COMPLETED)
            .build();

        StepRun updatedStepRun = StepRun.builder()
            .id(stepRunId)
            .status(ExecutionStatus.COMPLETED)
            .resultStats("{\"outputRecords\":10,\"executionTimeMs\":1500}")
            .build();

        when(stepRunRepository.findById(stepRunId)).thenReturn(Optional.of(stepRun));
        when(objectMapper.writeValueAsString(stats)).thenReturn("{\"outputRecords\":10,\"executionTimeMs\":1500}");
        when(stepRunRepository.save(stepRun)).thenReturn(updatedStepRun);

        // when
        StepRun result = stepRunService.setResultStats(stepRunId, stats);

        // then
        assertNotNull(result);
        assertEquals("{\"outputRecords\":10,\"executionTimeMs\":1500}", result.getResultStats());
        verify(stepRunRepository).findById(stepRunId);
        verify(objectMapper).writeValueAsString(stats);
        verify(stepRunRepository).save(stepRun);
    }

    @Test
    void setResultStats_shouldThrowException_whenStepRunNotFound() throws JsonProcessingException {
        // given
        final long stepRunId = 999L;
        BasicStepStats stats = new BasicStepStats();

        when(stepRunRepository.findById(stepRunId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepRunService.setResultStats(stepRunId, stats)
        );

        assertEquals("StepRun not found: " + stepRunId, exception.getMessage());
        verify(stepRunRepository).findById(stepRunId);
        verify(objectMapper, never()).writeValueAsString(any());
        verify(stepRunRepository, never()).save(any());
    }
}
