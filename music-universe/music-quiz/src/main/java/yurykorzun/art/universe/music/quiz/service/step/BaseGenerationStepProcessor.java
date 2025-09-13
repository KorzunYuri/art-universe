package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

public abstract class BaseGenerationStepProcessor implements GenerationStepProcessor {
    
    private final GenerationStepType stepType;
    
    protected BaseGenerationStepProcessor(GenerationStepType stepType) {
        this.stepType = stepType;
        GenerationStepProcessorRegistry.register(this);
    }
    
    @Override
    public GenerationStepType getStepType() {
        return stepType;
    }
    
    @Override
    public void validate(GenerationStep step) {
        if (!stepType.equals(step.getType())) {
            throw new IllegalArgumentException(
                String.format("Step type %s doesn't match processor type %s", step.getType(), stepType));
        }
        validateParams(step);
    }
    
    protected void validateParams(GenerationStep step) {
        // Default implementation - no validation required
    }
    
    @Override
    public String process(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step) {
        validate(step);
        return processStep(inputTable, gameId, generationId, stepId, step);
    }
    
    protected abstract String processStep(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step);
}
