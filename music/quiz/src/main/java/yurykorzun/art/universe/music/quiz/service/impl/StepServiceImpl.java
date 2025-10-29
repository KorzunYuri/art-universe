package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.service.StepService;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StepServiceImpl implements StepService {

    private final StepRepository stepRepository;
    private final StepProcessorRegistry stepProcessorRegistry;

    @Override
    @Transactional
    public Step createStep(StepType type, String cfgData) {
        log.debug("Creating step of type {} with config: {}", type, cfgData);
        
        String validatedConfig = validateAndMigrateConfiguration(type, cfgData);
        
        Step step = Step.builder()
            .type(type)
            .algVersion(type.getVersion())
            .cfgData(validatedConfig)
            .deleted(false)
            .immutable(false)
            .build();
        
        return stepRepository.save(step);
    }

    @Override
    @Transactional
    public Step createImmutableCopy(Long stepId) {
        log.debug("Creating immutable copy of step {}", stepId);
        Step originalStep = getStep(stepId);
        
        Step immutableStep = Step.builder()
            .type(originalStep.getType())
            .algVersion(originalStep.getAlgVersion())
            .cfgData(originalStep.getCfgData())
            .deleted(false)
            .immutable(true)
            .build();
        
        return stepRepository.save(immutableStep);
    }

    @Override
    @Transactional
    public Step updateStepConfiguration(Long stepId, String cfgData) {
        log.debug("Updating configuration for step {}", stepId);
        
        Step step = getStep(stepId);
        String validatedConfig = validateAndMigrateConfiguration(step.getType(), cfgData);
        
        step.setCfgData(validatedConfig);
        return stepRepository.save(step);
    }

    @Override
    @Transactional
    public void softDeleteStep(Long stepId) {
        log.debug("Soft deleting step {}", stepId);
        stepRepository.softDelete(stepId);
    }

    @Override
    @Transactional(readOnly = true)
    public Step getStep(Long stepId) {
        return stepRepository.findById(stepId)
            .orElseThrow(() -> new IllegalArgumentException("Step not found: " + stepId));
    }

    @Override
    public StepMetadataProjection getStepMetadata(Long stepId) {
        return stepRepository.getStepMetadata(stepId);
    }

    @Override
    public String validateAndMigrateConfiguration(StepType stepType, String cfgData) {
        StepProcessor processor = stepProcessorRegistry.get(stepType);
        return processor.verifyConfigurationIsActual(cfgData);
    }

    @Override
    public void updatePreview(Long stepId, String preview) {
        stepRepository.updatePreview(stepId, preview);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void clearResults(List<Long> stepIds) {
        log.debug("Clearing results for {} steps", stepIds.size());
        stepRepository.clearLastRun(stepIds);
    }
}
