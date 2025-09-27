package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApprovedFilterStep extends GenerationStep {
    
    public ApprovedFilterStep() {
        super(GenerationStepType.APPROVED_FILTER);
    }
}
