package yurykorzun.art.universe.music.quiz.entity.step.finish;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FinalSelectionStep extends FinalGenerationStep {
    private final Integer targetCount;
    
    public FinalSelectionStep(Integer targetCount) {
        super(GenerationStepType.FINAL_SELECTION);
        this.targetCount = targetCount;
    }
}
