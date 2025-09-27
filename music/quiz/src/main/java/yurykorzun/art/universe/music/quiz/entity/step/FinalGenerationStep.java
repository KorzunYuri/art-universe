package yurykorzun.art.universe.music.quiz.entity.step;

import yurykorzun.art.universe.music.quiz.entity.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

public abstract class FinalGenerationStep extends GenerationStep {
    
    protected FinalGenerationStep(GenerationStepType type) {
        super(type);
    }
    
    @Override
    public boolean isFinal() {
        return true;
    }
    
    public abstract Integer getTargetCount();
}
