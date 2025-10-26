package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.entity.PipelineRun;

public interface PipelineRunService {
    
    PipelineRun createPipelineRun(Long pipelineId);
    
    void startPipelineRun(Long pipelineRunId);
    
    void completePipelineRun(Long pipelineRunId, String resultTableName);
    
    void failPipelineRun(Long pipelineRunId);
}
