package yurykorzun.art.universe.music.quiz.service.step;

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
    private StepRepository stepRepository;

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
    void completeStepRun_shouldCompleteStepRunAndUpdateStep() throws Exception {
        // given
        StepRun stepRun = StepRun.builder()
            .id(1L)
            .status(ExecutionStatus.STARTED)
            .build();
        
        Step step = Step.builder()
            .id(1L)
            .build();

        BasicStepStats stats = new BasicStepStats();
        stats.setOutputRecords(10L);

        when(stepRunRepository.findById(1L)).thenReturn(Optional.of(stepRun));
        when(stepRepository.findById(1L)).thenReturn(Optional.of(step));
        when(objectMapper.writeValueAsString(stats)).thenReturn("{\"outputRecords\":10}");

        // when
        stepRunService.completeStepRun(1L, "result.table", stats, 1L);

        // then
        assertEquals(ExecutionStatus.COMPLETED, stepRun.getStatus());
        assertEquals("result.table", stepRun.getResultTableName());
        assertEquals("{\"outputRecords\":10}", stepRun.getResultStats());
        assertNotNull(stepRun.getCompletedAt());
        assertEquals(1L, step.getLastStepRunId());
        verify(stepRunRepository).save(stepRun);
        verify(stepRepository).save(step);
    }

    @Test
    void completeStepRun_shouldThrowException_whenStepRunNotFound() {
        // given
        when(stepRunRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> stepRunService.completeStepRun(999L, "result.table", new BasicStepStats(), 1L)
        );
        
        assertEquals("Failed to complete step run", exception.getMessage());
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
}
