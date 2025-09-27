package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

import java.util.List;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BlacklistFilterStep extends GenerationStep {
    private final List<Long> categoryIds;
    
    public BlacklistFilterStep(List<Long> categoryIds) {
        super(GenerationStepType.BLACKLIST_FILTER);
        this.categoryIds = categoryIds;
    }
}
