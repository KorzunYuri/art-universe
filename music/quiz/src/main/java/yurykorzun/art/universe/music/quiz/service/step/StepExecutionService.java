package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;

public interface StepExecutionService {

    String getPreview(Step step);

    StepRun executeStep(Step step, String inputTableName, @Nullable Long pipelineRunId);
    
    StepRunStats getResultStats(StepRun stepRun);
}
