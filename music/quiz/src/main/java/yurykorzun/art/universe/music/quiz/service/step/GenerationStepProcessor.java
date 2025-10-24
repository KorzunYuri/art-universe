package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.entity.step.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public interface GenerationStepProcessor<T extends GenerationStep> {
    
    GenerationStepType getStepType();
    
    Integer getCurrentVersion();
    
    String process(String inputTable, Long gameId, Long generationId, Integer stepOrder, T step);
    
    void validateConfiguration(String cfgData);
    
    String getPreview(String cfgData);
    
    String migrateConfiguration(String cfgData, Integer fromVersion, Integer toVersion);
}
