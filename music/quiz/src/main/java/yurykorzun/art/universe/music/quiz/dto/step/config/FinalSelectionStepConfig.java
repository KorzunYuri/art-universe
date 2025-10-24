package yurykorzun.art.universe.music.quiz.dto.step.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import yurykorzun.art.universe.music.quiz.entity.StepType;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FinalSelectionStepConfig extends StepConfig {

    private final Integer targetCount;
    
    public FinalSelectionStepConfig(Integer targetCount) {
        this.targetCount = targetCount;
    }

    @Override
    public StepType getType() {
        return StepType.FINAL_SELECTION;
    }
}
