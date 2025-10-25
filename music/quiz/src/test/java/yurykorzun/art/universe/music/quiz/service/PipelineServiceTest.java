package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.impl.PipelineServiceImpl;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessorRegistry;

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
    private PipelineRunRepository pipelineRunRepository;
    
    @Mock
    private StepRunRepository stepRunRepository;

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

        GenerationStepProcessor mockProcessor = mock(GenerationStepProcessor.class);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());
        when(stepRepository.save(any(Step.class))).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        try (MockedStatic<GenerationStepProcessorRegistry> mockedRegistry = mockStatic(GenerationStepProcessorRegistry.class)) {
            mockedRegistry.when(() -> GenerationStepProcessorRegistry.get(StepType.START_DATASOURCE))
                .thenReturn(mockProcessor);

            // when
            PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 1);

            // then
            assertNotNull(result);
            assertEquals(pipelineId, result.getId());
            verify(mockProcessor).validateConfiguration("{}");
            verify(stepRepository).save(any(Step.class));
            verify(pipelineStepRepository).save(any(PipelineStep.class));
        }
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
            .ord(1)
            .build();
        
        Step savedStep = Step.builder()
            .id(2L)
            .type(StepType.APPROVED_FILTER)
            .algVersion(1)
            .cfgData("{}")
            .build();

        GenerationStepProcessor mockProcessor = mock(GenerationStepProcessor.class);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));
        when(stepRepository.save(any(Step.class))).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        try (MockedStatic<GenerationStepProcessorRegistry> mockedRegistry = mockStatic(GenerationStepProcessorRegistry.class)) {
            mockedRegistry.when(() -> GenerationStepProcessorRegistry.get(StepType.APPROVED_FILTER))
                .thenReturn(mockProcessor);

            // when
            PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 2);

            // then
            assertNotNull(result);
            verify(mockProcessor).validateConfiguration("{}");
            verify(pipelineStepRepository).incrementOrderAfter(pipelineId, 2);
            verify(stepRepository).clearSubsequentStepResults(pipelineId, 2);
        }
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
        IndexOutOfBoundsException exception = assertThrows(
            IndexOutOfBoundsException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 2) // START step must be at position 1
        );
        
        assertTrue(exception.getMessage().contains("Index: 1, Size: 0"));
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

        GenerationStepProcessor mockProcessor = mock(GenerationStepProcessor.class);
        doThrow(new IllegalArgumentException("Invalid configuration"))
            .when(mockProcessor).validateConfiguration("invalid");

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId)).thenReturn(List.of());

        try (MockedStatic<GenerationStepProcessorRegistry> mockedRegistry = mockStatic(GenerationStepProcessorRegistry.class)) {
            mockedRegistry.when(() -> GenerationStepProcessorRegistry.get(StepType.START_DATASOURCE))
                .thenReturn(mockProcessor);

            // when & then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> pipelineService.addStep(pipelineId, stepDto, 1)
            );
            
            assertEquals("Invalid configuration", exception.getMessage());
        }
    }

    @Test
    void addStep_shouldAddFinalStepToEnd_whenValidPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_SELECTION)
            .cfgData("{}")
            .build();

        Step existingStep = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(1)
            .build();
        
        Step savedStep = Step.builder()
            .id(2L)
            .type(StepType.FINAL_SELECTION)
            .algVersion(1)
            .cfgData("{}")
            .build();

        GenerationStepProcessor mockProcessor = mock(GenerationStepProcessor.class);

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));
        when(stepRepository.save(any(Step.class))).thenReturn(savedStep);
        when(pipelineStepRepository.findPipelineStepsWithDetails(pipelineId)).thenReturn(List.of());

        try (MockedStatic<GenerationStepProcessorRegistry> mockedRegistry = mockStatic(GenerationStepProcessorRegistry.class)) {
            mockedRegistry.when(() -> GenerationStepProcessorRegistry.get(StepType.FINAL_SELECTION))
                .thenReturn(mockProcessor);

            // when
            PipelineDto result = pipelineService.addStep(pipelineId, stepDto, 2);

            // then
            assertNotNull(result);
            verify(mockProcessor).validateConfiguration("{}");
            verify(stepRepository).save(any(Step.class));
            verify(pipelineStepRepository).save(any(PipelineStep.class));
        }
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
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 2)
        );
        
        assertEquals("START step must be at position 1", exception.getMessage());
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
            .ord(1)
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
        
        assertEquals("Only one START step is allowed", exception.getMessage());
    }

    @Test
    void addStep_shouldThrowException_whenFinalStepNotAtLastPosition() {
        // given
        Long pipelineId = 1L;
        Pipeline pipeline = Pipeline.builder().id(pipelineId).immutable(false).build();
        PipelineStepDto stepDto = PipelineStepDto.builder()
            .type(StepType.FINAL_SELECTION)
            .cfgData("{}")
            .build();

        Step existingStep1 = Step.builder().id(1L).type(StepType.START_DATASOURCE).build();
        Step existingStep2 = Step.builder().id(2L).type(StepType.APPROVED_FILTER).build();
        PipelineStep existingPipelineStep1 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(1)
            .build();
        PipelineStep existingPipelineStep2 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(2L)
            .ord(2)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep1, existingPipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(existingStep1, existingStep2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 2) // Should be position 3
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
        Step existingStep2 = Step.builder().id(2L).type(StepType.FINAL_SELECTION).build();
        PipelineStep existingPipelineStep1 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(1)
            .build();
        PipelineStep existingPipelineStep2 = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(2L)
            .ord(2)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep1, existingPipelineStep2));
        when(stepRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(existingStep1, existingStep2));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 3)
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
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 1) // Trying to put MIDDLE before START
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

        Step existingStep = Step.builder().id(1L).type(StepType.FINAL_SELECTION).build();
        PipelineStep existingPipelineStep = PipelineStep.builder()
            .pipelineId(pipelineId)
            .stepId(1L)
            .ord(1)
            .build();

        when(pipelineRepository.findById(pipelineId)).thenReturn(Optional.of(pipeline));
        when(pipelineStepRepository.findByPipelineIdOrderByOrd(pipelineId))
            .thenReturn(List.of(existingPipelineStep));
        when(stepRepository.findAllById(List.of(1L))).thenReturn(List.of(existingStep));

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> pipelineService.addStep(pipelineId, stepDto, 2) // Trying to put MIDDLE after FINAL
        );
        
        assertEquals("MIDDLE step cannot have FINAL steps before it", exception.getMessage());
    }
}
