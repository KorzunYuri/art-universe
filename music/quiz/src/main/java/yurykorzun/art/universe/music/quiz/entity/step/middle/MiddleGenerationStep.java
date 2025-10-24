package yurykorzun.art.universe.music.quiz.entity.step.middle;

import yurykorzun.art.universe.music.quiz.entity.step.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepPosition;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public abstract class MiddleGenerationStep extends GenerationStep {

    protected MiddleGenerationStep(GenerationStepType type) {
        super(type);
    }

    @Override
    public GenerationStepPosition getPosition() {
        return GenerationStepPosition.MIDDLE;
    }
}
