package yurykorzun.art.universe.music.quiz.entity.step.middle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BlacklistFilterStep extends MiddleGenerationStep {
    private final List<Long> categoryIds;
    
    public BlacklistFilterStep(List<Long> categoryIds) {
        super(GenerationStepType.BLACKLIST_FILTER);
        this.categoryIds = categoryIds;
    }
}
