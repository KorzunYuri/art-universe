package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.entity.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

public interface GenerationStepProcessor<T extends GenerationStep> {
    
    GenerationStepType getStepType();
    
    String process(String inputTable, Long gameId, Long generationId, Integer stepOrder, T step);
}
