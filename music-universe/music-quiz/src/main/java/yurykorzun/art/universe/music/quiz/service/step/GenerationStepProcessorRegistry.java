package yurykorzun.art.universe.music.quiz.service.step;

import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.quiz.entity.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GenerationStepProcessorRegistry {

    private static final Map<GenerationStepType, GenerationStepProcessor<?>> REGISTRY = new ConcurrentHashMap<>();

    private GenerationStepProcessorRegistry() {}

    public static void register(GenerationStepProcessor<?> processor) {
        GenerationStepType stepType = processor.getStepType();
        GenerationStepProcessor<?> existing = REGISTRY.putIfAbsent(stepType, processor);
        if (existing != null) {
            // don't throw exception - breaks processor tests
            log.warn("Processor already exists for type " + stepType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends GenerationStep> GenerationStepProcessor<T> get(GenerationStepType stepType) {
        GenerationStepProcessor<?> processor = REGISTRY.get(stepType);
        if (processor == null) {
            throw new IllegalArgumentException("No processor found for step type: " + stepType);
        }
        return (GenerationStepProcessor<T>) processor;
    }
}
