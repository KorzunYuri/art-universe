package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;

public interface GenerationStepProcessor {
    
    StepType getStepType();
    
    Integer getStepTypeVersion();
    
    StepRun process(Step step, String inputTableName, @Nullable Long pipelineRunId);
    
    void validateConfiguration(String cfgData);
    
    String getPreview(String cfgData);
    
    String migrateConfiguration(String cfgData, Integer fromVersion, Integer toVersion);
    
    StepRunStats getResultStats(StepRun stepRun);
}
