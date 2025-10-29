package yurykorzun.art.universe.music.quiz.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.service.impl.StepServiceImpl;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StepServiceTest {

    @Mock
    private StepRepository stepRepository;

    @Mock
    private StepProcessorRegistry stepProcessorRegistry;

    @Mock
    private StepProcessor stepProcessor;

    @InjectMocks
    private StepServiceImpl stepService;

    @Test
    void createStep_shouldCreateAndSaveStep_whenValidInput() {
        // given
        StepType stepType = StepType.START_DATASOURCE;
        String cfgData = "{}";
        String validatedConfig = "{\"validated\": true}";
        
        Step savedStep = Step.builder()
            .id(1L)
            .type(stepType)
            .algVersion(stepType.getVersion())
            .cfgData(validatedConfig)
            .deleted(false)
            .immutable(false)
            .build();

        when(stepProcessorRegistry.get(stepType)).thenReturn(stepProcessor);
        when(stepProcessor.verifyConfigurationIsActual(cfgData)).thenReturn(validatedConfig);
        when(stepRepository.save(any(Step.class))).thenReturn(savedStep);

        // when
        Step result = stepService.createStep(stepType, cfgData);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(stepType, result.getType());
        assertEquals(validatedConfig, result.getCfgData());
        assertFalse(result.getDeleted());
        assertFalse(result.getImmutable());
        
        verify(stepProcessorRegistry).get(stepType);
        verify(stepProcessor).verifyConfigurationIsActual(cfgData);
        verify(stepRepository).save(any(Step.class));
    }

    @Test
    void createImmutableCopy_shouldCreateImmutableStep_whenValidInput() {
        // given
        Long stepId = 1L;
        Step originalStep = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .deleted(false)
            .immutable(false)
            .build();
        
        Step immutableStep = Step.builder()
            .id(2L)
            .type(StepType.START_DATASOURCE)
            .algVersion(1)
            .cfgData("{}")
            .deleted(false)
            .immutable(true)
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(originalStep));
        when(stepRepository.save(any(Step.class))).thenReturn(immutableStep);

        // when
        Step result = stepService.createImmutableCopy(stepId);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(originalStep.getType(), result.getType());
        assertEquals(originalStep.getAlgVersion(), result.getAlgVersion());
        assertEquals(originalStep.getCfgData(), result.getCfgData());
        assertFalse(result.getDeleted());
        assertTrue(result.getImmutable());
        
        verify(stepRepository).findById(stepId);
        verify(stepRepository).save(any(Step.class));
    }

    @Test
    void updateStepConfiguration_shouldUpdateAndSaveStep_whenValidInput() {
        // given
        Long stepId = 1L;
        String newCfgData = "{\"updated\": true}";
        String validatedConfig = "{\"validated\": true}";
        
        Step existingStep = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .cfgData("{}")
            .build();
        
        Step updatedStep = Step.builder()
            .id(stepId)
            .type(StepType.START_DATASOURCE)
            .cfgData(validatedConfig)
            .build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(existingStep));
        when(stepProcessorRegistry.get(StepType.START_DATASOURCE)).thenReturn(stepProcessor);
        when(stepProcessor.verifyConfigurationIsActual(newCfgData)).thenReturn(validatedConfig);
        when(stepRepository.save(existingStep)).thenReturn(updatedStep);

        // when
        Step result = stepService.updateStepConfiguration(stepId, newCfgData);

        // then
        assertNotNull(result);
        assertEquals(validatedConfig, result.getCfgData());
        
        verify(stepRepository).findById(stepId);
        verify(stepProcessorRegistry).get(StepType.START_DATASOURCE);
        verify(stepProcessor).verifyConfigurationIsActual(newCfgData);
        verify(stepRepository).save(existingStep);
    }

    @Test
    void softDeleteStep_shouldMarkStepAsDeleted_whenStepExists() {
        // given
        Long stepId = 1L;

        // when
        stepService.softDeleteStep(stepId);

        // then
        verify(stepRepository).softDelete(stepId);
    }

    @Test
    void getStep_shouldReturnStep_whenStepExists() {
        // given
        Long stepId = 1L;
        Step existingStep = Step.builder().id(stepId).build();

        when(stepRepository.findById(stepId)).thenReturn(Optional.of(existingStep));

        // when
        Step result = stepService.getStep(stepId);

        // then
        assertNotNull(result);
        assertEquals(stepId, result.getId());
        verify(stepRepository).findById(stepId);
    }

    @Test
    void getStep_shouldThrowException_whenStepNotFound() {
        // given
        Long stepId = 999L;
        when(stepRepository.findById(stepId)).thenReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> stepService.getStep(stepId)
        );
        
        assertEquals("Step not found: 999", exception.getMessage());
        verify(stepRepository).findById(stepId);
    }

    @Test
    void validateAndMigrateConfiguration_shouldReturnValidatedConfig() {
        // given
        StepType stepType = StepType.START_DATASOURCE;
        String cfgData = "{}";
        String validatedConfig = "{\"validated\": true}";

        when(stepProcessorRegistry.get(stepType)).thenReturn(stepProcessor);
        when(stepProcessor.verifyConfigurationIsActual(cfgData)).thenReturn(validatedConfig);

        // when
        String result = stepService.validateAndMigrateConfiguration(stepType, cfgData);

        // then
        assertEquals(validatedConfig, result);
        verify(stepProcessorRegistry).get(stepType);
        verify(stepProcessor).verifyConfigurationIsActual(cfgData);
    }

    @Test
    void clearResults_shouldClearResultsForAllSteps() {
        // given
        List<Long> stepIds = List.of(1L, 2L);

        // when
        stepService.clearResults(stepIds);

        // then
        verify(stepRepository).clearLastRun(eq(stepIds));
    }
}
