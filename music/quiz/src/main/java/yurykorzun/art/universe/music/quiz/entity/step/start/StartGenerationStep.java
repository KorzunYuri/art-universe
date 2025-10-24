package yurykorzun.art.universe.music.quiz.entity.step.start;

import yurykorzun.art.universe.music.quiz.entity.step.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepPosition;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

public class StartGenerationStep extends GenerationStep {

    protected StartGenerationStep(GenerationStepType type) {
        super(type);
    }

    @Override
    public GenerationStepPosition getPosition() {
        return GenerationStepPosition.START;
    }
}
