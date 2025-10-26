package yurykorzun.art.universe.music.quiz.service.step.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class StepProcessorRegistry {

    private final Map<StepType, StepProcessor> registry = new ConcurrentHashMap<>();

    public void register(StepProcessor processor) {
        StepType stepType = processor.getStepType();
        StepProcessor existing = registry.put(stepType, processor);
        if (existing != null) {
            log.warn("Processor already exists for type " + stepType);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends StepProcessor> T get(StepType stepType) {
        StepProcessor processor = registry.get(stepType);
        if (processor == null) {
            throw new IllegalArgumentException("No processor found for step type: " + stepType);
        }
        return (T) processor;
    }
}
