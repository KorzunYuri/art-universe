package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.Data;

@Data
public abstract class GenerationStep {
    private final GenerationStepType type;
    
    protected GenerationStep(GenerationStepType type) {
        this.type = type;
    }

    public abstract GenerationStepPosition getPosition();
}
