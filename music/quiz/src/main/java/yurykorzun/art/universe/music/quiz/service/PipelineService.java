package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;

public interface PipelineService {
    
    PipelineDto createBasicPipeline();
    
    PipelineDto addStep(Long pipelineId, PipelineStepDto stepDto, Integer position);
    
    PipelineDto moveStep(Long pipelineId, Long stepId, Integer newPosition);
    
    PipelineDto removeStep(Long pipelineId, Long stepId);
    
    PipelineDto updateStepConfiguration(Long pipelineId, Long stepId, PipelineStepDto stepDto);
    
    PipelineDto getPipeline(Long pipelineId);
    
    String getStepPreview(Long stepId);
    
    PipelineDto executeStep(Long pipelineId, Long stepId);
    
    String executePipeline(Long pipelineId, Long pipelineRunId);
    
    void validatePipelineForGeneration(Long pipelineId);
}
