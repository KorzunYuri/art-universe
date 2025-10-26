package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.lang.Nullable;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.ExecutionStatus;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;

public interface StepRunService {
    
    StepRun createStepRun(Step step, String inputTableName, @Nullable Long pipelineRunId);
    
    void updateStepRunStatus(Long stepRunId, ExecutionStatus status, String resultTableName);
    
    void completeStepRun(Long stepRunId, String resultTableName, StepRunStats stats, Long stepId);
    
    void failStepRun(Long stepRunId);
}
