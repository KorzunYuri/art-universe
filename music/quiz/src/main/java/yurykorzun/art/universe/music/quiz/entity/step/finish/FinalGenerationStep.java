package yurykorzun.art.universe.music.quiz.entity.step.finish;

import yurykorzun.art.universe.music.quiz.entity.step.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepPosition;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public abstract class FinalGenerationStep extends GenerationStep {
    
    protected FinalGenerationStep(GenerationStepType type) {
        super(type);
    }

    @Override
    public GenerationStepPosition getPosition() {
        return GenerationStepPosition.FINAL;
    }

    public abstract Integer getTargetCount();
}
