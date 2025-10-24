package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public interface GenerationStepProcessor {
    
    GenerationStepType getStepType();

    Integer getStepTypeVersion();
    
    String process(Step step, String inputTableName, @Nullable Long pipelineRunId);
    
    void validateConfiguration(String cfgData);
    
    String getPreview(String cfgData);
}
