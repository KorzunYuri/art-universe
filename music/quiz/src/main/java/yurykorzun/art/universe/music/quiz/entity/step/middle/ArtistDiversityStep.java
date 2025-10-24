package yurykorzun.art.universe.music.quiz.entity.step.middle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ArtistDiversityStep extends MiddleGenerationStep {
    
    public ArtistDiversityStep() {
        super(GenerationStepType.ARTIST_DIVERSITY);
    }
}
