package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.entity.step.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public abstract class BaseGenerationStepProcessor<T extends GenerationStep> implements GenerationStepProcessor<T> {
    
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
    public String process(String inputTable, Long gameId, Long generationId, Integer stepOrder, T step) {
        validateInputTable(inputTable);
        return processStep(inputTable, gameId, generationId, stepOrder, step);
    }
    
    private void validateInputTable(String inputTable) {
        if (inputTable == null || inputTable.trim().isEmpty()) {
            throw new IllegalArgumentException("Input table cannot be null or empty");
        }
        String[] parts = inputTable.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Input table must be in format 'schema.table'");
        }
    }
    
    protected abstract String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, T step);
}
