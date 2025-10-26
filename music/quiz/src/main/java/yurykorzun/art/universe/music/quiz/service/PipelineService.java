package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;
import yurykorzun.art.universe.music.quiz.entity.PipelineRun;
import yurykorzun.art.universe.music.quiz.entity.StepRun;

public interface PipelineService {
    
    PipelineDto createBasicPipeline();
    
    PipelineDto addStep(Long pipelineId, PipelineStepDto stepDto, Integer position);
    
    PipelineDto moveStep(Long pipelineId, Long stepId, Integer newPosition);
    
    PipelineDto removeStep(Long pipelineId, Long stepId);
    
    PipelineDto updateStepConfiguration(Long pipelineId, Long stepId, PipelineStepDto stepDto);
    
    PipelineDto getPipeline(Long pipelineId);
    
    String getStepPreview(Long stepId);
    
    StepRun executeStep(Long pipelineId, Long stepId);
    
    PipelineRun executePipeline(Long pipelineId);
    
    void validatePipelineForGeneration(Long pipelineId);
}
