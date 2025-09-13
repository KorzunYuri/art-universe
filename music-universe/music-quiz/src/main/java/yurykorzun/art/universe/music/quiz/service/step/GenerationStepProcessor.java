package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

public interface GenerationStepProcessor {
    
    GenerationStepType getStepType();
    
    void validate(GenerationStep step);
    
    String process(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step);
}
