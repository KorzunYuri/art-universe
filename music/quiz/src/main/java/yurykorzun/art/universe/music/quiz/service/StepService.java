package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepMetadataProjection;

import java.util.List;

public interface StepService {

    Step getStep(Long stepId);
    List<Step> getSteps(List<Long> stepIds);
    StepMetadataProjection getStepMetadata(Long id);

    Step createStep(StepType type, String cfgData);
    Step createImmutableCopy(Long stepId);
    void softDeleteStep(Long stepId);
    
    Step updateStepConfiguration(Long stepId, String cfgData);
    String validateAndMigrateConfiguration(StepType stepType, String cfgData);

    void updatePreview(Long stepId, String preview);

    void updateLastRun(Long stepId, Long lastRunId);

    void clearResults(List<Long> stepIds);
}
