package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.entity.PipelineRun;

public interface PipelineRunService {
    
    PipelineRun createPipelineRun(Long pipelineId);

    PipelineRun startPipelineRun(Long pipelineRunId);

    PipelineRun completePipelineRun(Long pipelineRunId, String resultTableName);

    PipelineRun failPipelineRun(Long pipelineRunId);
}
