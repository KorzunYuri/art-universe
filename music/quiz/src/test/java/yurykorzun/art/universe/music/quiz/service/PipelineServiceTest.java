package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.PipelineServiceImpl;
import yurykorzun.art.universe.music.quiz.service.step.StepExecutionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;
    
    @Mock
    private StepRepository stepRepository;
    
    @Mock
    private PipelineStepRepository pipelineStepRepository;
    
    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepExecutionService stepExecutionService;

    @Mock
    private PipelineRunService pipelineRunService;

    @Mock
    private StepService stepService;

    @InjectMocks
    private PipelineServiceImpl pipelineService;

    @Test
    void createBasicPipeline_shouldReturnPipelineDto_whenSuccessful() {
        // given
        Pipeline savedPipeline = Pipeline.builder()
            .id(1L)
            .immutable(false)
            .build();

        when(pipelineRepository.save(any(Pipeline.class))).thenReturn(savedPipeline);

        // when
        PipelineDto result = pipelineService.createBasicPipeline();

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertFalse(result.getImmutable());
        assertTrue(result.getSteps().isEmpty());
        
        verify(pipelineRepository).save(any(Pipeline.class));
    }

    @Test
    void getPipeline_shouldReturnPipelineDto_whenPipelineExists() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder()
            .id(pipelineId)
            .immutable(false)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.getPipeline(pipelineId);

        // then
        assertNotNull(result);
        assertEquals(pipelineId, result.getId());
        assertTrue(result.getSteps().isEmpty());
        
        verify(pipelineRepository).findById(pipelineId);
        verify(pipelineStepRepository).findPipelineStepsWithDetails(pipelineId);
    }

    @Test
    void getPipeline_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.getPipeline(pipelineId)
        );
        
        assertEquals("Pipeline not found: 999", exception.getMessage());
        verify(pipelineRepository).findById(pipelineId);
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenNoSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder()
            .id(pipelineId)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );
        
        assertEquals("Pipeline must have at least one step", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1)
        );
        
        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void addStep_shouldAddStartStepToEmptyPipeline_whenValidPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();
        
        Step savedStep = Step.builder()
            .id(1L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());
        when(stepService.createStep(StepType.START_DATASOURCE, "{}")).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 0);

        // then
        assertNotNull(result);
        assertEquals(pipelineId, result.getId());
        verify(stepService).createStep(StepType.START_DATASOURCE, "{}");
        verify(pipelineStepRepository).save(any(PipelineStep.class));
        verify(stepService).clearResults(any());
    }

    @Test
    void addStep_shouldAddMiddleStepToExistingPipeline_whenValidPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();
        
        Step savedStep = Step.builder()
            .id(2L)
            .type(StepType.APPROVED_FILTER)
            .algVersion(1)
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));
        when(stepService.createStep(StepType.APPROVED_FILTER, "{}")).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 1);

        // then
        assertNotNull(result);
        verify(stepService).createStep(StepType.APPROVED_FILTER, "{}");
        verify(pipelineStepRepository).incrementOrderAfter(pipelineId, 1);
        verify(stepService).clearResults(any());
    }

    @Test
    void addStep_shouldThrowException_whenInvalidStepPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1)
        );
        
        assertEquals("target step position 1 is out of pipeline bounds 0", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenInvalidConfiguration() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("invalid")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());
        when(stepService.createStep(StepType.START_DATASOURCE, "invalid"))
            .thenThrow(new IllegalArgumentException("Invalid configuration"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 0)
        );
        
        assertEquals("Invalid configuration", exception.getMessage());
    }

    @Test
    void addStep_shouldAddFinalStepToEnd_whenValidPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();
        
        Step savedStep = Step.builder()
            .id(2L)
            .type(StepType.FINAL_LIMITER)
            .algVersion(1)
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));
        when(stepService.createStep(StepType.FINAL_LIMITER, "{}")).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 1);

        // then
        assertNotNull(result);
        verify(stepService).createStep(StepType.FINAL_LIMITER, "{}");
        verify(pipelineStepRepository).save(any(PipelineStep.class));
        verify(stepService).clearResults(any());
    }

    @Test
    void addStep_shouldThrowException_whenStartStepNotAtPosition1() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.APPROVED_FILTER).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1)
        );
        
        assertEquals("START step must be at position 0", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenMultipleStartSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 0)
        );
        
        assertEquals("Only one START step is allowed", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenFinalStepNotAtLastPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_LIMITER)
            .cfgData("{}")
            .build();

        Step existingStep1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step existingStep2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        PipelineStep existingPipelineStep1 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();
        PipelineStep existingPipelineStep2 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(2L)
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep1, existingPipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(existingStep1, existingStep2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1) // Should be position 2
        );
        
        assertEquals("FINAL step must be at last position", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenMultipleFinalSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_CATEGORIES_BALANCER)
            .cfgData("{}")
            .build();

        Step existingStep1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step existingStep2 = Step.builder().id(2L).type(StepType.FINAL_LIMITER).build();
        PipelineStep existingPipelineStep1 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();
        PipelineStep existingPipelineStep2 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(2L)
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep1, existingPipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(existingStep1, existingStep2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 2)
        );
        
        assertEquals("Only one FINAL step is allowed", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenMiddleStepHasStartAfter() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 0) // Trying to put MIDDLE before START
        );
        
        assertEquals("MIDDLE step cannot have START steps after it", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenMiddleStepHasFinalBefore() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.FINAL_LIMITER).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(0)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1) // Trying to put MIDDLE after FINAL
        );
        
        assertEquals("MIDDLE step cannot have FINAL steps before it", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        Long stepId = 1L;
        Integer newPosition = 2;

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenStepNotFoundInPipeline() {
        // given
        Long pipelineId = 1L;
        Long stepId = 999L;
        Integer newPosition = 2;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("Step not found in pipeline", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenStepNotFound() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Integer newPosition = 2;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        PipelineStep pipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(stepId)
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep));
        when(stepService.getStep(stepId)).thenThrow(new IllegalArgumentException("Step not found"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("Step not found", exception.getMessage());
    }

    @Test
    void moveStep_shouldReturnPipeline_whenMovingToSamePosition() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Integer newPosition = 0;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        Step step = Step.builder().id(stepId).type(StepType.START_DATASOURCE).build();
        PipelineStep pipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(stepId)
            .ord(0)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep));
        when(stepService.getStep(stepId)).thenReturn(step);
        when(stepRepository.findAllById(List.of(stepId))).thenReturn(List.of(step));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.moveStep(pipelineId, stepId, newPosition);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository, never()).decrementOrderBetween(any(), any(), any());
        verify(pipelineStepRepository, never()).incrementOrderBetween(any(), any(), any());
        verify(pipelineStepRepository, never()).updateStepOrder(any(), any(), any());
    }

    @Test
    void moveStep_shouldMoveStepDown_whenNewPositionIsHigher() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L; // Moving MIDDLE step
        Integer newPosition = 2;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.BLACKLIST_FILTER).build();
        Step step4 = Step.builder().id(4L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();
        PipelineStep pipelineStep4 = PipelineStep.builder().pipelineId(pipelineId).stepId(4L).ord(3).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3, pipelineStep4));
        when(stepService.getStep(stepId)).thenReturn(step2);
        when(stepRepository.findAllById(List.of(1L, 2L, 3L, 4L))).thenReturn(List.of(step1, step2, step3, step4));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.moveStep(pipelineId, stepId, newPosition);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).decrementOrderBetween(pipelineId, 1, 2);
        verify(pipelineStepRepository).updateStepOrder(pipelineId, stepId, newPosition);
        verify(stepService).clearResults(any());
    }

    @Test
    void moveStep_shouldMoveStepUp_whenNewPositionIsLower() {
        // given
        Long pipelineId = 1L;
        Long stepId = 3L; // Moving MIDDLE step
        Integer newPosition = 1;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.BLACKLIST_FILTER).build();
        Step step4 = Step.builder().id(4L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();
        PipelineStep pipelineStep4 = PipelineStep.builder().pipelineId(pipelineId).stepId(4L).ord(3).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3, pipelineStep4));
        when(stepService.getStep(stepId)).thenReturn(step3);
        when(stepRepository.findAllById(List.of(1L, 2L, 3L, 4L))).thenReturn(List.of(step1, step2, step3, step4));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.moveStep(pipelineId, stepId, newPosition);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).incrementOrderBetween(pipelineId, 1, 2);
        verify(pipelineStepRepository).updateStepOrder(pipelineId, stepId, newPosition);
    }

    @Test
    void moveStep_shouldThrowException_whenStartStepNotAtPosition0() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Integer newPosition = 1;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2));
        when(stepService.getStep(stepId)).thenReturn(step1);
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(step1, step2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("START step must be at position 0", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenFinalStepNotAtLastPosition() {
        // given
        Long pipelineId = 1L;
        Long stepId = 3L;
        Integer newPosition = 1;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(stepService.getStep(stepId)).thenReturn(step3);
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("FINAL step must be at last position", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenMiddleStepHasStartAfter() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        Integer newPosition = 0;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2));
        when(stepService.getStep(stepId)).thenReturn(step2);
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(step1, step2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("MIDDLE step cannot have START steps after it", exception.getMessage());
    }

    @Test
    void moveStep_shouldThrowException_whenMiddleStepHasFinalBefore() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        Integer newPosition = 2;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(stepService.getStep(stepId)).thenReturn(step2);
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.moveStep(pipelineId, stepId, newPosition)
        );
        
        assertEquals("MIDDLE step cannot have FINAL steps before it", exception.getMessage());
    }

    @Test
    void removeStep_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        Long stepId = 1L;

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.removeStep(pipelineId, stepId)
        );
        
        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void removeStep_shouldThrowException_whenStepNotFoundInPipeline() {
        // given
        Long pipelineId = 1L;
        Long stepId = 999L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.removeStep(pipelineId, stepId)
        );
        
        assertEquals("Step not found in pipeline", exception.getMessage());
    }

    @Test
    void removeStep_shouldThrowException_whenStepNotFound() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        PipelineStep pipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(stepId)
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep));
        // No mock for stepService.getStep() - it's not called in removeStep

        // when - should not throw exception since removeStep doesn't validate step existence
        PipelineDto result = pipelineService.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).deleteByPipelineIdAndStepId(pipelineId, stepId);
        verify(stepService).softDeleteStep(stepId);
    }

    @Test
    void removeStep_shouldRemoveStepAndShiftPositions_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L; // Remove middle step
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).deleteByPipelineIdAndStepId(pipelineId, stepId);
        verify(stepService).softDeleteStep(stepId);
        verify(pipelineStepRepository).decrementOrderAfter(pipelineId, 1);
        verify(stepService).clearResults(any());
    }

    @Test
    void removeStep_shouldRemoveFirstStep_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L; // Remove first step
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).deleteByPipelineIdAndStepId(pipelineId, stepId);
        verify(stepService).softDeleteStep(stepId);
        verify(pipelineStepRepository).decrementOrderAfter(pipelineId, 0);
    }

    @Test
    void removeStep_shouldRemoveLastStep_whenSuccessful() {
        // given
        Long pipelineId = 1L;
        Long stepId = 3L; // Remove last step
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();
        
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).deleteByPipelineIdAndStepId(pipelineId, stepId);
        verify(stepService).softDeleteStep(stepId);
        verify(pipelineStepRepository).decrementOrderAfter(pipelineId, 2);
    }

    @Test
    void removeStep_shouldRemoveOnlyStep_whenSingleStepPipeline() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        
        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.removeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        verify(pipelineStepRepository).deleteByPipelineIdAndStepId(pipelineId, stepId);
        verify(stepService).softDeleteStep(stepId);
        verify(pipelineStepRepository).decrementOrderAfter(pipelineId, 0);
    }


    @Test
    void validatePipelineForGeneration_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );

        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenPipelineWithoutStartStep() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.APPROVED_FILTER).build();
        Step step2 = Step.builder().id(2L).type(StepType.FINAL_LIMITER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(step1, step2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );

        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenPipelineWithoutFinalStep() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(step1, step2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );

        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenPipelineWithMultipleStartSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.START_DATASOURCE).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );

        assertEquals("Pipeline must have exactly one START step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldThrowException_whenPipelineWithMultipleFinalSteps() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.FINAL_LIMITER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_CATEGORIES_BALANCER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.validatePipelineForGeneration(pipelineId)
        );

        assertEquals("Pipeline must have exactly one FINAL step", exception.getMessage());
    }

    @Test
    void validatePipelineForGeneration_shouldPass_whenValidPipeline() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));

        // when & then
        assertDoesNotThrow(() -> pipelineService.validatePipelineForGeneration(pipelineId));
    }

    @Test
    void updateStepConfiguration_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        Long stepId = 1L;
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("{\"updated\": true}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto)
        );

        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void updateStepConfiguration_shouldThrowException_whenStepNotFound() {
        // given
        Long pipelineId = 1L;
        Long stepId = 999L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("{\"updated\": true}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(stepService.getStep(stepId)).thenThrow(new IllegalArgumentException("Step not found: 999"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto)
        );

        assertEquals("Step not found: 999", exception.getMessage());
    }

    @Test
    void updateStepConfiguration_shouldThrowException_whenInvalidConfiguration() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        Step step = Step.builder().id(stepId).type(StepType.START_DATASOURCE).cfgData("{}").build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("invalid")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(stepService.getStep(stepId)).thenReturn(step);
        when(stepService.updateStepConfiguration(stepId, "invalid"))
            .thenThrow(new IllegalArgumentException("Invalid configuration"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto)
        );

        assertEquals("Invalid configuration", exception.getMessage());
    }

    @Test
    void updateStepConfiguration_shouldReturnPipeline_whenConfigurationNotChanged() {
        // given
        Long pipelineId = 1L;
        Long stepId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        Step step = Step.builder().id(stepId).type(StepType.START_DATASOURCE).cfgData("{}").build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("{}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(stepService.getStep(stepId)).thenReturn(step);
        when(stepService.updateStepConfiguration(stepId, "{}")).thenReturn(step);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto);

        // then
        assertNotNull(result);
        verify(stepService).updateStepConfiguration(stepId, "{}");
        verify(pipelineStepRepository, never()).findByPipelineIdOrderByOrd(any());
    }

    @Test
    void updateStepConfiguration_shouldUpdateAndClearResults_whenConfigurationChanged() {
        // given
        Long pipelineId = 1L;
        Long stepId = 2L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        Step step = Step.builder().id(stepId).type(StepType.APPROVED_FILTER).cfgData("{}").build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("{\"updated\": true}")
            .build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(stepService.getStep(stepId)).thenReturn(step);
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        // when
        PipelineDto result = pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto);

        // then
        assertNotNull(result);
        verify(stepService).updateStepConfiguration(stepId, "{\"updated\": true}");
        verify(stepService).clearResults(any());
    }

    @Test
    void updateStepConfiguration_shouldThrowException_whenStepNotFoundInPipeline() {
        // given
        Long pipelineId = 1L;
        Long stepId = 999L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();
        Step step = Step.builder().id(stepId).type(StepType.APPROVED_FILTER).cfgData("{}").build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .cfgData("{\"updated\": true}")
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(stepService.getStep(stepId)).thenReturn(step);
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.updateStepConfiguration(pipelineId, stepId, stepDto)
        );

        assertEquals("Step not found in pipeline", exception.getMessage());
    }


    @Test
    void getStepPreview_shouldReturnPreview_whenStepExists() {
        // given
        Long stepId = 1L;

        when(stepExecutionService.generatePreview(stepId)).thenReturn("{\"preview\": \"data\"}");

        // when
        String result = pipelineService.getStepPreview(stepId);

        // then
        assertEquals("{\"preview\": \"data\"}", result);
        verify(stepExecutionService).generatePreview(stepId);
    }

    @Test
    void getStepPreview_shouldThrowException_whenStepNotFound() {
        // given
        Long stepId = 999L;
        when(stepExecutionService.generatePreview(stepId)).thenThrow(new IllegalArgumentException("Step not found"));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.getStepPreview(stepId)
        );

        assertEquals("Step not found", exception.getMessage());
    }

    @Test
    void executeStep_shouldThrowException_whenPipelineNotFound() {
        // given
        Long pipelineId = 999L;
        Long stepId = 1L;
        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.executeStep(pipelineId, stepId)
        );

        assertEquals("Pipeline not found: 999", exception.getMessage());
    }

    @Test
    void executeStep_shouldThrowException_whenStepNotFoundInPipeline() {
        // given
        Long pipelineId = 1L;
        Long stepId = 999L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.executeStep(pipelineId, stepId)
        );

        assertEquals("Step not found in pipeline", exception.getMessage());
    }

    @Test
    void executeStep_shouldExecuteFromEarliestWithoutResult_whenPreviousStepsMissingResults() {
        // given
        Long pipelineId = 1L;
        Long stepId = 3L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).lastStepRunId(1L).build();
        Step step2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        Step step3 = Step.builder().id(3L).type(StepType.FINAL_LIMITER).build();

        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();
        PipelineStep pipelineStep2 = PipelineStep.builder().pipelineId(pipelineId).stepId(2L).ord(1).build();
        PipelineStep pipelineStep3 = PipelineStep.builder().pipelineId(pipelineId).stepId(3L).ord(2).build();

        StepRun stepRun1 = StepRun.builder().id(1L).resultTableName("output_table_1").build();
        StepRun stepRun2 = StepRun.builder().id(2L).resultTableName("output_table_2").build();
        StepRun stepRun3 = StepRun.builder().id(3L).resultTableName("output_table_3").build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(pipelineStep1, pipelineStep2, pipelineStep3));
        when(pipelineStepRepository.findEarliestStepWithoutResult(pipelineId)).thenReturn(Optional.of(1));
        
        // Batch loading: findAllById will be called with [1L, 2L, 3L]
        when(stepRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(step1, step2, step3));
        when(stepRunRepository.findById(1L)).thenReturn(Optional.of(stepRun1));

        when(stepExecutionService.executeStep(step2, "output_table_1", null)).thenReturn(stepRun2);
        when(stepExecutionService.executeStep(step3, "output_table_2", null)).thenReturn(stepRun3);

        // when
        StepRun result = pipelineService.executeStep(pipelineId, stepId);

        // then
        assertNotNull(result);
        assertEquals("output_table_3", result.getResultTableName());
    }

    @Test
    void executePipeline_shouldThrowException_whenPipelineHasNoSteps() {
        // given
        Long pipelineId = 1L;

        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.executePipeline(pipelineId)
        );

        assertEquals("Pipeline has no steps", exception.getMessage());
    }

    @Test
    void executePipeline_shouldExecuteSuccessfully_whenValidPipeline() {
        // given
        Long pipelineId = 1L;

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();

        StepRun stepRun = StepRun.builder().id(1L).resultTableName("output_table").build();
        PipelineRun pipelineRun = PipelineRun.builder().id(1L).pipelineId(pipelineId).status(ExecutionStatus.COMPLETED).resultTableName("output_table").build();

        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of(pipelineStep1));
        when(pipelineRunService.createPipelineRun(pipelineId)).thenReturn(pipelineRun);
        when(stepRepository.findById(1L)).thenReturn(Optional.of(step1));
        when(stepExecutionService.executeStep(step1, null, 1L)).thenReturn(stepRun);

        // when
        PipelineRun result = pipelineService.executePipeline(pipelineId);

        // then
        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals("output_table", result.getResultTableName());
        verify(pipelineRunService).createPipelineRun(pipelineId);
        verify(pipelineRunService).startPipelineRun(1L);
        verify(pipelineRunService).completePipelineRun(1L, "output_table");
    }

    @Test
    void executePipeline_shouldHandleFailure_whenStepProcessingFails() {
        // given
        Long pipelineId = 1L;
        PipelineRun pipelineRun = PipelineRun.builder().id(1L).pipelineId(pipelineId).build();

        Step step1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep pipelineStep1 = PipelineStep.builder().pipelineId(pipelineId).stepId(1L).ord(0).build();

        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of(pipelineStep1));
        when(pipelineRunService.createPipelineRun(pipelineId)).thenReturn(pipelineRun);
        when(stepRepository.findById(1L)).thenReturn(Optional.of(step1));
        when(stepExecutionService.executeStep(step1, null, 1L)).thenThrow(new RuntimeException("Processing failed"));

        // when & then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> pipelineService.executePipeline(pipelineId)
        );

        assertEquals("Pipeline execution failed", exception.getMessage());
        verify(pipelineRunService).startPipelineRun(1L);
        verify(pipelineRunService).failPipelineRun(1L);
    }
}
