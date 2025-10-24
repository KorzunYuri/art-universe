package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.PipelineDto;
import yurykorzun.art.universe.music.quiz.dto.PipelineStepDto;

public interface PipelineService {
    
    PipelineDto createBasicPipeline(Long gameId);
    
    PipelineDto addStep(Long gameId, PipelineStepDto stepDto, Integer position);
    
    PipelineDto moveStep(Long gameId, Long stepId, Integer newPosition);
    
    PipelineDto removeStep(Long gameId, Long stepId);
    
    PipelineDto updateStepConfiguration(Long gameId, Long stepId, PipelineStepDto stepDto);
    
    PipelineDto getPipeline(Long gameId);
    
    String getStepPreview(Long stepId);
    
    PipelineDto executeStep(Long gameId, Long stepId);
    
    void validatePipelineForGeneration(Long gameId);
}
